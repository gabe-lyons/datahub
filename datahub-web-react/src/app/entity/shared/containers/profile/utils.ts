import { EntityType } from '../../../../../types.generated';
import { GenericEntityProperties } from './types';

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
