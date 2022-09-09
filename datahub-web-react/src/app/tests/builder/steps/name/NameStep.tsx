import React from 'react';
import styled from 'styled-components';
import { Button, Form, Input, Typography } from 'antd';
import { StepProps } from '../../types';

const StyledForm = styled(Form)`
    max-width: 400px;
`;

const ControlsContainer = styled.div`
    display: flex;
    justify-content: space-between;
    margin-top: 8px;
`;

const SaveButton = styled(Button)`
    margin-right: 15px;
`;

export const NameStep = ({ state, updateState, prev, submit }: StepProps) => {
    const setName = (name: string) => {
        updateState({
            ...state,
            name,
        });
    };

    const setCategory = (category: string) => {
        updateState({
            ...state,
            category,
        });
    };

    const setDescription = (description: string) => {
        updateState({
            ...state,
            description,
        });
    };

    const onClickCreate = () => {
        if (state.name !== undefined && state.name.length > 0) {
            submit();
        }
    };

    return (
        <>
            <StyledForm layout="vertical">
                <Form.Item required label={<Typography.Text strong>Name</Typography.Text>}>
                    <Typography.Paragraph>Give your test a name.</Typography.Paragraph>
                    <Input
                        placeholder="A name for your test"
                        value={state.name}
                        onChange={(event) => setName(event.target.value)}
                    />
                </Form.Item>
                <Form.Item required label={<Typography.Text strong>Category</Typography.Text>}>
                    <Typography.Paragraph>The category of your test.</Typography.Paragraph>
                    <Input
                        placeholder="The category of your test"
                        value={state.category}
                        onChange={(event) => setCategory(event.target.value)}
                    />
                </Form.Item>
                <Form.Item label={<Typography.Text strong>Description</Typography.Text>}>
                    <Typography.Paragraph>
                        An optional description to help keep track of your test.
                    </Typography.Paragraph>
                    <Input
                        placeholder="The description for your test"
                        value={state.description || undefined}
                        onChange={(event) => setDescription(event.target.value)}
                    />
                </Form.Item>
            </StyledForm>
            <ControlsContainer>
                <Button onClick={prev}>Previous</Button>
                <SaveButton
                    disabled={!(state.name !== undefined && state.name.length > 0)}
                    onClick={() => onClickCreate()}
                >
                    Save
                </SaveButton>
            </ControlsContainer>
        </>
    );
};
