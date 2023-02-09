import { Dataset, Entity, EntityType } from '../../../../../types.generated';

function getAssertionsSummary(dataset: Dataset) {
    let numAssertionsFailing = 0;
    let numAssertionsPassing = 0;

    dataset.assertions?.assertions?.forEach((assertion) => {
        const totalRunEvents = assertion.runEvents?.total;
        if (totalRunEvents) {
            const failingForDataset = assertion.runEvents?.failed;
            if (failingForDataset) {
                numAssertionsFailing += 1;
            } else {
                numAssertionsPassing += 1;
            }
        }
    });

    return { numAssertionsPassing, numAssertionsFailing };
}

export interface UpstreamSummary {
    passingUpstreams: number;
    failingUpstreams: number;
    datasetsWithFailingAssertions: Dataset[];
    datasetsWithActiveIncidents: Dataset[];
}

export function extractUpstreamSummary(upstreams: Entity[]): UpstreamSummary {
    let passingUpstreams = 0;
    let failingUpstreams = 0;
    const datasetsWithFailingAssertions = new Set<Dataset>();
    const datasetsWithActiveIncidents = new Set<Dataset>();

    upstreams.forEach((entity) => {
        if (entity.type === EntityType.Dataset) {
            const { numAssertionsPassing, numAssertionsFailing } = getAssertionsSummary(entity as Dataset);
            const { activeIncidents } = entity as any;

            if (numAssertionsFailing || activeIncidents.total) {
                failingUpstreams += 1;
            }

            if (numAssertionsFailing) {
                datasetsWithFailingAssertions.add(entity as Dataset);
            } else if (numAssertionsPassing) {
                passingUpstreams += 1;
            }

            if (activeIncidents.total) {
                datasetsWithActiveIncidents.add(entity as Dataset);
            }
        }
    });

    return {
        failingUpstreams,
        passingUpstreams,
        datasetsWithFailingAssertions: Array.from(datasetsWithFailingAssertions),
        datasetsWithActiveIncidents: Array.from(datasetsWithActiveIncidents),
    };
}

export function getNumAssertionsFailing(dataset: Dataset) {
    let numFailing = 0;

    dataset.assertions?.assertions.forEach((assertion) => {
        if (assertion.runEvents?.failed) {
            numFailing += 1;
        }
    });

    return numFailing;
}
