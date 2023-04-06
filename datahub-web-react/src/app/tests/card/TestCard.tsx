import React from 'react';
import styled from 'styled-components';
import { Card } from 'antd';
import { TestCardDetails } from './TestCardDetails';
import { TestCardResults } from './TestCardResults';
import { Test } from '../../../types.generated';

const StyledCard = styled(Card)`
    margin: 8px 12px 12px 12px;
    box-shadow: ${(props) => props.theme.styles['box-shadow']};
`;

type Props = {
    test: Test;
    onEdited?: (newTest) => void;
    onDeleted?: () => void;
    index: number;
};

export const TestCard = ({ test, onEdited, onDeleted, index }: Props) => {
    return (
        <StyledCard title={<TestCardDetails test={test} index={index} onEdited={onEdited} onDeleted={onDeleted} />}>
            <TestCardResults testUrn={test.urn} name={test.name} />
        </StyledCard>
    );
};
