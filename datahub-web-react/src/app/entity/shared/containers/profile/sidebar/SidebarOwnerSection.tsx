import { Typography, Tag } from 'antd';
import React from 'react';
import styled from 'styled-components';

import { EntityType } from '../../../../../../types.generated';
import { ANTD_GRAY } from '../../../constants';
import { GenericEntityProperties } from '../../../types';

type Props = {
    urn: string;
    entityType: EntityType;
    entityData: GenericEntityProperties | null;
};

const HeaderText = styled(Typography.Text)`
    font-weight: 700;
    font-size: 14px;
    line-height: 22px;
    color: ${ANTD_GRAY[9]};
`;

export const SidebarOwnerSection = ({ urn, entityData, entityType }: Props) => {
    console.log({ urn, entityType });
    return (
        <div>
            <HeaderText>Owners</HeaderText>
            <div>
                {entityData?.ownership?.owners?.map((owner) => (
                    <Tag color="blue">{owner.owner.urn}</Tag>
                ))}
            </div>
        </div>
    );
};
