import React from 'react';
import styled from 'styled-components';
import { Dropdown, Menu } from 'antd';
import { LogicalOperatorType } from './types';
import { ANTD_GRAY } from '../../../../../entity/shared/constants';

const DropdownWrapper = styled.div<{
    disabled: boolean;
}>`
    cursor: ${(props) => (props.disabled ? 'normal' : 'pointer')};
    color: ${(props) => (props.disabled ? ANTD_GRAY[7] : 'none')};
    display: flex;
    margin-left: 4px;
    margin-right: 12px;
`;

type Props = {
    disabled?: boolean;
    onAddPropertyPredicate: () => void;
    onAddLogicalPredicate: (operator: LogicalOperatorType) => void;
    options?: {
        predicateDisplayName?: string;
    };
};

export const AddPredicateButton = ({
    disabled = false,
    options = { predicateDisplayName: 'predicate' },
    onAddPropertyPredicate,
    onAddLogicalPredicate,
}: Props) => {
    return (
        <Dropdown
            disabled={disabled}
            trigger={['click']}
            overlay={
                <Menu>
                    <Menu.Item onClick={onAddPropertyPredicate}>
                        Property {options?.predicateDisplayName}
                        ...
                    </Menu.Item>
                    <Menu.Item onClick={() => onAddLogicalPredicate(LogicalOperatorType.AND)}>
                        And {options?.predicateDisplayName}...
                    </Menu.Item>
                    <Menu.Item onClick={() => onAddLogicalPredicate(LogicalOperatorType.OR)}>
                        Or {options?.predicateDisplayName}...
                    </Menu.Item>
                    <Menu.Item onClick={() => onAddLogicalPredicate(LogicalOperatorType.NOT)}>
                        Not {options?.predicateDisplayName}...
                    </Menu.Item>
                </Menu>
            }
        >
            <DropdownWrapper disabled={disabled}>+ Add {options?.predicateDisplayName}</DropdownWrapper>
        </Dropdown>
    );
};
