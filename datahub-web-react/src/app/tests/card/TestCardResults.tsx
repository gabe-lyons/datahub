import React from 'react';
import { TestResultsSummary } from '../TestResultsSummary';

type Props = {
    testUrn: string;
    name: string;
};

export const TestCardResults = ({ testUrn, name }: Props) => {
    return <TestResultsSummary urn={testUrn} name={name} />;
};
