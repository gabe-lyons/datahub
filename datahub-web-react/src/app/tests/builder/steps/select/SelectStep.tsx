import React, { useMemo } from 'react';
import styled from 'styled-components';
import { Typography } from 'antd';
import { EntityCapabilityType } from '../../../../entity/Entity';
import { useEntityRegistry } from '../../../../useEntityRegistry';
import { EntityTypeSelect } from '../definition/builder/property/input/EntityTypeSelect';
import { LogicalPredicateBuilder } from '../definition/builder/LogicalPredicateBuilder';
import { LogicalPredicate } from '../definition/builder/types';
import { YamlStep } from '../definition/yaml/YamlStep';
import { entityTypesToGraphNames, graphNamesToEntityTypes } from './utils';
import { deserializeTestDefinition, serializeTestDefinition } from '../definition/utils';
import {
    convertLogicalPredicateToTestPredicate,
    convertTestPredicateToLogicalPredicate,
} from '../definition/builder/utils';
import { StepProps, TestBuilderStep } from '../../types';
import { getPropertiesForEntityTypes } from '../definition/builder/property/utils';

const Section = styled.div`
    margin-top: 20px;
    margin-bottom: 10px;
`;

const BuilderWrapper = styled.div`
    margin-bottom: 28px;
`;

export const SelectStep = ({ state, updateState, goTo }: StepProps) => {
    const entityRegistry = useEntityRegistry();
    const testDefinition = useMemo(() => deserializeTestDefinition(state?.definition?.json || '{}'), [state]);

    const onClickNext = () => {
        goTo(TestBuilderStep.RULES);
    };

    const onChangeTypes = (newTypes) => {
        const newDefinition = {
            ...testDefinition,
            on: {
                types: entityTypesToGraphNames(newTypes, entityRegistry),
                conditions: testDefinition.on?.conditions,
            },
        };
        const newState = {
            ...state,
            definition: {
                json: serializeTestDefinition(newDefinition),
            },
        };
        updateState(newState);
    };

    const onChangePredicate = (newPredicate) => {
        const newDefinition = {
            ...testDefinition,
            on: {
                types: testDefinition.on?.types || [],
                conditions: convertLogicalPredicateToTestPredicate(newPredicate),
            },
        };
        const newState = {
            ...state,
            definition: {
                json: serializeTestDefinition(newDefinition),
            },
        };
        updateState(newState);
    };

    const testEntities = Array.from(entityRegistry.getTypesWithSupportedCapabilities(EntityCapabilityType.TEST));
    const selectedEntityTypes = graphNamesToEntityTypes(testDefinition.on?.types || [], entityRegistry);

    return (
        <>
            <YamlStep state={state} updateState={updateState} onNext={onClickNext}>
                <Typography.Title level={4}>Select Assets</Typography.Title>
                <Typography.Paragraph type="secondary">
                    Choose assets to run this test against. Tests are evaluated once every day, and when an asset
                    changes.
                </Typography.Paragraph>
                <Section>
                    <Typography.Title level={5}>Asset Types</Typography.Title>
                    <Typography.Paragraph type="secondary">
                        Select entity types that are eligible to be tested
                    </Typography.Paragraph>
                    <EntityTypeSelect
                        selectedTypes={selectedEntityTypes}
                        entityTypes={testEntities}
                        onChangeTypes={onChangeTypes}
                    />
                </Section>
                <Section>
                    <Typography.Title level={5}>Conditions</Typography.Title>
                    <Typography.Paragraph type="secondary">
                        Run this test for all assets matching the following conditions
                    </Typography.Paragraph>
                    <BuilderWrapper>
                        <LogicalPredicateBuilder
                            selectedPredicate={
                                convertTestPredicateToLogicalPredicate(
                                    testDefinition.on.conditions || [],
                                ) as LogicalPredicate
                            }
                            onChangePredicate={onChangePredicate}
                            properties={getPropertiesForEntityTypes(selectedEntityTypes)}
                            disabled={!testDefinition.on?.types || testDefinition.on?.types.length === 0}
                            options={{
                                predicateDisplayName: 'condition',
                            }}
                        />
                    </BuilderWrapper>
                </Section>
            </YamlStep>
        </>
    );
};
