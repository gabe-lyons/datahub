import React from 'react';
import { EditableSchemaMetadata, EntityType, GlobalTags, SchemaField } from '../../../../../../../types.generated';
import TagTermGroup from '../../../../../../shared/tags/TagTermGroup';
import { findFieldPathProposal } from '../../../../../../shared/tags/utils/proposalUtils';
import { pathMatchesNewPath } from '../../../../../dataset/profile/schema/utils/utils';
import { useSchemaRefetch } from '../SchemaContext';
import { useBaseEntity, useMutationUrn, useRefetch } from '../../../../EntityContext';

export default function useTagsAndTermsRenderer(
    editableSchemaMetadata: EditableSchemaMetadata | null | undefined,
    tagHoveredIndex: string | undefined,
    setTagHoveredIndex: (index: string | undefined) => void,
    options: { showTags: boolean; showTerms: boolean },
    filterText: string,
) {
    const urn = useMutationUrn();
    const baseEntity = useBaseEntity();
    const refetch = useRefetch();
    const schemaRefetch = useSchemaRefetch();

    const refresh: any = () => {
        refetch?.();
        schemaRefetch?.();
    };

    const tagAndTermRender = (tags: GlobalTags, record: SchemaField) => {
        const relevantEditableFieldInfo = editableSchemaMetadata?.editableSchemaFieldInfo.find(
            (candidateEditableFieldInfo) => pathMatchesNewPath(candidateEditableFieldInfo.fieldPath, record.fieldPath),
        );

        return (
            <div data-testid={`schema-field-${record.fieldPath}-${options.showTags ? 'tags' : 'terms'}`}>
                <TagTermGroup
                    uneditableTags={options.showTags ? tags : null}
                    editableTags={options.showTags ? relevantEditableFieldInfo?.globalTags : null}
                    uneditableGlossaryTerms={options.showTerms ? record.glossaryTerms : null}
                    editableGlossaryTerms={options.showTerms ? relevantEditableFieldInfo?.glossaryTerms : null}
                    canRemove
                    buttonProps={{ size: 'small' }}
                    canAddTag={tagHoveredIndex === record.fieldPath && options.showTags}
                    canAddTerm={tagHoveredIndex === record.fieldPath && options.showTerms}
                    onOpenModal={() => setTagHoveredIndex(undefined)}
                    entityUrn={urn}
                    entityType={EntityType.Dataset}
                    entitySubresource={record.fieldPath}
                    highlightText={filterText}
                    refetch={refresh}
                    proposedGlossaryTerms={
                        options.showTerms
                            ? findFieldPathProposal(
                                  // eslint-disable-next-line
                                  // @ts-ignore
                                  // eslint-disable-next-line
                                  baseEntity?.['dataset']?.['termProposals'] || [],
                                  record.fieldPath,
                              )
                            : []
                    }
                    proposedTags={
                        options.showTags
                            ? findFieldPathProposal(
                                  // eslint-disable-next-line
                                  // @ts-ignore
                                  // eslint-disable-next-line
                                  baseEntity?.['dataset']?.['tagProposals'] || [],
                                  record.fieldPath,
                              )
                            : []
                    }
                />
            </div>
        );
    };
    return tagAndTermRender;
}
