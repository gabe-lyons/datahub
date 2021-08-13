import { Typography } from 'antd';
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

const AboutText = styled(Typography.Text)`
    font-weight: 500;
    font-size: 12px;
    line-height: 20x;
    color: ${ANTD_GRAY[8]};
`;

export const SidebarAboutSection = ({ urn, entityData, entityType }: Props) => {
    console.log({ urn, entityType });
    return (
        <div>
            <HeaderText>About</HeaderText>
            <AboutText>{entityData?.description}</AboutText>
        </div>
    );
};
