import React from 'react';
import styled from 'styled-components';
import { groupTestsByCategory } from './utils';
import { TestsSection } from './TestsSection';
import { Test } from '../../types.generated';
import { ANTD_GRAY } from '../entity/shared/constants';
import EmptyTests from './EmptyTests';

const Container = styled.div`
    padding-top 28px;
    background-color: ${ANTD_GRAY[3]};
    min-height: 100vh;
`;

type Props = {
    tests: Test[];
};

export const Tests = ({ tests }: Props) => {
    const hasTests = tests.length > 0;
    const testSections = groupTestsByCategory(tests);
    return (
        <Container>
            {(hasTests &&
                testSections.map((section) => {
                    return (
                        <TestsSection
                            title={section.name}
                            tooltip={section.description}
                            tests={section.tests}
                            key={section.name || ''}
                        />
                    );
                })) || <EmptyTests />}
        </Container>
    );
};
