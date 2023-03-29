import { EntityType } from '../../../../../../../../types.generated';
import { entityProperties } from '../types/properties';
import { ValueInputType } from '../types/values';
import { getPropertiesForEntityType, getPropertiesForEntityTypes, getValueOptions } from '../utils';

describe('utils', () => {
    describe('getPropertiesForEntityTypes', () => {
        it('test single entity type', () => {
            expect(getPropertiesForEntityTypes([EntityType.Dataset])).toEqual(
                entityProperties.filter((obj) => obj.type === EntityType.Dataset)[0].properties,
            );
        });
        it('test empty entity types', () => {
            expect(getPropertiesForEntityTypes([])).toEqual([]);
        });
        it('test multiple entity type correctly intersects', () => {
            const res = getPropertiesForEntityTypes([EntityType.Dataset, EntityType.Chart, EntityType.Dashboard]);

            // Size of result should be less than both dataset props + chart props.
            expect(res.length).toBeLessThan(
                entityProperties.filter((obj) => obj.type === EntityType.Dataset)[0].properties.length,
            );
            expect(res.length).toBeLessThan(
                entityProperties.filter((obj) => obj.type === EntityType.Chart)[0].properties.length,
            );
            expect(res.length).toBeLessThan(
                entityProperties.filter((obj) => obj.type === EntityType.Dashboard)[0].properties.length,
            );
        });
    });
    describe('getValueOptions', () => {
        it('returns correct input type for timestamp properties', () => {
            expect(
                getValueOptions(
                    {
                        property: '__firstSynchronized',
                        operator: 'greater_than',
                        values: ['1'],
                    },
                    getPropertiesForEntityType(EntityType.Dataset),
                ),
            ).toEqual({
                inputType: ValueInputType.TIME_SELECT,
            });
        });
    });
});
