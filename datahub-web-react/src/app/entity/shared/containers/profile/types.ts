import {
    DownstreamEntityRelationships,
    GlobalTags,
    GlobalTagsUpdate,
    GlossaryTerms,
    Maybe,
    Ownership,
    OwnershipUpdate,
    UpstreamEntityRelationships,
} from '../../../../../types.generated';

export type Tab = {
    name: string;
    component: React.ComponentType;
    hide?: () => boolean;
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
};

export type GenericEntityUpdate = {
    urn: string;
    description?: Maybe<string>;
    globalTags?: Maybe<GlobalTagsUpdate>;
    ownership?: Maybe<OwnershipUpdate>;
};
