import React from 'react';
import { TestResultsSummary } from '../TestResultsSummary';

type Props = {
    testUrn: string;
};

export const TestCardResults = ({ testUrn }: Props) => {
    return <TestResultsSummary urn={testUrn} />;
};
