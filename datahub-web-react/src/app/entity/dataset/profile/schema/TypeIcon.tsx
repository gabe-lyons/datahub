import {
    FieldBinaryOutlined,
    NumberOutlined,
    UnorderedListOutlined,
    QuestionCircleOutlined,
    UnderlineOutlined,
    CalendarOutlined,
    FieldTimeOutlined,
} from '@ant-design/icons';
import { Typography } from 'antd';
import React, { FC } from 'react';
import { VscSymbolString, VscFileBinary } from 'react-icons/vsc';
import styled from 'styled-components';
import { SchemaFieldDataType } from '../../../../../types.generated';

const TypeIconContainer = styled.div`
    display: flex;
    flex-direction: column;
    justify-content: center;
    text-align: center;
    margin-top: 2.5px;
    width: 40px;
`;

const TypeSubtitle = styled(Typography.Text)`
    font-size: 8px;
    text-align: center;
`;

const IconSpan = styled.span`
    font-size: 18px;
`;

const DATA_TYPE_ICON_MAP: Record<
    SchemaFieldDataType,
    { icon: FC<{ style: any }> | null; size: number; text: string }
> = {
    [SchemaFieldDataType.Boolean]: {
        icon: FieldBinaryOutlined,
        size: 18,
        text: 'Boolean',
    },
    [SchemaFieldDataType.Fixed]: { icon: FieldBinaryOutlined, size: 18, text: 'Fixed' },
    [SchemaFieldDataType.String]: {
        icon: () => (
            <IconSpan role="img" aria-label="calendar" className="anticon anticon-calendar">
                <VscSymbolString />
            </IconSpan>
        ),
        size: 20,
        text: 'String',
    },
    [SchemaFieldDataType.Bytes]: {
        icon: () => (
            <IconSpan role="img" aria-label="calendar" className="anticon anticon-calendar">
                <VscFileBinary />
            </IconSpan>
        ),
        size: 18,
        text: 'Bytes',
    },
    [SchemaFieldDataType.Number]: { icon: NumberOutlined, size: 14, text: 'Number' },
    [SchemaFieldDataType.Date]: { icon: CalendarOutlined, size: 18, text: 'Date' },
    [SchemaFieldDataType.Time]: { icon: FieldTimeOutlined, size: 18, text: 'Time' },
    [SchemaFieldDataType.Enum]: { icon: UnorderedListOutlined, size: 18, text: 'Enum' },
    [SchemaFieldDataType.Null]: { icon: QuestionCircleOutlined, size: 16, text: '' },
    [SchemaFieldDataType.Map]: { icon: null, size: 0, text: 'Map' },
    [SchemaFieldDataType.Array]: { icon: UnorderedListOutlined, size: 14, text: 'Array' },
    [SchemaFieldDataType.Union]: { icon: UnderlineOutlined, size: 14, text: 'Union' },
    [SchemaFieldDataType.Struct]: { icon: null, size: 0, text: 'Struct' },
};

type Props = {
    type: SchemaFieldDataType;
};

export default function TypeIcon({ type }: Props) {
    const { icon: Icon, size, text } = DATA_TYPE_ICON_MAP[type];
    return (
        <TypeIconContainer data-testid={`icon-${type}`}>
            {Icon && <Icon style={{ fontSize: size }} />}
            <TypeSubtitle type="secondary">{text}</TypeSubtitle>
        </TypeIconContainer>
    );
}
