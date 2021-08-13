import { MutationFunctionOptions, FetchResult } from '@apollo/client';
import React from 'react';

import { EntityType } from '../../../../../../types.generated';
import { GenericEntityProperties, GenericEntityUpdate } from '../../../types';
import { SidebarAboutSection } from './SidebarAboutSection';
import { SidebarOwnerSection } from './SidebarOwnerSection';
import { SidebarTagsSection } from './SidebarTagsSection';

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

export const EntitySidebar = <U,>({ urn, entityData, entityType, updateEntity }: Props<U>) => {
    return (
        <div>
            <SidebarAboutSection urn={urn} entityData={entityData} entityType={entityType} />
            <SidebarOwnerSection urn={urn} entityData={entityData} entityType={entityType} />
            <SidebarTagsSection urn={urn} entityData={entityData} entityType={entityType} updateEntity={updateEntity} />
        </div>
    );
};
