import React from 'react';
import styled from 'styled-components';
import { Dropdown, Menu, Tag } from 'antd';
import { LogicalOperatorType } from './types';
import { getOperatorDisplayName } from './utils';

const LogicalTypeTagContainer = styled.div`
    margin-right: 8px;
`;

type Props = {
    operator: LogicalOperatorType;
    onSelectOperator: (operator) => void;
};

export const LogicalOperatorDropdown = ({ operator, onSelectOperator }: Props) => {
    const operatorName = getOperatorDisplayName(operator);

    const menu = (
        <Menu onClick={(e) => onSelectOperator(e.key as LogicalOperatorType)}>
            <Menu.Item key={LogicalOperatorType.AND}>And</Menu.Item>
            <Menu.Item key={LogicalOperatorType.OR}>Or</Menu.Item>
            <Menu.Item key={LogicalOperatorType.NOT}>Not</Menu.Item>
        </Menu>
    );

    return (
        <LogicalTypeTagContainer>
            <Dropdown overlay={menu}>
                <Tag>
                    <b>{operatorName}</b>
                </Tag>
            </Dropdown>
        </LogicalTypeTagContainer>
    );
};
