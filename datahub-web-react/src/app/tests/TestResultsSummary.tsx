import React from 'react';
import { Button, Tag, Typography } from 'antd';
import { useHistory } from 'react-router-dom';
import styled from 'styled-components';
import { SUCCESS_COLOR_HEX } from '../entity/shared/tabs/Incident/incidentUtils';
import { navigateToSearchUrl } from '../search/utils/navigateToSearchUrl';
import { useGetTestResultsSummaryQuery } from '../../graphql/test.generated';
import { formatNumberWithoutAbbreviation } from '../shared/formatNumber';
import { NoResultsSummary } from './NoResultsSummary';
import { ANTD_GRAY } from '../entity/shared/constants';
import { PLACEHOLDER_TEST_URN } from './constants';

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

type Props = {
    urn: string;
};

export const TestResultsSummary = ({ urn }: Props) => {
    const history = useHistory();

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
                        onClick={() =>
                            navigateToSearchUrl({
                                filters: [
                                    {
                                        field: 'passingTests',
                                        values: [urn],
                                    },
                                ],
                                history,
                            })
                        }
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
                        onClick={() =>
                            navigateToSearchUrl({
                                filters: [
                                    {
                                        field: 'failingTests',
                                        values: [urn],
                                    },
                                ],
                                history,
                            })
                        }
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
        </Container>
    );
};
