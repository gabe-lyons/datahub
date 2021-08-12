import React, { useEffect } from 'react';

import { EntityTab } from '../../../types';
import { useEntityPath } from '../utils';
import { EntityType } from '../../../../../../types.generated';
import { useHistory } from 'react-router';

type Props = {
    tabs: EntityTab[];
    selectedTab: EntityTab;
    urn: string;
    entityType: EntityType;
};

export const EntityTabs = ({ tabs, selectedTab, urn, entityType }: Props) => {
    console.log({ tabs, selectedTab, urn, entityType });
    const entityPath = useEntityPath(entityType, urn);
    const history = useHistory();

    const routeToTab = (tab: EntityTab) => {
        history.push(`${entityPath}/${tab.name}`);
    };

    useEffect(() => {
        if (!selectedTab) {
            if (tabs[0]) {
                routeToTab(tabs[0]);
            }
        }
    }, [tabs, selectedTab, entityPath]);

    return <div>tabs {entityPath}</div>;
};
