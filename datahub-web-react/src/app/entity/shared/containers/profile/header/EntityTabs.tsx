import React, { useCallback, useEffect } from 'react';
import { useHistory } from 'react-router';
import { Tabs } from 'antd';
import styled from 'styled-components';

import { EntityTab } from '../../../types';
import { getEntityPath } from '../utils';
import { EntityType } from '../../../../../../types.generated';
import useIsLineageMode from '../../../../../lineage/utils/useIsLineageMode';
import { useEntityRegistry } from '../../../../../useEntityRegistry';

type Props = {
    tabs: EntityTab[];
    selectedTab?: EntityTab;
    urn: string;
    entityType: EntityType;
};

const Tab = styled(Tabs.TabPane)`
    font-size: 14px;
    line-height: 22px;
`;

export const EntityTabs = ({ tabs, selectedTab, urn, entityType }: Props) => {
    const isLineageMode = useIsLineageMode();
    const entityRegistry = useEntityRegistry();
    const history = useHistory();

    console.log({ selectedTab });

    const routeToTab = useCallback(
        (tabName: string) => {
            history.push(getEntityPath(entityType, urn, entityRegistry, isLineageMode, tabName));
        },
        [history, entityType, urn, entityRegistry, isLineageMode],
    );

    useEffect(() => {
        if (!selectedTab) {
            if (tabs[0]) {
                routeToTab(tabs[0].name);
            }
        }
    }, [tabs, selectedTab, routeToTab]);

    return (
        <Tabs activeKey={selectedTab?.name} size="large" onTabClick={(tab: string) => routeToTab(tab)}>
            {tabs.map((tab) => (
                <Tab tab={tab.name} key={tab.name} />
            ))}
        </Tabs>
    );
};
