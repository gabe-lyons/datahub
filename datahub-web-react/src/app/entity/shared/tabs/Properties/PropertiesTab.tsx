import React from 'react';
import { Table, Typography } from 'antd';
import styled from 'styled-components';

import { TabProps } from '../../types';
import { ANTD_GRAY } from '../../constants';

const PropertiesTable = styled(Table)`
    &&& .ant-table-cell {
        background-color: #fff;
    }
    &&& .ant-table-thead .ant-table-cell {
        font-weight: 600;
        font-size: 12px;
        color: ${ANTD_GRAY[8]};
    }
    &&
        .ant-table-thead
        > tr
        > th:not(:last-child):not(.ant-table-selection-column):not(.ant-table-row-expand-icon-cell):not([colspan])::before {
        border: 1px solid ${ANTD_GRAY[4]};
    }
`;

const NameText = styled(Typography.Text)`
    font-family: 'Roboto Mono';
    font-weight: 600;
    font-size: 12px;
    color: ${ANTD_GRAY[9]};
`;

const ValueText = styled(Typography.Text)`
    font-family: 'Roboto Mono';
    font-weight: 400;
    font-size: 12px;
    color: ${ANTD_GRAY[8]};
`;

export const PropertiesTab = ({ entityData }: TabProps) => {
    const propertyTableColumns = [
        {
            width: 210,
            title: 'Name',
            dataIndex: 'key',
            sorter: (a, b) => a?.key.localeCompare(b?.key || '') || 0,
            defaultSortOrder: 'ascend',
            render: (name: string) => <NameText>{name}</NameText>,
        },
        {
            title: 'Value',
            dataIndex: 'value',
            render: (value: string) => <ValueText>{value}</ValueText>,
        },
    ];

    return (
        <PropertiesTable
            pagination={false}
            // typescript is complaining that default sort order is not a valid column field- overriding this here
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            columns={propertyTableColumns}
            dataSource={entityData?.properties || undefined}
        />
    );
};
