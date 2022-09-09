import React, { useEffect, useState } from 'react';
import styled from 'styled-components';
import { PropertyPredicate } from '../types';
import { Property } from './types/properties';
import { getPropertyWithId } from './utils';
import { CustomPropertyPredicateBuilder } from './CustomPropertyPredicateBuilder';
import { TypedPropertyPredicateBuilder } from './TypedPropertyPredicateBuilder';

const CUSTOM_ID = 'custom';

const PredicateContainer = styled.div`
    display: flex;
    align-items: center;
`;

type Props = {
    selectedPredicate?: PropertyPredicate;
    properties: Property[];
    onChangePredicate: (newPredicate: PropertyPredicate) => void;
};

/**
 * This component allows you to construct a single Property Predicate.
 */
export const PropertyPredicateBuilder = ({ selectedPredicate, properties, onChangePredicate }: Props) => {
    /**
     * Whether to show a custom property + operator input
     */
    const [isCustomPredicate, setIsCustomPredicate] = useState(false);

    /**
     * If we are not aware of the property id, fallback to building a custom predicate
     */
    useEffect(() => {
        const maybePropertyId = selectedPredicate?.property;
        if (maybePropertyId && !getPropertyWithId(maybePropertyId, properties)) {
            setIsCustomPredicate(true);
        }
    }, [selectedPredicate, properties, setIsCustomPredicate]);

    const onChangeProperty = (propertyId: string) => {
        if (propertyId === CUSTOM_ID) {
            // We should render the custom property viewer.
            setIsCustomPredicate(true);
            return;
        }
        const newPredicate = {
            property: propertyId,
        };
        onChangePredicate(newPredicate);
    };

    const onChangeOperator = (operatorId: string) => {
        const newPredicate = {
            ...selectedPredicate,
            operator: operatorId,
            values: undefined,
        };
        onChangePredicate(newPredicate);
    };

    const onChangeValues = (values: string[] | undefined) => {
        const newPredicate = {
            ...selectedPredicate,
            values,
        };
        onChangePredicate(newPredicate);
    };

    return (
        <PredicateContainer>
            {isCustomPredicate ? (
                <CustomPropertyPredicateBuilder
                    selectedPredicate={selectedPredicate}
                    onChangeProperty={onChangeProperty}
                    onChangeOperator={onChangeOperator}
                    onChangeValues={onChangeValues}
                />
            ) : (
                <TypedPropertyPredicateBuilder
                    selectedPredicate={selectedPredicate}
                    properties={properties}
                    onChangeProperty={onChangeProperty}
                    onChangeOperator={onChangeOperator}
                    onChangeValues={onChangeValues}
                />
            )}
        </PredicateContainer>
    );
};
