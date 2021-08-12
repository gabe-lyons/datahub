import { useLocation, useRouteMatch } from 'react-router';
import { EntityType } from '../../../../../types.generated';
import useIsLineageMode from '../../../../lineage/utils/useIsLineageMode';
import { useEntityRegistry } from '../../../../useEntityRegistry';
import EntityRegistry from '../../../EntityRegistry';
import { EntityTab, GenericEntityProperties } from '../../types';

export function getDataForEntityType<T>({
    data,
    entityType,
    getOverrideProperties,
}: {
    data: T;
    entityType: EntityType;
    getOverrideProperties: (T) => GenericEntityProperties;
}): GenericEntityProperties | null {
    if (!data) {
        return null;
    }
    return {
        ...data[entityType.toLowerCase()],
        ...getOverrideProperties(data),
    };
}

export function getEntityPath(
    entityType: EntityType,
    urn: string,
    entityRegistry: EntityRegistry,
    isLineageMode: boolean,
    tabName?: string,
) {
    if (!tabName) {
        return `/${entityRegistry.getPathName(entityType)}/${urn}?is_lineage_mode=${isLineageMode}`;
    }
    return `/${entityRegistry.getPathName(entityType)}/${urn}/${tabName}?is_lineage_mode=${isLineageMode}`;
}

export function useEntityPath(entityType: EntityType, urn: string, tabName?: string) {
    const isLineageMode = useIsLineageMode();
    const entityRegistry = useEntityRegistry();
    return getEntityPath(entityType, urn, entityRegistry, isLineageMode, tabName);
}

export function useRoutedTab(tabs: EntityTab[]): EntityTab | undefined {
    const { pathname } = useLocation();
    const { path, url } = useRouteMatch();
    const trimmedPathName = pathname.endsWith('/') ? pathname.slice(0, pathname.length - 1) : pathname;
    const splitPathName = trimmedPathName.split('/');
    const lastTokenInPath = splitPathName[splitPathName.length - 1];
    const routedTab = tabs.find((tab) => tab.name === lastTokenInPath);
    console.log({ pathname, lastTokenInPath, splitPathName, routedTab, path, url });
    return routedTab;
}
