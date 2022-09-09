import { CheckCircleFilled, CloseCircleFilled } from '@ant-design/icons';
import { Button, Select, Typography } from 'antd';
import React, { useState } from 'react';
import styled from 'styled-components';
import { ANTD_GRAY } from '../../../../entity/shared/constants';
import { FAILURE_COLOR_HEX, SUCCESS_COLOR_HEX } from '../../../../entity/shared/tabs/Incident/incidentUtils';
import { StepProps, TestBuilderStep } from '../../types';
import { ActionType, ResultActions } from '../definition/builder/property/types/action';

const ControlsContainer = styled.div`
    display: flex;
    justify-content: space-between;
    margin-top: 8px;
`;

const ActionSection = styled.div`
    margin-bottom: 20px;
`;

const ActionSectionTitle = styled.div`
    margin-top: 8px;
    margin-bottom: 4px;
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
    margin: 0px;
    padding: 0px;
    margin-left: 8px;
`;

const ActionSelect = styled.div`
    margin-bottom: 8px;
    margin-top: 12px;
    margin-right: 12px;
`;

// This step is NOT yet supported and is still under active development.
export const ActionsStep = ({ state, updateState, prev, goTo }: StepProps) => {
    const [actions, setActions] = useState<ResultActions>();

    const onAddSuccessAction = () => {
        const newSuccessActions = [
            ...(actions?.onSuccess || []),
            {
                id: ActionType.ADD_TAGS,
            },
        ];
        const newActions = {
            onFailure: actions?.onFailure || [],
            onSuccess: newSuccessActions,
        };
        setActions(newActions);
    };

    const onAddFailureAction = () => {
        const newFailureActions = [
            ...(actions?.onFailure || []),
            {
                id: ActionType.REMOVE_TAGS,
            },
        ];
        const newActions = {
            onSuccess: actions?.onSuccess || [],
            onFailure: newFailureActions,
        };
        setActions(newActions);
    };

    const onClickNext = () => {
        const newState = {
            ...state,
            actions,
        };
        updateState(newState);
        goTo(TestBuilderStep.NAME);
    };

    return (
        <>
            <Typography.Title level={4}>Actions</Typography.Title>
            <Typography.Paragraph type="secondary">
                Define a set of actions to run against the entities that fail or succeed the test.
            </Typography.Paragraph>
            <ActionsContainer>
                <ActionSection>
                    <ActionSectionTitle>
                        <SuccessIcon />
                        <StatusTitle level={5}>On success</StatusTitle>
                    </ActionSectionTitle>
                    <Button type="text" onClick={onAddSuccessAction}>
                        + Add action
                    </Button>
                </ActionSection>
                <ActionSection>
                    <ActionSectionTitle>
                        <FailureIcon />
                        <StatusTitle level={5}>On failure</StatusTitle>
                    </ActionSectionTitle>
                    <ActionSelect>
                        {actions?.onFailure &&
                            actions.onFailure.map((action) => (
                                <ActionSelect>
                                    <Select style={{ width: 200 }} value={action.id}>
                                        {Object.keys(ActionType).map((key) => (
                                            <Select.Option value={ActionType[key]}>{ActionType[key]}</Select.Option>
                                        ))}
                                    </Select>
                                </ActionSelect>
                            ))}
                        <Button type="text" onClick={onAddFailureAction}>
                            + Add action
                        </Button>
                    </ActionSelect>
                </ActionSection>
            </ActionsContainer>
            <ControlsContainer>
                <Button onClick={prev}>Previous</Button>
                <Button onClick={onClickNext}>Next</Button>
            </ControlsContainer>
        </>
    );
};
