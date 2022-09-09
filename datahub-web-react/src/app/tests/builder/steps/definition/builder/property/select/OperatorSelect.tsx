import React from 'react';
import styled from 'styled-components';
import { Select, Tooltip, Typography } from 'antd';
import { Operator } from '../types/operators';

const StyledSelect = styled(Select)`
    width: 120px;
    margin-right: 12px;
`;

type Props = {
    selectedOperator?: string;
    operators: Operator[];
    onChangeOperator: (newOperatorId: string) => void;
};

/**
 * A single node in a menu tree. This node can have children corresponding
 * to properties that should appear nested inside of it.
 */
export const OperatorSelect = ({ selectedOperator, operators, onChangeOperator }: Props) => {
    return (
        <StyledSelect
            defaultActiveFirstOption={false}
            placeholder="Select an operator..."
            onSelect={(newVal) => onChangeOperator(newVal as string)}
            value={selectedOperator}
        >
            {operators?.map((operator) => {
                return (
                    <Select.Option value={operator.id} key={operator.id}>
                        <Tooltip title={operator.description} placement="right">
                            <Typography.Text>{operator.displayName}</Typography.Text>
                        </Tooltip>
                    </Select.Option>
                );
            })}
        </StyledSelect>
    );
};
