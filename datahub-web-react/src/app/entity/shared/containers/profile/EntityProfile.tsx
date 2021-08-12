import React from 'react';
import { Alert } from 'antd';
import { MutationHookOptions, MutationTuple, QueryHookOptions, QueryResult } from '@apollo/client/react/types/types';
import styled from 'styled-components';

import { EntityType, Exact } from '../../../../../types.generated';
import { Message } from '../../../../shared/Message';
import { useEntityRegistry } from '../../../../useEntityRegistry';
import { getDataForEntityType, useRoutedTab } from './utils';
import { EntityTab, GenericEntityProperties, GenericEntityUpdate } from '../../types';
import { ProfileNavBar } from './nav/ProfileNavBar';
import { REDESIGN_COLORS } from '../../constants';
import { EntityHeader } from './header/EntityHeader';
import { EntityTabs } from './header/EntityTabs';

type Props<T, U> = {
    urn: string;
    entityType: EntityType;
    useEntityQuery: (
        baseOptions: QueryHookOptions<
            T,
            Exact<{
                urn: string;
            }>
        >,
    ) => QueryResult<
        T,
        Exact<{
            urn: string;
        }>
    >;
    useUpdateQuery: (
        baseOptions?: MutationHookOptions<U, { input: GenericEntityUpdate }> | undefined,
    ) => MutationTuple<U, { input: GenericEntityUpdate }>;
    getOverrideProperties: (T) => GenericEntityProperties;
    tabs: EntityTab[];
};

const ContentContainer = styled.div`
    display: flex;
    height: auto;
    min-height: 100%;
    align-items: stretch;
    flex: 1;
    font-family: 'Manrope', 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans',
        'Helvetica Neue', sans-serif;
`;

const HeaderAndTabs = styled.div`
    width: 100%;
    justify-content: space-between;
    display: flex;
    flex-direction: column;
`;
const Sidebar = styled.div`
    width: 489px;
    border: 1px solid ${REDESIGN_COLORS.GREY};
`;
const Header = styled.div`
    height: 135px;
    border: 1px solid ${REDESIGN_COLORS.GREY};
    padding: 20px;
`;
const TabContent = styled.div`
    border: 1px solid ${REDESIGN_COLORS.GREY};
    flex: 1;
`;

// TODO(Gabe): Refactor this to generate dynamically
const QUERY_NAME = 'getDataset';

/**
 * Container for display of the Entity Page
 */
export const EntityProfile = <T, U>({
    urn,
    useEntityQuery,
    useUpdateQuery,
    entityType,
    getOverrideProperties,
    tabs,
}: Props<T, U>): JSX.Element => {
    const entityRegistry = useEntityRegistry();
    const routedTab = useRoutedTab(tabs);

    const { loading, error, data } = useEntityQuery({ variables: { urn } });

    const [updateEntity] = useUpdateQuery({
        refetchQueries: () => [QUERY_NAME],
    });

    const entityData = getDataForEntityType({ data, entityType, getOverrideProperties });

    console.log({ entityRegistry, updateEntity });

    return (
        <>
            <ProfileNavBar urn={urn} entityData={entityData} entityType={entityType} />
            {loading && <Message type="loading" content="Loading..." style={{ marginTop: '10%' }} />}
            {!loading && error && (
                <Alert type="error" message={error?.message || `Entity failed to load for urn ${urn}`} />
            )}
            <ContentContainer>
                <HeaderAndTabs>
                    <Header>
                        <EntityHeader urn={urn} entityType={entityType} entityData={entityData} />
                        <EntityTabs tabs={tabs} urn={urn} entityType={entityType} selectedTab={routedTab} />
                    </Header>
                    <TabContent>
                        {routedTab && <routedTab.component urn={urn} entityType={entityType} entityData={entityData} />}
                    </TabContent>
                </HeaderAndTabs>
                <Sidebar>Sidebar</Sidebar>
            </ContentContainer>

            {/* entityData && <div>hi</div> */}
        </>
    );
};

/* <LegacyEntityProfile
                    titleLink={`/${entityRegistry.getPathName(entityType)}/${urn}?is_lineage_mode=${isLineageMode}`}
                    title={entityData.name || ''}
                    tags={
                        <TagTermGroup
                            editableTags={entityData?.globalTags}
                            glossaryTerms={entityData?.glossaryTerms}
                            canAdd
                            canRemove
                            updateTags={(globalTags) => {
                                analytics.event({
                                    type: EventType.EntityActionEvent,
                                    actionType: EntityActionType.UpdateTags,
                                    entityType,
                                    entityUrn: urn,
                                });
                                return updateEntity({ variables: { input: { urn, globalTags } } });
                            }}
                        />
                    }
                    tabs={[]}
                    header={<div>I AM A HEADER</div>}
                    onTabChange={(tab: string) => {
                        analytics.event({
                            type: EventType.EntitySectionViewEvent,
                            entityType,
                            entityUrn: urn,
                            section: tab,
                        });
                    }}
                />
            )} */
