import React from 'react';
import { Divider } from 'antd';
import styled from 'styled-components';

import TagTermGroup from '../../../../../shared/tags/TagTermGroup';
import { SidebarHeader } from './SidebarHeader';
import { useBaseEntity, useEntityData, useMutationUrn, useRefetch } from '../../../EntityContext';
import { findTopLevelProposals } from '../../../../../shared/tags/utils/proposalUtils';
import { GetDatasetQuery } from '../../../../../../graphql/dataset.generated';
import {
    ENTITY_PROFILE_GLOSSARY_TERMS_ID,
    ENTITY_PROFILE_TAGS_ID,
} from '../../../../../onboarding/config/EntityProfileOnboardingConfig';
import ConstraintGroup from '../../../../../shared/constraints/ConstraintGroup';

const StyledDivider = styled(Divider)`
    margin: 16px 0;
`;

interface Props {
    properties?: any;
    readOnly?: boolean;
}

export const SidebarTagsSection = ({ properties, readOnly }: Props) => {
    const canAddTag = properties?.hasTags;
    const canAddTerm = properties?.hasTerms;

    const mutationUrn = useMutationUrn();

    const { entityType, entityData } = useEntityData();
    const baseEntity = useBaseEntity<GetDatasetQuery>();

    const refetch = useRefetch();

    return (
        <div>
            <span id={ENTITY_PROFILE_TAGS_ID}>
                <SidebarHeader title="Tags" />
                <TagTermGroup
                    editableTags={entityData?.globalTags}
                    canAddTag={canAddTag}
                    canRemove
                    showEmptyMessage
                    entityUrn={mutationUrn}
                    entityType={entityType}
                    refetch={refetch}
                    readOnly={readOnly}
                    fontSize={12}
                    proposedTags={findTopLevelProposals(entityData?.tagProposals || [])}
                />
            </span>
            <StyledDivider />
            <span id={ENTITY_PROFILE_GLOSSARY_TERMS_ID}>
                <SidebarHeader title="Glossary Terms" />
                <ConstraintGroup constraints={baseEntity?.dataset?.constraints || []} />
                <TagTermGroup
                    editableGlossaryTerms={entityData?.glossaryTerms}
                    canAddTerm={canAddTerm}
                    canRemove
                    showEmptyMessage
                    entityUrn={mutationUrn}
                    entityType={entityType}
                    refetch={refetch}
                    readOnly={readOnly}
                    fontSize={12}
                    proposedGlossaryTerms={findTopLevelProposals(entityData?.termProposals || [])}
                />
            </span>
        </div>
    );
};
