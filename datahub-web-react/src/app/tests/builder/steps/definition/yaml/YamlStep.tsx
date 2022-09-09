import React, { useEffect, useState } from 'react';
import styled from 'styled-components';
import { CodeOutlined, FormOutlined } from '@ant-design/icons';
import { Button, message } from 'antd';
import { jsonToYaml, yamlToJson } from '../../../../../ingest/source/utils';
import { ANTD_GRAY } from '../../../../../entity/shared/constants';
import { YamlBuilder } from './YamlBuilder';
import { validateJsonDefinition } from '../utils';
import { TestBuilderState } from '../../../types';

const ControlsContainer = styled.div`
    display: flex;
    justify-content: space-between;
    margin-top: 8px;
`;

const StyledButton = styled(Button)<{ isSelected: boolean }>`
    ${(props) =>
        (props.isSelected &&
            `
        color: ${ANTD_GRAY[9]};
        &:focus {
            color: ${ANTD_GRAY[9]};
        }    
    `) ||
        `color: ${ANTD_GRAY[7]};
`}
`;

const ToggleViewButtonWrapper = styled.div`
    display: flex;
    justify-content: flex-end;
    margin-bottom: 10px;
`;

const ActionButton = styled(Button)`
    margin-right: 8px;
`;

type YamlStepProps = {
    children: React.ReactNode;
    state: TestBuilderState;
    updateState: (newState: TestBuilderState) => void;
    onNext: () => void;
    onPrev?: () => void;
    actionTitle?: string;
    onAction?: () => void;
};

export const YamlStep = ({ children, state, updateState, onNext, onPrev, actionTitle, onAction }: YamlStepProps) => {
    const [showYamlEditor, setShowYamlEditor] = useState(false);
    const [stagedYaml, setStagedYaml] = useState(state?.definition?.json);

    useEffect(() => {
        setStagedYaml(state?.definition?.json);
    }, [state?.definition?.json]);

    const updateTestDefinition = (yaml: string): boolean => {
        let json;
        try {
            json = yamlToJson(yaml);
        } catch (e) {
            message.error(`Failed to validate test definition. Failed to parse test YAML.`);
            return false;
        }
        const validationResult = validateJsonDefinition(json);
        if (validationResult.isValid) {
            updateState({
                ...state,
                definition: {
                    json,
                },
            });
            return true;
        }
        message.error(`Failed to validate test definition: ${validationResult.message}`);
        return false;
    };

    const updateStagedYaml = (newYaml: string) => {
        setStagedYaml(newYaml);
    };

    const onChangeView = (isEditorView: boolean) => {
        const res = updateTestDefinition(stagedYaml || '');
        if (res) {
            setShowYamlEditor(isEditorView);
        }
    };

    const handleOnAction = () => {
        const res = updateTestDefinition(stagedYaml || '');
        if (res) {
            onAction?.();
        }
    };

    const handleOnNext = () => {
        const res = updateTestDefinition(stagedYaml || '');
        if (res) {
            onNext();
        }
    };

    return (
        <>
            <ToggleViewButtonWrapper>
                <StyledButton type="text" isSelected={!showYamlEditor} onClick={() => onChangeView(false)}>
                    <FormOutlined /> Form
                </StyledButton>
                <StyledButton type="text" isSelected={showYamlEditor} onClick={() => onChangeView(true)}>
                    <CodeOutlined /> YAML
                </StyledButton>
            </ToggleViewButtonWrapper>
            {(showYamlEditor && (
                <YamlBuilder initialValue={jsonToYaml(state?.definition?.json || '{}')} onChange={updateStagedYaml} />
            )) ||
                children}
            <ControlsContainer>
                {(onPrev && <Button onClick={onPrev}>Previous</Button>) || <div> </div>}
                <div>
                    {onAction && (
                        <ActionButton onClick={handleOnAction} type="primary">
                            {actionTitle}
                        </ActionButton>
                    )}
                    <Button onClick={handleOnNext}>Next</Button>
                </div>
            </ControlsContainer>
        </>
    );
};
