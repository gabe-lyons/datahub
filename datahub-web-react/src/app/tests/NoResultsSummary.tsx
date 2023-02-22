import React from 'react';
import { Popover, Tag, Typography } from 'antd';
import styled from 'styled-components';
import { InfoCircleOutlined } from '@ant-design/icons';
import { ANTD_GRAY } from '../entity/shared/constants';

const Container = styled.span`
    max-width: 50px;
`;

const Paragraph = styled.div`
    margin-top: 8px;
    color: ${ANTD_GRAY[8]};
`;

const StyledTag = styled(Tag)`
    font-size: 12px;
    color: ${ANTD_GRAY[8]};
    padding-right: 8px;
    padding-left: 8px;
`;

const StyledInfo = styled(InfoCircleOutlined)`
    margin-left: 4px;
    font-size: 12px;
`;

export const NoResultsSummary = () => {
    return (
        <Popover
            overlayStyle={{ maxWidth: 240 }}
            content={
                <Container>
                    <Typography.Text strong>No results found for this test (yet).</Typography.Text>
                    <Paragraph>Once created, tests can take up to 24 hours to run for the first time.</Paragraph>
                </Container>
            }
        >
            <StyledTag color={ANTD_GRAY[3]}>
                No results
                <StyledInfo />
            </StyledTag>
        </Popover>
    );
};
