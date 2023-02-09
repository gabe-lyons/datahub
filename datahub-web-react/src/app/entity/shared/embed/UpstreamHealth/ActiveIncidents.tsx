import React from 'react';
import { useEntityRegistry } from '../../../../useEntityRegistry';
import { FailingDataWrapper, FailingSectionWrapper } from './FailingAssertions';
import FailingEntity from './FailingEntity';
import { UpstreamSummary } from './utils';

interface Props {
    upstreamSummary: UpstreamSummary;
}

export default function ActiveIncidents({ upstreamSummary }: Props) {
    const { datasetsWithActiveIncidents } = upstreamSummary;
    const entityRegistry = useEntityRegistry();

    return (
        <FailingSectionWrapper>
            {datasetsWithActiveIncidents.length} active incident{datasetsWithActiveIncidents.length > 1 && 's'} on data
            sources
            <FailingDataWrapper>
                {datasetsWithActiveIncidents.map((dataset) => {
                    const numActiveIncidents = (dataset as any).activeIncidents.total;

                    return (
                        <FailingEntity
                            key={dataset.urn}
                            link={entityRegistry.getEntityUrl(dataset.type, dataset.urn)}
                            displayName={entityRegistry.getDisplayName(dataset.type, dataset)}
                            contentText={`${numActiveIncidents} active incident${numActiveIncidents > 1 ? 's' : ''}`}
                        />
                    );
                })}
            </FailingDataWrapper>
        </FailingSectionWrapper>
    );
}
