import React from 'react';
import { Button, Tag, Typography } from 'antd';
import { useHistory } from 'react-router-dom';
import styled from 'styled-components';
import { SUCCESS_COLOR_HEX } from '../entity/shared/tabs/Incident/incidentUtils';
import { navigateToSearchUrl } from '../search/utils/navigateToSearchUrl';
import { useGetTestResultsSummaryQuery } from '../../graphql/test.generated';
import { formatNumberWithoutAbbreviation } from '../shared/formatNumber';
import { PLACEHOLDER_TEST_URN } from './utils';

const StyledButton = styled(Button)`
    margin: 0px;
    padding: 0px;
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

    const passingCount =
        results?.test?.results?.passingCount !== undefined
            ? formatNumberWithoutAbbreviation(results?.test?.results?.passingCount)
            : '-';
    const failingCount =
        results?.test?.results?.failingCount !== undefined
            ? formatNumberWithoutAbbreviation(results?.test?.results?.failingCount)
            : '-';

    return (
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
                <Tag>
                    <Typography.Text style={{ color: SUCCESS_COLOR_HEX }} strong>
                        {passingCount}{' '}
                    </Typography.Text>
                    passing{' '}
                </Tag>
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
                <Tag>
                    <Typography.Text style={{ color: 'red' }} strong>
                        {failingCount}{' '}
                    </Typography.Text>
                    failing
                </Tag>
            </StyledButton>
        </>
    );
};
