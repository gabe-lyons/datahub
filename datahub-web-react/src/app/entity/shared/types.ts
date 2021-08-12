import {
    DataPlatform,
    DownstreamEntityRelationships,
    EntityType,
    GlobalTags,
    GlobalTagsUpdate,
    GlossaryTerms,
    Maybe,
    Ownership,
    OwnershipUpdate,
    StringMapEntry,
    UpstreamEntityRelationships,
} from '../../../types.generated';

export type EntityTab = {
    name: string;
    component: React.FunctionComponent<TabProps>;
    hide?: (GenericEntityProperties) => boolean;
};

export type GenericEntityProperties = {
    urn?: string;
    name?: Maybe<string>;
    description?: Maybe<string>;
    globalTags?: Maybe<GlobalTags>;
    glossaryTerms?: Maybe<GlossaryTerms>;
    upstreamLineage?: Maybe<UpstreamEntityRelationships>;
    downstreamLineage?: Maybe<DownstreamEntityRelationships>;
    ownership?: Maybe<Ownership>;
    platform?: Maybe<DataPlatform>;
    properties: Maybe<StringMapEntry[]>;
};

export type GenericEntityUpdate = {
    urn: string;
    description?: Maybe<string>;
    globalTags?: Maybe<GlobalTagsUpdate>;
    ownership?: Maybe<OwnershipUpdate>;
};

export type TabProps = {
    urn: string;
    entityType: EntityType;
    entityData: GenericEntityProperties | null;
};
