import { Typography, Image } from 'antd';
import React from 'react';
import { Link } from 'react-router-dom';
import styled from 'styled-components';

import { EntityType } from '../../../../../../types.generated';
import useIsLineageMode from '../../../../../lineage/utils/useIsLineageMode';
import { capitalizeFirstLetter } from '../../../../../shared/capitalizeFirstLetter';
import { useEntityRegistry } from '../../../../../useEntityRegistry';
import { GenericEntityProperties } from '../../../types';

type Props = {
    urn: string;
    entityType: EntityType;
    entityData: GenericEntityProperties | null;
};

const PreviewImage = styled(Image)`
    max-height: 20px;
    padding-top: 3px;
    width: auto;
    object-fit: contain;
`;

export const EntityHeader = ({ urn, entityData, entityType }: Props) => {
    const entityRegistry = useEntityRegistry();
    const platformName = capitalizeFirstLetter(entityData?.platform?.name);
    const platformLogoUrl = entityData?.platform?.info?.logoUrl;
    const isLineageMode = useIsLineageMode();
    const entityTypeCased = entityType[0] + entityType.slice(1).toLowerCase();

    return (
        <div>
            <div>
                <span>
                    {!!platformLogoUrl && (
                        <PreviewImage preview={false} src={platformLogoUrl} placeholder alt={platformName} />
                    )}
                </span>
                <Typography.Text style={{ fontSize: 16 }}>{platformName}</Typography.Text>|
                <Typography.Text style={{ fontSize: 16 }}>{entityTypeCased}</Typography.Text>
            </div>
            <Link to={`/${entityRegistry.getPathName(entityType)}/${urn}?is_lineage_mode=${isLineageMode}`}>
                <Typography.Text style={{ fontSize: 22 }}>{entityData?.name}</Typography.Text>
            </Link>
        </div>
    );
};
