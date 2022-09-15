/**
 * The file contains a set of well-supported properties,
 * which the UI deeply understands.
 */
import { EntityType } from '../../../../../../../../types.generated';
import { SelectInputMode, ValueTypeId } from './values';

/**
 * This file is a work in progress. Backend work is
 * still required to ensure that these simply properties
 * resolve to real values. For now, large blocks will be commented out
 * until the backend implements them.
 *
 * We also will be moving some of these fields into the server as "aliases" for
 * nested predicates or other fields.
 */

/**
 * A single well-supported property.
 */
export type Property = {
    id: string;
    displayName: string;
    description?: string;
    valueType?: ValueTypeId;
    valueOptions?: any;
    children?: Property[]; // Child Properties.
};

/**
 * Maps properties to their nested properties and types, which are required to render the correct
 * operator + predicate types. --> These all need to come from the server ideally in a one-time fetch.
 *
 * Example cases:
 *          "If an asset has an upstream that is tagged with sensitive, tag it with sensitive."
 *          "If an asset has > 10 upstreams, then mark it as important."
 *          "If an asset has no upstreams, then mark it as root."
 *          "If an asset has > 10 downstreams, then mark it as Heavy node"
 *          "Must have an owner of type Technical Owner"
 */
const commonProps: Property[] = [
    // {
    //     id: 'entityType', // --> TODO Determine what this means for Datasets.
    //     displayName: 'Asset Type',
    //     description: 'The type of the asset.',
    //     valueType: ValueTypeId.STRING,
    //     valueOptions: {
    //         mode: 'multiple',
    //         options: [
    //             {
    //                 id: 'dataset',
    //                 displayName: 'Dataset',
    //             },
    //             {
    //                 id: 'dashboard',
    //                 displayName: 'Dashboard',
    //             },
    //             {
    //                 id: 'chart',
    //                 displayName: 'Chart',
    //             },
    //             {
    //                 id: 'dataJob',
    //                 displayName: 'Data Job (Task)',
    //             },
    //             {
    //                 id: 'dataFlow',
    //                 displayName: 'Data Flow (Pipeline)',
    //             },
    //             {
    //                 id: 'container',
    //                 displayName: 'Container',
    //             },
    //         ],
    //     },
    // },
    // {
    //     id: 'name', // --> TODO Determine what this means for Datasets.
    //     displayName: 'Name',
    //     description: 'The name of the asset, as defined at the source.',
    //     valueType: ValueTypeId.STRING,
    // },
    // {
    //     id: 'editableDatasetProperties.description',
    //     displayName: 'Description',
    //     description: 'The description text for the asset, as displayed inside the Documentation tab.',
    //     valueType: ValueTypeId.STRING,
    // },
    {
        id: 'dataPlatformInstance.platform',
        displayName: 'Platform',
        description: 'The data platform where the asset lives.',
        valueType: ValueTypeId.URN,
        valueOptions: {
            entityTypes: [EntityType.DataPlatform],
            mode: SelectInputMode.SINGLE,
        },
    },
    {
        id: 'globalTags.tags.tag',
        displayName: 'Tags',
        description: 'The tags attached to the asset.',
        valueType: ValueTypeId.URN_LIST,
        valueOptions: {
            entityTypes: [EntityType.Tag],
            mode: SelectInputMode.MULTIPLE,
        },
    },
    {
        id: 'glossaryTerms.terms.urn',
        displayName: 'Glossary Terms',
        description: 'The glossary terms attached to the asset.',
        valueType: ValueTypeId.URN_LIST,
        valueOptions: {
            entityTypes: [EntityType.GlossaryTerm],
            mode: SelectInputMode.MULTIPLE,
        },
        children: [
            {
                id: 'glossaryTerms.terms.urn.glossaryTermInfo.parentNode',
                displayName: 'Term Groups',
                description: 'The term groups in which the terms reside.',
                valueType: ValueTypeId.URN_LIST,
                valueOptions: {
                    entityTypes: [EntityType.GlossaryNode],
                    mode: SelectInputMode.MULTIPLE,
                },
            },
        ],
    },
    {
        id: 'domains.domains',
        displayName: 'Domain',
        description: 'The domain that the asset is a part of.',
        valueType: ValueTypeId.URN,
        valueOptions: {
            entityTypes: [EntityType.Domain],
            mode: SelectInputMode.SINGLE,
        },
    },
    {
        id: 'ownership.owners.owner',
        displayName: 'Owners',
        description: 'The owners of the asset.',
        valueType: ValueTypeId.URN_LIST,
        valueOptions: {
            entityTypes: [EntityType.CorpUser, EntityType.CorpGroup],
            mode: SelectInputMode.MULTIPLE,
        },
    },
    {
        id: 'container.container',
        displayName: 'Container',
        description: 'The parent container of the asset.',
        valueType: ValueTypeId.URN,
        valueOptions: {
            entityTypes: [EntityType.Container],
            mode: SelectInputMode.SINGLE,
        },
    },
    {
        id: 'deprecation.deprecated',
        displayName: 'Deprecated',
        description: 'Whether the asset is deprecated or not.',
        valueType: ValueTypeId.BOOLEAN,
    },
];

