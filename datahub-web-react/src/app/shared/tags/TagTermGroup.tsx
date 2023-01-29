<<<<<<< HEAD
import { Modal, Tag, Typography, Button, message, Tooltip } from 'antd';
=======
import { Typography, Button } from 'antd';
>>>>>>> oss_master
import React, { useState } from 'react';
import styled from 'styled-components';
<<<<<<< HEAD
import { BookOutlined, ClockCircleOutlined, PlusOutlined, ThunderboltOutlined } from '@ant-design/icons';
import Highlight from 'react-highlighter';

import { useEntityRegistry } from '../../useEntityRegistry';
import {
    Domain,
    ActionRequest,
    EntityType,
    GlobalTags,
    GlossaryTermAssociation,
    GlossaryTerms,
    SubResourceType,
    TagAssociation,
} from '../../../types.generated';
import { StyledTag } from '../../entity/shared/components/styled/StyledTag';
=======
import { PlusOutlined } from '@ant-design/icons';
import Highlight from 'react-highlighter';

import { useEntityRegistry } from '../../useEntityRegistry';
import { Domain, EntityType, GlobalTags, GlossaryTerms, SubResourceType } from '../../../types.generated';
>>>>>>> oss_master
import { EMPTY_MESSAGES, ANTD_GRAY } from '../../entity/shared/constants';
import { DomainLink } from './DomainLink';
<<<<<<< HEAD
import { TagProfileDrawer } from './TagProfileDrawer';
import { useAcceptProposalMutation, useRejectProposalMutation } from '../../../graphql/actionRequest.generated';
import ProposalModal from './ProposalModal';
=======
>>>>>>> oss_master
import EditTagTermsModal from './AddTagsTermsModal';
import StyledTerm from './term/StyledTerm';
import Tag from './tag/Tag';

const PropagateThunderbolt = styled(ThunderboltOutlined)`
    color: rgba(0, 143, 100, 0.95);
    margin-right: -4px;
    font-weight: bold;
`;

type Props = {
    uneditableTags?: GlobalTags | null;
    editableTags?: GlobalTags | null;
    editableGlossaryTerms?: GlossaryTerms | null;
    uneditableGlossaryTerms?: GlossaryTerms | null;
    domain?: Domain | undefined | null;
    canRemove?: boolean;
    canAddTag?: boolean;
    canAddTerm?: boolean;
    showEmptyMessage?: boolean;
    buttonProps?: Record<string, unknown>;
    onOpenModal?: () => void;
    maxShow?: number;
    entityUrn?: string;
    entityType?: EntityType;
    entitySubresource?: string;
    highlightText?: string;
    refetch?: () => Promise<any>;
<<<<<<< HEAD

    proposedGlossaryTerms?: ActionRequest[];
    proposedTags?: ActionRequest[];
=======
    readOnly?: boolean;
>>>>>>> oss_master
};

const NoElementButton = styled(Button)`
    :not(:last-child) {
        margin-right: 8px;
    }
`;

const TagText = styled.span`
    color: ${ANTD_GRAY[7]};
`;

const ProposedTerm = styled(Tag)`
    opacity: 0.7;
    border-style: dashed;
`;

const PROPAGATOR_URN = 'urn:li:corpuser:__datahub_propagator';

const highlightMatchStyle = { background: '#ffe58f', padding: '0' };

