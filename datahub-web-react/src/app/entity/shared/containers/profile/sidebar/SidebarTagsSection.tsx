import { FetchResult, MutationFunctionOptions } from '@apollo/client';
import { Typography } from 'antd';
import React from 'react';
import styled from 'styled-components';

import { EntityType } from '../../../../../../types.generated';
import analytics, { EntityActionType, EventType } from '../../../../../analytics';
import TagTermGroup from '../../../../../shared/tags/TagTermGroup';
import { ANTD_GRAY } from '../../../constants';
import { GenericEntityProperties, GenericEntityUpdate } from '../../../types';

type Props<U> = {
    urn: string;
    entityType: EntityType;
    entityData: GenericEntityProperties | null;
    updateEntity: (
        options?:
            | MutationFunctionOptions<
                  U,
                  {
                      input: GenericEntityUpdate;
                  }
              >
            | undefined,
    ) => Promise<FetchResult<U, Record<string, any>, Record<string, any>>>;
};

const HeaderText = styled(Typography.Text)`
    font-weight: 700;
    font-size: 14px;
    line-height: 22px;
    color: ${ANTD_GRAY[9]};
`;

export const SidebarTagsSection = <U,>({ urn, entityData, entityType, updateEntity }: Props<U>) => {
    return (
        <div>
            <HeaderText>Tags</HeaderText>
            <TagTermGroup
                editableTags={entityData?.globalTags}
                glossaryTerms={entityData?.glossaryTerms}
                canAdd
                canRemove
                updateTags={(globalTags) => {
                    analytics.event({
                        type: EventType.EntityActionEvent,
                        actionType: EntityActionType.UpdateTags,
                        entityType,
                        entityUrn: urn,
                    });
                    return updateEntity({ variables: { input: { urn, globalTags } } });
                }}
            />
        </div>
    );
};
