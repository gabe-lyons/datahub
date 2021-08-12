import { useLocation } from 'react-router';
import { EntityType } from '../../../../../types.generated';
import useIsLineageMode from '../../../../lineage/utils/useIsLineageMode';
import { useEntityRegistry } from '../../../../useEntityRegistry';
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

export function useEntityPath(entityType: EntityType, urn: string) {
    const isLineageMode = useIsLineageMode();
    const entityRegistry = useEntityRegistry();

    return `/${entityRegistry.getPathName(entityType)}/${urn}?is_lineage_mode=${isLineageMode}`;
}

export function useRoutedTab(tabs: EntityTab[]): EntityTab | undefined {
    const { pathname } = useLocation();
    const trimmedPathName = pathname.endsWith('/') ? pathname.slice(0, pathname.length - 1) : pathname;
    const splitPathName = trimmedPathName.split('/');
    const lastTokenInPath = splitPathName[splitPathName.length - 1];
    const routedTab = tabs.find((tab) => tab.name === lastTokenInPath);
    return routedTab;
}
