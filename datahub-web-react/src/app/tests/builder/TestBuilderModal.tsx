import React, { useState } from 'react';
import styled from 'styled-components';
import { Button, Modal, Steps, Typography } from 'antd';
import { ExpandAltOutlined, ShrinkOutlined } from '@ant-design/icons';
import ClickOutside from '../../shared/ClickOutside';
import { TestBuilderStepComponent, TestBuilderStepTitles } from './conf';
import { DEFAULT_BUILDER_STATE, StepProps, TestBuilderState, TestBuilderStep } from './types';

const modalStyle = { top: 40 };
const modalBodyStyle = { paddingRight: 60, paddingLeft: 60, paddingBottom: 40 };

const ExpandButton = styled(Button)`
    && {
        margin-right: 32px;
    }
`;

const TitleContainer = styled.div`
    display: flex;
    justify-content: space-between;
`;

const StepsContainer = styled.div`
    margin-right: 20px;
    margin-left: 20px;
    margin-bottom: 40px;
`;

const stepIds = Object.values(TestBuilderStep);
const stepIndexToId = new Map();
stepIds.forEach((stepId, index) => stepIndexToId.set(stepId, index));

type Props = {
    initialState?: TestBuilderState;
    onSubmit?: (input: TestBuilderState) => void;
    onCancel?: () => void;
};

export const TestBuilderModal = ({ initialState, onSubmit, onCancel }: Props) => {
    const isEditing = initialState !== undefined;
    const titleText = isEditing ? 'Edit Metadata Test' : 'New Metadata Test';

    const [stepStack, setStepStack] = useState<TestBuilderStep[]>([TestBuilderStep.SELECT]);
    const [modalExpanded, setModalExpanded] = useState(false);
    const [builderState, setBuilderState] = useState<TestBuilderState>(initialState || DEFAULT_BUILDER_STATE);

    const goTo = (step: TestBuilderStep) => {
        setStepStack([...stepStack, step]);
    };

    const prev = () => {
        setStepStack(stepStack.slice(0, -1));
    };

    const cancel = () => {
        onCancel?.();
    };

    const submit = () => {
        onSubmit?.(builderState);
    };

    const modalClosePopup = () => {
        Modal.confirm({
            title: 'Exit Test Editor',
            content: `Are you sure you want to exit test editor? All changes will be lost`,
            onOk() {
                onCancel?.();
            },
            onCancel() {},
            okText: 'Yes',
            maskClosable: true,
            closable: true,
        });
    };

    /**
     * The current step id, e.g. SELECT
     */
    const currentStep = stepStack[stepStack.length - 1];
    /**
     * The current step index, e.g. 0
     */
    const currentStepIndex = stepIndexToId.get(currentStep);
    /**
     * The current step component
     */
    const StepComponent: React.FC<StepProps> = TestBuilderStepComponent[currentStep];

    return (
        <ClickOutside onClickOutside={modalClosePopup} wrapperClassName="test-builder-modal">
            <Modal
                wrapClassName="test-builder-modal"
                width={modalExpanded ? 1400 : 1000}
                footer={null}
                title={
                    <TitleContainer>
                        <Typography.Text>{titleText}</Typography.Text>
                        <ExpandButton onClick={() => setModalExpanded(!modalExpanded)}>
                            {(modalExpanded && <ShrinkOutlined />) || <ExpandAltOutlined />}
                        </ExpandButton>
                    </TitleContainer>
                }
                style={modalStyle}
                bodyStyle={modalBodyStyle}
                visible
                onCancel={onCancel}
            >
                <StepsContainer>
                    <Steps current={currentStepIndex}>
                        {stepIds.map((id) => (
                            <Steps.Step key={id} title={TestBuilderStepTitles[id]} />
                        ))}
                    </Steps>
                </StepsContainer>
                <StepComponent
                    state={builderState}
                    updateState={setBuilderState}
                    goTo={goTo}
                    prev={stepStack.length > 1 ? prev : undefined}
                    submit={submit}
                    cancel={cancel}
                />
            </Modal>
        </ClickOutside>
    );
};