export default function TagTermGroup({
    uneditableTags,
    editableTags,
    canRemove,
    canAddTag,
    canAddTerm,
    showEmptyMessage,
    buttonProps,
    onOpenModal,
    maxShow,
    uneditableGlossaryTerms,
    editableGlossaryTerms,
    proposedGlossaryTerms,
    proposedTags,
    domain,
    entityUrn,
    entityType,
    entitySubresource,
    highlightText,
    refetch,
    readOnly,
}: Props) {
    const entityRegistry = useEntityRegistry();
    const [showAddModal, setShowAddModal] = useState(false);
    const [addModalType, setAddModalType] = useState(EntityType.Tag);
<<<<<<< HEAD

    const [acceptProposalMutation] = useAcceptProposalMutation();
    const [rejectProposalMutation] = useRejectProposalMutation();
    const [showProposalDecisionModal, setShowProposalDecisionModal] = useState(false);

    const tagsEmpty =
        !editableTags?.tags?.length &&
        !uneditableTags?.tags?.length &&
        !editableGlossaryTerms?.terms?.length &&
        !uneditableGlossaryTerms?.terms?.length &&
        !proposedTags?.length &&
        !proposedGlossaryTerms?.length;
    const [removeTagMutation] = useRemoveTagMutation();
    const [removeTermMutation] = useRemoveTermMutation();
    const [tagProfileDrawerVisible, setTagProfileDrawerVisible] = useState(false);
    const [addTagUrn, setAddTagUrn] = useState('');

    const removeTag = (tagAssociationToRemove: TagAssociation) => {
        const tagToRemove = tagAssociationToRemove.tag;
        onOpenModal?.();
        Modal.confirm({
            title: `Do you want to remove ${tagToRemove?.name} tag?`,
            content: `Are you sure you want to remove the ${tagToRemove?.name} tag?`,
            onOk() {
                if (tagAssociationToRemove.associatedUrn || entityUrn) {
                    removeTagMutation({
                        variables: {
                            input: {
                                tagUrn: tagToRemove.urn,
                                resourceUrn: tagAssociationToRemove.associatedUrn || entityUrn || '',
                                subResource: entitySubresource,
                                subResourceType: entitySubresource ? SubResourceType.DatasetField : null,
                            },
                        },
                    })
                        .then(({ errors }) => {
                            if (!errors) {
                                message.success({ content: 'Removed Tag!', duration: 2 });
                            }
                        })
                        .then(refetch)
                        .catch((e) => {
                            message.destroy();
                            message.error({ content: `Failed to remove tag: \n ${e.message || ''}`, duration: 3 });
                        });
                }
            },
            onCancel() {},
            okText: 'Yes',
            maskClosable: true,
            closable: true,
        });
    };

    const removeTerm = (termToRemove: GlossaryTermAssociation) => {
        onOpenModal?.();
        const termName = termToRemove && entityRegistry.getDisplayName(termToRemove.term.type, termToRemove.term);
        Modal.confirm({
            title: `Do you want to remove ${termName} term?`,
            content: `Are you sure you want to remove the ${termName} term?`,
            onOk() {
                if (termToRemove.associatedUrn || entityUrn) {
                    removeTermMutation({
                        variables: {
                            input: {
                                termUrn: termToRemove.term.urn,
                                resourceUrn: termToRemove.associatedUrn || entityUrn || '',
                                subResource: entitySubresource,
                                subResourceType: entitySubresource ? SubResourceType.DatasetField : null,
                            },
                        },
                    })
                        .then(({ errors }) => {
                            if (!errors) {
                                message.success({ content: 'Removed Term!', duration: 2 });
                            }
                        })
                        .then(refetch)
                        .catch((e) => {
                            message.destroy();
                            message.error({ content: `Failed to remove term: \n ${e.message || ''}`, duration: 3 });
                        });
                }
            },
            onCancel() {},
            okText: 'Yes',
            maskClosable: true,
            closable: true,
        });
    };

    let renderedTags = 0;

    const showTagProfileDrawer = (urn: string) => {
        setTagProfileDrawerVisible(true);
        setAddTagUrn(urn);
    };

    const closeTagProfileDrawer = () => {
        setTagProfileDrawerVisible(false);
    };

    const onCloseProposalDecisionModal = (e) => {
        e.stopPropagation();
        setShowProposalDecisionModal(false);
        setTimeout(() => refetch?.(), 2000);
    };

    const onProposalAcceptance = (actionRequest: ActionRequest) => {
        acceptProposalMutation({ variables: { urn: actionRequest.urn } })
            .then(() => {
                message.success('Successfully accepted the proposal!');
            })
            .then(refetch)
            .catch((err) => {
                console.log(err);
                message.error('Failed to accept proposal. :(');
            });
    };

    const onProposalRejection = (actionRequest: ActionRequest) => {
        rejectProposalMutation({ variables: { urn: actionRequest.urn } })
            .then(() => {
                message.info('Proposal declined.');
            })
            .then(refetch)
            .catch((err) => {
                console.log(err);
                message.error('Failed to reject proposal. :(');
            });
    };

    const onActionRequestUpdate = () => {
        refetch?.();
    };

=======
    const tagsEmpty = !editableTags?.tags?.length && !uneditableTags?.tags?.length;
    const termsEmpty = !editableGlossaryTerms?.terms?.length && !uneditableGlossaryTerms?.terms?.length;

    let renderedTags = 0;

>>>>>>> oss_master
    return (
        <>
            {domain && (
                <DomainLink domain={domain} name={entityRegistry.getDisplayName(EntityType.Domain, domain) || ''} />
            )}
            {uneditableGlossaryTerms?.terms?.map((term) => {
                renderedTags += 1;
                if (maxShow && renderedTags === maxShow + 1)
                    return (
                        <TagText>
                            <Highlight matchStyle={highlightMatchStyle} search={highlightText}>
                                {uneditableGlossaryTerms?.terms
                                    ? `+${uneditableGlossaryTerms?.terms?.length - maxShow}`
                                    : null}
                            </Highlight>
                        </TagText>
                    );
                if (maxShow && renderedTags > maxShow) return null;

                return (
                    <StyledTerm
                        term={term}
                        entityUrn={entityUrn}
                        entitySubresource={entitySubresource}
                        canRemove={false}
                        readOnly={readOnly}
                        highlightText={highlightText}
                        onOpenModal={onOpenModal}
                        refetch={refetch}
                    />
                );
            })}
            {editableGlossaryTerms?.terms?.map((term) => (
<<<<<<< HEAD
                <HoverEntityTooltip entity={term.term}>
                    <TermLink
                        to={entityRegistry.getEntityUrl(EntityType.GlossaryTerm, term.term.urn)}
                        key={term.term.urn}
                    >
                        <Tag
                            style={{ cursor: 'pointer' }}
                            closable={canRemove}
                            onClose={(e) => {
                                e.preventDefault();
                                removeTerm(term);
                            }}
                        >
                            <BookOutlined style={{ marginRight: '3%' }} />
                            <Highlight
                                style={{ marginLeft: 0 }}
                                matchStyle={highlightMatchStyle}
                                search={highlightText}
                            >
                                {entityRegistry.getDisplayName(EntityType.GlossaryTerm, term.term)}
                            </Highlight>
                            {term.actor?.urn === PROPAGATOR_URN && <PropagateThunderbolt />}
                        </Tag>
                    </TermLink>
                </HoverEntityTooltip>
=======
                <StyledTerm
                    term={term}
                    entityUrn={entityUrn}
                    entitySubresource={entitySubresource}
                    canRemove={canRemove}
                    readOnly={readOnly}
                    highlightText={highlightText}
                    onOpenModal={onOpenModal}
                    refetch={refetch}
                />
>>>>>>> oss_master
            ))}
            {proposedGlossaryTerms?.map((actionRequest) => (
                <Tooltip overlay="Pending approval from owners">
                    <ProposedTerm
                        closable={false}
                        data-testid={`proposed-term-${actionRequest.params?.glossaryTermProposal?.glossaryTerm?.name}`}
                        onClick={() => {
                            setShowProposalDecisionModal(true);
                        }}
                    >
                        <BookOutlined style={{ marginRight: '3%' }} />
                        {entityRegistry.getDisplayName(
                            EntityType.GlossaryTerm,
                            actionRequest.params?.glossaryTermProposal?.glossaryTerm,
                        )}
                        <ProposalModal
                            actionRequest={actionRequest}
                            showProposalDecisionModal={showProposalDecisionModal}
                            onCloseProposalDecisionModal={onCloseProposalDecisionModal}
                            onProposalAcceptance={onProposalAcceptance}
                            onProposalRejection={onProposalRejection}
                            onActionRequestUpdate={onActionRequestUpdate}
                            elementName={entityRegistry.getDisplayName(
                                EntityType.GlossaryTerm,
                                actionRequest.params?.glossaryTermProposal?.glossaryTerm,
                            )}
                        />
                        <ClockCircleOutlined style={{ color: 'orange', marginLeft: '3%' }} />
                    </ProposedTerm>
                </Tooltip>
            ))}
            {/* uneditable tags are provided by ingestion pipelines exclusively */}
            {uneditableTags?.tags?.map((tag) => {
                renderedTags += 1;
                if (maxShow && renderedTags === maxShow + 1)
                    return (
                        <TagText>{uneditableTags?.tags ? `+${uneditableTags?.tags?.length - maxShow}` : null}</TagText>
                    );
                if (maxShow && renderedTags > maxShow) return null;

                return (
                    <Tag
                        tag={tag}
                        entityUrn={entityUrn}
                        entitySubresource={entitySubresource}
                        canRemove={false}
                        readOnly={readOnly}
                        highlightText={highlightText}
                        onOpenModal={onOpenModal}
                        refetch={refetch}
                    />
                );
            })}
            {/* editable tags may be provided by ingestion pipelines or the UI */}
            {editableTags?.tags?.map((tag) => {
                renderedTags += 1;
                if (maxShow && renderedTags > maxShow) return null;

                return (
                    <Tag
                        tag={tag}
                        entityUrn={entityUrn}
                        entitySubresource={entitySubresource}
                        canRemove={canRemove}
                        readOnly={readOnly}
                        highlightText={highlightText}
                        onOpenModal={onOpenModal}
                        refetch={refetch}
                    />
                );
            })}
<<<<<<< HEAD
            {proposedTags?.map((actionRequest) => (
                <Tooltip overlay="Pending approval from owners">
                    <StyledTag
                        data-testid={`proposed-tag-${actionRequest?.params?.tagProposal?.tag?.name}`}
                        $colorHash={actionRequest?.params?.tagProposal?.tag?.urn}
                        $color={actionRequest?.params?.tagProposal?.tag?.properties?.colorHex}
                        onClick={() => {
                            setShowProposalDecisionModal(true);
                        }}
                    >
                        {actionRequest?.params?.tagProposal?.tag?.name}
                        <ProposalModal
                            actionRequest={actionRequest}
                            showProposalDecisionModal={showProposalDecisionModal}
                            onCloseProposalDecisionModal={onCloseProposalDecisionModal}
                            onProposalAcceptance={onProposalAcceptance}
                            onProposalRejection={onProposalRejection}
                            onActionRequestUpdate={onActionRequestUpdate}
                            elementName={actionRequest?.params?.tagProposal?.tag?.name}
                        />
                        <ClockCircleOutlined style={{ color: 'orange', marginLeft: '3%' }} />
                    </StyledTag>
                </Tooltip>
            ))}
            {tagProfileDrawerVisible && (
                <TagProfileDrawer
                    closeTagProfileDrawer={closeTagProfileDrawer}
                    tagProfileDrawerVisible={tagProfileDrawerVisible}
                    urn={addTagUrn}
                />
            )}
=======
>>>>>>> oss_master
            {showEmptyMessage && canAddTag && tagsEmpty && (
                <Typography.Paragraph type="secondary">
                    {EMPTY_MESSAGES.tags.title}. {EMPTY_MESSAGES.tags.description}
                </Typography.Paragraph>
            )}
            {showEmptyMessage && canAddTerm && termsEmpty && (
                <Typography.Paragraph type="secondary">
                    {EMPTY_MESSAGES.terms.title}. {EMPTY_MESSAGES.terms.description}
                </Typography.Paragraph>
            )}
            {canAddTag && !readOnly && (
                <NoElementButton
                    type={showEmptyMessage && tagsEmpty ? 'default' : 'text'}
                    onClick={() => {
                        setAddModalType(EntityType.Tag);
                        setShowAddModal(true);
                    }}
                    {...buttonProps}
                >
                    <PlusOutlined />
                    <span>Add Tags</span>
                </NoElementButton>
            )}
            {canAddTerm && !readOnly && (
                <NoElementButton
                    type={showEmptyMessage && termsEmpty ? 'default' : 'text'}
                    onClick={() => {
                        setAddModalType(EntityType.GlossaryTerm);
                        setShowAddModal(true);
                    }}
                    {...buttonProps}
                >
                    <PlusOutlined />
                    <span>Add Terms</span>
                </NoElementButton>
            )}
            {showAddModal && !!entityUrn && !!entityType && (
                <EditTagTermsModal
                    type={addModalType}
                    visible
                    onCloseModal={() => {
                        onOpenModal?.();
                        setShowAddModal(false);
                        setTimeout(() => refetch?.(), 2000);
                    }}
                    resources={[
                        {
                            resourceUrn: entityUrn,
                            subResource: entitySubresource,
                            subResourceType: entitySubresource ? SubResourceType.DatasetField : null,
                        },
                    ]}
                    showPropose={entityType === EntityType.Dataset}
                />
            )}
        </>
    );
}
