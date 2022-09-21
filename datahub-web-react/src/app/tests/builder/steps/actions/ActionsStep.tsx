import { CheckCircleFilled, CloseCircleFilled } from '@ant-design/icons';
import { Typography } from 'antd';
import React, { useMemo } from 'react';
import styled from 'styled-components';
import { ANTD_GRAY } from '../../../../entity/shared/constants';
import { FAILURE_COLOR_HEX, SUCCESS_COLOR_HEX } from '../../../../entity/shared/tabs/Incident/incidentUtils';
import { StepProps, TestBuilderStep } from '../../types';
import { ACTION_TYPES } from '../definition/builder/property/types/action';
import { deserializeTestDefinition, serializeTestDefinition } from '../definition/utils';
import { YamlStep } from '../definition/yaml/YamlStep';
import { ActionsBuilder } from '../definition/builder/action/ActionsBuilder';
import { Action } from './types';

const ActionSection = styled.div`
    margin-bottom: 20px;
`;

const ActionSectionTitle = styled.div`
    margin-top: 8px;
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    justify-content: left;
`;

const ActionsContainer = styled.div`
    background-color: ${ANTD_GRAY[3]};
    border-radius: 4px;
    padding: 20px;
    padding-top: 8px;
    border: 0.5px solid ${ANTD_GRAY[6]};
    margin-top: 16px;
    margin-bottom: 20px;
`;

const FailureIcon = styled(CloseCircleFilled)`
    color: ${FAILURE_COLOR_HEX};
    font-size: 18px;
`;

const SuccessIcon = styled(CheckCircleFilled)`
    color: ${SUCCESS_COLOR_HEX};
    font-size: 18px;
`;

const StatusTitle = styled(Typography.Title)`
    && {
        margin: 0px;
        padding: 0px;
        margin-left: 8px;
    }
`;

const ActionSelect = styled.div`
    margin-bottom: 8px;
    margin-top: 12px;
    margin-right: 12px;
`;

export const ActionsStep = ({ state, updateState, prev, goTo }: StepProps) => {
    const testDefinition = useMemo(() => deserializeTestDefinition(state?.definition?.json || '{}'), [state]);

    const selectedPassingActions = testDefinition.actions?.passing || [];
    const selectedFailingActions = testDefinition.actions?.failing || [];

    const onSetPassingActions = (newActions: Action[]) => {
        const newDefinition = {
            ...testDefinition,
            actions: {
                passing: newActions,
                failing: testDefinition.actions?.failing || [],
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

    const onSetFailingActions = (newActions: Action[]) => {
        const newDefinition = {
            ...testDefinition,
            actions: {
                passing: testDefinition.actions?.passing || [],
                failing: newActions,
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

    const onClickNext = () => {
        goTo(TestBuilderStep.NAME);
    };

    return (
        <>
            <YamlStep state={state} updateState={updateState} onNext={onClickNext} onPrev={prev}>
                <Typography.Title level={4}>Actions</Typography.Title>
                <Typography.Paragraph type="secondary">
                    Define a set of actions to run against the entities that fail or succeed the test.
                </Typography.Paragraph>
                <ActionsContainer>
                    <ActionSection>
                        <ActionSectionTitle>
                            <SuccessIcon />
                            <StatusTitle level={5}>On Passing</StatusTitle>
                        </ActionSectionTitle>
                        <ActionsBuilder
                            actionTypes={ACTION_TYPES}
                            selectedActions={selectedPassingActions}
                            onChangeActions={onSetPassingActions}
                        />
                    </ActionSection>
                    <ActionSection>
                        <ActionSectionTitle>
                            <FailureIcon />
                            <StatusTitle level={5}>On Failing</StatusTitle>
                        </ActionSectionTitle>
                        <ActionSelect>
                            <ActionsBuilder
                                actionTypes={ACTION_TYPES}
                                selectedActions={selectedFailingActions}
                                onChangeActions={onSetFailingActions}
                            />
                        </ActionSelect>
                    </ActionSection>
                </ActionsContainer>
            </YamlStep>
        </>
    );
};
