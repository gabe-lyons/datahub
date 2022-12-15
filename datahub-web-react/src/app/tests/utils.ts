import { ListTestsDocument, ListTestsQuery } from '../../graphql/test.generated';

export const PLACEHOLDER_TEST_URN = 'placeholder-test-urn';

/**
 * Add an entry to the ListTests cache.
 */
export const addToListTestsCache = (client, newTest, pageSize, query) => {
    // Read the data from our cache for this query.
    const currData: ListTestsQuery | null = client.readQuery({
        query: ListTestsDocument,
        variables: {
            input: {
                start: 0,
                count: pageSize,
                query,
            },
        },
    });

    // Add our new test into the existing list.
    const newTests = [newTest, ...(currData?.listTests?.tests || [])];

    // Write our data back to the cache.
    client.writeQuery({
        query: ListTestsDocument,
        variables: {
            input: {
                start: 0,
                count: pageSize,
                query,
            },
        },
        data: {
            listTests: {
                start: 0,
                count: (currData?.listTests?.count || 0) + 1,
                total: (currData?.listTests?.total || 0) + 1,
                tests: newTests,
            },
        },
    });
};

/**
 * Remove an entry from the ListTests cache.
 */
export const removeFromListTestsCache = (client, urn, page, pageSize, query) => {
    // Read the data from our cache for this query.
    const currData: ListTestsQuery | null = client.readQuery({
        query: ListTestsDocument,
        variables: {
            input: {
                start: (page - 1) * pageSize,
                count: pageSize,
                query,
            },
        },
    });

    // Remove the test from the existing tests set.
    const newTests = [...(currData?.listTests?.tests || []).filter((test) => test.urn !== urn)];

    // Write our data back to the cache.
    client.writeQuery({
        query: ListTestsDocument,
        variables: {
            input: {
                start: (page - 1) * pageSize,
                count: pageSize,
                query,
            },
        },
        data: {
            listTests: {
                start: currData?.listTests?.start || 0,
                count: (currData?.listTests?.count || 1) - 1,
                total: (currData?.listTests?.total || 1) - 1,
                tests: newTests,
            },
        },
    });
};