const datasetProps: Property[] = [
    ...commonProps,
    {
        id: 'subTypes.typeNames',
        displayName: 'Subtype',
        description: 'The subtype of the asset.',
        valueType: ValueTypeId.STRING,
    },
    // {
    //     id: 'schemaFields',
    //     displayName: 'Schema Fields',
    //     description: 'Apply conditions that match any schema field.',
    //     children: [
    //         {
    //             id: 'schemaFields.tags',
    //             displayName: 'Schema Field Tags',
    //             description: 'The aggregate set of tags attached to all schema fields.',
    //             valueType: ValueTypeId.URN_LIST,
    //             valueOptions: {
    //                 entityTypes: [EntityType.Tag],
    //                 mode: 'multiple',
    //             },
    //         },
    //         {
    //             id: 'schemaFields.glossaryTerms',
    //             displayName: 'Schema Field Glossary Terms',
    //             description: 'The aggregate set of glossary terms attached to all schema fields.',
    //             valueType: ValueTypeId.URN_LIST,
    //             valueOptions: {
    //                 entityTypes: [EntityType.GlossaryTerm],
    //                 mode: 'multiple',
    //             },
    //         },
    //         {
    //             id: 'schemaFields.name',
    //             displayName: 'Schema Field Names',
    //             description: 'The aggregate set of names of the schema fields, flattened into a schema path.',
    //             valueType: ValueTypeId.STRING_LIST,
    //         },
    //         {
    //             id: 'schemaFields.description',
    //             displayName: 'Schema Field Descriptions',
    //             description: 'The aggregate set of descriptions of the schema fields.',
    //             valueType: ValueTypeId.STRING_LIST,
    //         },
    //     ],
    // },
    {
        id: 'metrics',
        displayName: 'Metrics',
        children: [
            {
                id: 'usageFeatures.usageCountLast30Days',
                displayName: 'Query Count in Last 30 Days',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.queryCountPercentileLast30Days',
                displayName: 'Query Count Percentile in Last 30 Days (0-100)',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.writeCountLast30Days',
                displayName: 'Update Count in Last 30 Days',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.writeCountPercentileLast30Days',
                displayName: 'Update Count Percentile in Last 30 Days (0-100)',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.uniqueUserCountLast30Days',
                displayName: 'Unique Users in the Last 30 Days',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.uniqueUserPercentileLast30Days',
                displayName: 'Unique User Percentile in the Last 30 Days (0-100)',
                valueType: ValueTypeId.NUMBER,
            },
        ],
    },
];

const dataJobProps = [...commonProps];

const dataFlowProps = [...commonProps];

const dashboardProps = [
    ...commonProps,
    {
        id: 'metrics',
        displayName: 'Metrics',
        children: [
            {
                id: 'usageFeatures.viewCountTotal',
                displayName: 'Total View Count',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.viewCountLast30Days',
                displayName: 'View Count in Last 30 Days',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.viewCountPercentileLast30Days',
                displayName: 'View Count Percentile in Last 30 Days (0-100)',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.uniqueUserCountLast30Days',
                displayName: 'Unique Users in the Last 30 Days',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.uniqueUserPercentileLast30Days',
                displayName: 'Unique User Percentile in the Last 30 Days (0-100)',
                valueType: ValueTypeId.NUMBER,
            },
        ],
    },
];

const chartProps = [
    ...commonProps,
    {
        id: 'metrics',
        displayName: 'Metrics',
        selectable: false,
        children: [
            {
                id: 'usageFeatures.viewCountTotal',
                displayName: 'Total View Count',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.viewCountLast30Days',
                displayName: 'View Count in Last 30 Days',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.viewCountPercentileLast30Days',
                displayName: 'View Count Percentile in Last 30 Days (0-100)',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.uniqueUserCountLast30Days',
                displayName: 'Unique Users in the Last 30 Days',
                valueType: ValueTypeId.NUMBER,
            },
            {
                id: 'usageFeatures.uniqueUserPercentileLast30Days',
                displayName: 'Unique User Percentile in the Last 30 Days (0-100)',
                valueType: ValueTypeId.NUMBER,
            },
        ],
    },
];

const containerProps = [
    ...commonProps,
    {
        id: 'subTypes.typeNames',
        displayName: 'Subtypes',
        description: 'The subtype(s) of the asset.',
        valueType: ValueTypeId.STRING,
    },
];

/**
 * A list of entity types to the well-supported properties
 * that each can support.
 */
export const entityProperties = [
    {
        type: EntityType.Dataset,
        properties: datasetProps,
    },
    {
        type: EntityType.Dashboard,
        properties: dashboardProps,
    },
    {
        type: EntityType.Chart,
        properties: chartProps,
    },
    {
        type: EntityType.DataFlow,
        properties: dataFlowProps,
    },
    {
        type: EntityType.DataJob,
        properties: dataJobProps,
    },
    {
        type: EntityType.Container,
        properties: containerProps,
    },
];
