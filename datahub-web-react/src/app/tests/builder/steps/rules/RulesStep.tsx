import React, { useMemo, useState } from 'react';
import styled from 'styled-components';
import { Typography } from 'antd';
import { LogicalPredicateBuilder } from '../definition/builder/LogicalPredicateBuilder';
import { LogicalPredicate } from '../definition/builder/types';
import { serializeTestDefinition, deserializeTestDefinition } from '../definition/utils';
import { ValidateTestModal } from '../validate/ValidateTestModal';
import { YamlStep } from '../definition/yaml/YamlStep';
import {
    convertLogicalPredicateToTestPredicate,
    convertTestPredicateToLogicalPredicate,
} from '../definition/builder/utils';
import { StepProps, TestBuilderStep } from '../../types';
import { getPropertiesForEntityTypes } from '../definition/builder/property/utils';
import { graphNamesToEntityTypes } from '../select/utils';
import { useEntityRegistry } from '../../../../useEntityRegistry';

const BuilderWrapper = styled.div`
    margin-bottom: 28px;
`;

export const RulesStep = ({ state, updateState, prev, goTo }: StepProps) => {
    const entityRegistry = useEntityRegistry();
    const testDefinition = useMemo(() => deserializeTestDefinition(state?.definition?.json || '{}'), [state]);

    const [showTestModal, setShowTestModal] = useState(false);

    const onClickNext = () => {
        goTo(TestBuilderStep.NAME);
    };

    const onClickTest = () => {
        setShowTestModal(true);
    };

    const onChangePredicate = (newPredicate) => {
        const newDefinition = {
            ...testDefinition,
            rules: convertLogicalPredicateToTestPredicate(newPredicate),
        };
        const newState = {
            ...state,
            definition: {
                json: serializeTestDefinition(newDefinition),
            },
        };
        updateState(newState);
    };

    const selectedEntityTypes = graphNamesToEntityTypes(testDefinition.on?.types || [], entityRegistry);

    return (
        <>
            <YamlStep
                state={state}
                updateState={updateState}
                onNext={onClickNext}
                onPrev={prev}
                actionTitle="Try it out"
                onAction={onClickTest}
            >
                <Typography.Title level={4}>Rules</Typography.Title>
                <Typography.Paragraph type="secondary">
                    Define the rules that each selected asset must pass in order to pass the test. If you do not provide
                    rules, then all selected entities will pass the test.
                </Typography.Paragraph>
                <BuilderWrapper>
                    <LogicalPredicateBuilder
                        selectedPredicate={
                            convertTestPredicateToLogicalPredicate(testDefinition.rules) as LogicalPredicate
                        }
                        onChangePredicate={onChangePredicate}
                        properties={getPropertiesForEntityTypes(selectedEntityTypes)}
                        options={{
                            predicateDisplayName: 'rule',
                        }}
                    />
                </BuilderWrapper>
            </YamlStep>
            {showTestModal && <ValidateTestModal state={state} onClose={() => setShowTestModal(false)} />}
        </>
    );
};
