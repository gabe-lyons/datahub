import {
    DataPlatform,
    DownstreamEntityRelationships,
    GlobalTags,
    GlobalTagsUpdate,
    GlossaryTerms,
    Maybe,
    Ownership,
    OwnershipUpdate,
    UpstreamEntityRelationships,
} from '../../../../../types.generated';

export type EntityTab = {
    name: string;
    component: React.ComponentType;
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
};

export type GenericEntityUpdate = {
    urn: string;
    description?: Maybe<string>;
    globalTags?: Maybe<GlobalTagsUpdate>;
    ownership?: Maybe<OwnershipUpdate>;
};
