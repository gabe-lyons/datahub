import React from 'react';
import styled from 'styled-components';
import { EditOutlined } from '@ant-design/icons';
import { Button } from 'antd';

const EditTestActionButton = styled(Button)`
    && {
        margin: 0px;
        padding: 0px 6px 0px 4px;
    }
`;

export type Props = {
    onClickEdit?: () => void;
    index?: number;
};

export default function TestCardEditButton({ onClickEdit, index }: Props) {
    return (
        <EditTestActionButton type="text" onClick={onClickEdit} data-testid={`test-edit-button-${index}`}>
            <EditOutlined />
        </EditTestActionButton>
    );
}
