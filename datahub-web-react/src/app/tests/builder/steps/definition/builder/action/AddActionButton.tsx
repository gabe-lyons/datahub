import React from 'react';
import { Button } from 'antd';
import styled from 'styled-components';

const StyledButton = styled(Button)`
    && {
        padding: 0px;
        margin: 0px;
    }
`;

type Props = {
    disabled?: boolean;
    onAddAction: () => void;
};

export const AddActionButton = ({ disabled, onAddAction }: Props) => {
    return (
        <StyledButton type="text" disabled={disabled} onClick={onAddAction}>
            + Add action
        </StyledButton>
    );
};
