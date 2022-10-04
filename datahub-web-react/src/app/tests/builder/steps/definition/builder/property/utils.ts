import { EntityType } from '../../../../../../../types.generated';
import { PropertyPredicate } from '../types';
import { isUnaryOperator, Operator, OPERATOR_ID_TO_DETAILS } from './types/operators';
import { entityProperties, Property } from './types/properties';
import { ValueInputType, ValueOptions, VALUE_TYPE_ID_TO_DETAILS } from './types/values';

/**
 * Returns true if a well-supported Property supports searchable
 * entity values.
 */
const isSearchableProperty = (property: Property): boolean => {
    return property.valueOptions?.entityTypes && property.valueOptions?.mode;
};

/**
 * Returns true if a well-supported Property supports a fixed
 * set of select options.
 */
const isSelectableProperty = (property: Property): boolean => {
    return property.valueOptions?.options && property.valueOptions?.mode;
};

/**
 * Returns a list of properties supported fora given entity type.
 */
export const getPropertiesForEntityType = (type: EntityType): Property[] => {
    const maybeProperties = entityProperties.filter((entry) => entry.type === type);
    return maybeProperties.length > 0 ? maybeProperties[0].properties : [];
};

const intersectPropertySets = (a: Property[], b: Property[]): Property[] => {
    const aIds = new Set<string>(a.map((prop) => prop.id));
    const mergedProperties: Property[] = [];
    b.forEach((prop) => {
        if (aIds.has(prop.id)) {
            mergedProperties.push(prop);
        }
    });
    return mergedProperties;
};

/**
 * Returns the subset of properties supported by all entity types in the set. (intersection)
 */
export const getPropertiesForEntityTypes = (types: EntityType[]): Property[] => {
    const propertySets: Property[][] = [];
    types.forEach((type) => {
        const props = getPropertiesForEntityType(type);
        propertySets.push(props);
    });
    return propertySets.length > 0 ? propertySets.reduce(intersectPropertySets) : [];
};

/**
 * Retrieves a specific Property from a list of properties given the
 * property's unique id, or undefined if one cannot be found.
 */
export const getPropertyWithId = (propertyId: string, properties: Property[]): Property | undefined => {
    // eslint-disable-next-line
    for (const prop of properties) {
        if (prop.id === propertyId) {
            return prop;
        }
        if (prop?.children) {
            const foundProp = getPropertyWithId(propertyId, prop?.children);
            if (foundProp) {
                return foundProp;
            }
        }
    }
    return undefined;
};

/**
 * Returns the set of operators that are supported
 * for a given well-supported property.
 *
 * This is based on the "value type" of the property, along with the options.
 */
export const getOperatorOptions = (predicate: PropertyPredicate, properties: Property[]): Operator[] | undefined => {
    if (!predicate.property) {
        return undefined;
    }
    const property = getPropertyWithId(predicate.property, properties);
    if (!property) {
        console.warn(
            `Failed to find Metadata Tests property with id ${predicate.property}. Could not find operator options`,
        );
        return undefined;
    }
    const maybeDetails = VALUE_TYPE_ID_TO_DETAILS.get(property.valueType);
    if (maybeDetails) {
        return maybeDetails.operators.map((op) => OPERATOR_ID_TO_DETAILS.get(op));
    }
    return [];
};

/**
 * Returns a set of ValueOptions which determines how to render
 * the value selector for a particular well-supported Property.
 */
export const getValueOptions = (predicate: PropertyPredicate, properties: Property[]): ValueOptions | undefined => {
    if (!predicate.property || !predicate.operator || isUnaryOperator(predicate.operator)) {
        return undefined;
    }
    const property = getPropertyWithId(predicate.property, properties);
    if (!property) {
        // Did not find any matching property.
        console.warn(
            `Failed to find Metadata Tests property with id ${predicate.property}. Could not find value options`,
        );
        return undefined;
    }
    // Display an Entity Search values input.
    if (isSearchableProperty(property)) {
        return {
            inputType: ValueInputType.ENTITY_SEARCH,
            options: property.valueOptions,
        };
    }
    // Display a fixed select values input.
    if (isSelectableProperty(property)) {
        return {
            inputType: ValueInputType.SELECT,
            options: property.valueOptions,
        };
    }
    // By default, just render a normal text input.
    return {
        inputType: ValueInputType.TEXT,
        options: property.valueOptions,
    };
};
