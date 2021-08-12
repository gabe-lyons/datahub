import React from 'react';
import { Space, Table, Typography } from 'antd';
import { ColumnsType } from 'antd/es/table';

import { TabProps } from '../../types';
import { StringMapEntry } from '../../../../../types.generated';

export const PropertiesTab = ({ entityData }: TabProps) => {
    const propertyTableColumns: ColumnsType<StringMapEntry> = [
        {
            title: 'Name',
            dataIndex: 'key',
            sorter: (a, b) => a?.key.localeCompare(b?.key || '') || 0,
            defaultSortOrder: 'ascend',
        },
        {
            title: 'Value',
            dataIndex: 'value',
        },
    ];

    return (
        <Space direction="vertical" style={{ width: '100%' }} size="large">
            <Typography.Title level={3}>Properties</Typography.Title>
            <Table pagination={false} columns={propertyTableColumns} dataSource={entityData?.properties || undefined} />
        </Space>
    );
};
