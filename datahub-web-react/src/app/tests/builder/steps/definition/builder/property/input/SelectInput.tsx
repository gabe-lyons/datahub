import React from 'react';
import { Select } from 'antd';
import { SelectOption } from '../types/values';

type Props = {
    options: SelectOption[];
    selected?: string | string[];
    mode?: 'multiple' | 'tags';
    placeholder?: string;
    style?: any;
    onChangeSelected: (newSelectedIds: string | string[] | undefined) => void;
};

export const SelectInput = ({ options, selected, mode, placeholder, style, onChangeSelected }: Props) => {
    const onSelect = (id) => {
        if (Array.isArray(selected)) {
            const newSelected = [...selected, id];
            onChangeSelected(newSelected);
        } else {
            onChangeSelected(id);
        }
    };

    const onDeselect = (id) => {
        if (Array.isArray(selected)) {
            onChangeSelected(selected.filter((item) => item !== id));
        } else {
            onChangeSelected(undefined);
        }
    };

    return (
        <Select
            style={style}
            value={selected}
            mode={mode}
            placeholder={placeholder || 'Select values...'}
            onSelect={onSelect}
            onDeselect={onDeselect}
        >
            {options.map((option) => (
                <Select.Option value={option.id}>{option.displayName}</Select.Option>
            ))}
        </Select>
    );
};
