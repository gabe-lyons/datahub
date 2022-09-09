import React from 'react';
import styled from 'styled-components';
import { Button } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
/* eslint-disable import/no-cycle */
import { LogicalPredicateBuilder } from './LogicalPredicateBuilder';
import { ANTD_GRAY } from '../../../../../entity/shared/constants';
import { PropertyPredicateBuilder } from './property/PropertyPredicateBuilder';
import { LogicalPredicate, PropertyPredicate } from './types';
import { isLogicalPredicate } from './utils';
import { Property } from './property/types/properties';

const PredicateContainer = styled.div`
    display: flex;
    align-items: center;
    justify-content: left;
    margin-top: 12px;
    margin-bottom: 12px;
    margin-right: 8px;
`;

const DeleteButton = styled(Button)`
    margin: 0px;
    padding: 0px;
    margin-left: 8px;
`;

const DeleteIcon = styled(DeleteOutlined)`
    font-size: 16px;
    color: ${ANTD_GRAY[6]};
`;

type Props = {
    operand: LogicalPredicate | PropertyPredicate;
    onChange: (newOperand: LogicalPredicate | PropertyPredicate) => void;
    onDelete: () => void;
    properties: Property[];
    options?: any;
};

export const LogicalOperatorOperand = ({ operand, onChange, onDelete, properties, options }: Props) => {
    /**
     * Whether this operand is a logical operand (and / or / not), or a property
     * operand (prop, op, vals)
     */
    const isLogPred = isLogicalPredicate(operand);

    const predicateBuilder = isLogPred ? (
        <LogicalPredicateBuilder
            selectedPredicate={operand as LogicalPredicate}
            onChangePredicate={onChange}
            properties={properties}
            options={options}
        />
    ) : (
        <PropertyPredicateBuilder
            selectedPredicate={operand as PropertyPredicate}
            onChangePredicate={onChange}
            properties={properties}
        />
    );

    return (
        <PredicateContainer>
            {predicateBuilder}
            <DeleteButton type="text" onClick={onDelete}>
                <DeleteIcon />
            </DeleteButton>
        </PredicateContainer>
    );
};
