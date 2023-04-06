import React, { useState } from 'react';
import styled from 'styled-components';
import { Button, Tag, Typography } from 'antd';
import { SUCCESS_COLOR_HEX } from '../entity/shared/tabs/Incident/incidentUtils';
import { useGetTestResultsSummaryQuery } from '../../graphql/test.generated';
import { formatNumberWithoutAbbreviation } from '../shared/formatNumber';
import { NoResultsSummary } from './NoResultsSummary';
import { ANTD_GRAY } from '../entity/shared/constants';
import { PLACEHOLDER_TEST_URN } from './constants';
import TestResultsModal from './TestResultsModal';
import { TestResultType } from '../../types.generated';

const Container = styled.div`
    padding: 4px;
    height: 80px;
`;

const StyledButton = styled(Button)`
    margin: 0px;
    padding: 0px;
`;

const StyledTag = styled(Tag)`
    font-size: 12px;
`;

const Title = styled.div`
    color: ${ANTD_GRAY[6]};
    font-size: 10px;
    letter-spacing: 1px;
    margin-bottom: 12px;
`;

const DEFAULT_MODAL_OPTIONS = { visible: false, defaultActive: TestResultType.Success };

type Props = {
    urn: string;
    name: string;
};

export const TestResultsSummary = ({ urn, name }: Props) => {
    const [resultsModalOptions, setResultsModalOptions] = useState(DEFAULT_MODAL_OPTIONS);

    const { data: results } = useGetTestResultsSummaryQuery({
        skip: !urn || urn === PLACEHOLDER_TEST_URN,
        variables: {
            urn,
        },
    });

    const hasResults = results?.test?.results?.passingCount || results?.test?.results?.failingCount;
    const passingCount =
        results?.test?.results?.passingCount !== undefined
            ? formatNumberWithoutAbbreviation(results?.test?.results?.passingCount)
            : '-';
    const failingCount =
        results?.test?.results?.failingCount !== undefined
            ? formatNumberWithoutAbbreviation(results?.test?.results?.failingCount)
            : '-';

    return (
        <Container>
            <Title>RESULTS</Title>
            {(hasResults && (
                <>
                    <StyledButton
                        type="link"
                        onClick={() => setResultsModalOptions({ visible: true, defaultActive: TestResultType.Success })}
                    >
                        <StyledTag>
                            <Typography.Text style={{ color: SUCCESS_COLOR_HEX }} strong>
                                {passingCount}{' '}
                            </Typography.Text>
                            passing
                        </StyledTag>
                    </StyledButton>
                    <StyledButton
                        type="link"
                        onClick={() => setResultsModalOptions({ visible: true, defaultActive: TestResultType.Failure })}
                    >
                        <StyledTag>
                            <Typography.Text style={{ color: 'red' }} strong>
                                {failingCount}{' '}
                            </Typography.Text>
                            failing
                        </StyledTag>
                    </StyledButton>
                </>
            )) || <NoResultsSummary />}
            {resultsModalOptions.visible && results && (
                <TestResultsModal
                    urn={urn}
                    name={name}
                    defaultActive={resultsModalOptions.defaultActive}
                    passingCount={passingCount}
                    failingCount={failingCount}
                    onClose={() => setResultsModalOptions(DEFAULT_MODAL_OPTIONS)}
                />
            )}
        </Container>
    );
};
