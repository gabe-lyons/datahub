import React from 'react';
import { Link, useHistory, useLocation } from 'react-router-dom';
import { Badge, Breadcrumb, Row } from 'antd';
import styled from 'styled-components';
import { IconBaseProps } from 'react-icons/lib';
import { VscRepoForked, VscPreview } from 'react-icons/vsc';
import { blue, grey } from '@ant-design/colors';
import { EntityType } from '../../../../../../types.generated';
import { useEntityRegistry } from '../../../../../useEntityRegistry';
import { PageRoutes } from '../../../../../../conf/Global';
import { navigateToLineageUrl } from '../../../../../lineage/utils/navigateToLineageUrl';
import useIsLineageMode from '../../../../../lineage/utils/useIsLineageMode';

type Props = {
    type: EntityType;
    path: Array<string>;
    upstreams: number;
    downstreams: number;
};

const LineageIconGroup = styled.div`
    width: 60px;
    display: flex;
    justify-content: space-between;
`;

const HoverableVscPreview = styled(({ isSelected: _, ...props }: IconBaseProps & { isSelected: boolean }) => (
    <VscPreview {...props} />
))`
    color: ${(props) => (props.isSelected ? 'black' : grey[2])};
    &:hover {
        color: ${(props) => (props.isSelected ? 'black' : blue[4])};
        cursor: pointer;
    }
`;

const HoverableVscRepoForked = styled(({ isSelected: _, ...props }: IconBaseProps & { isSelected: boolean }) => (
    <VscRepoForked {...props} />
))`
    color: ${(props) => (props.isSelected ? 'black' : grey[2])};
    &:hover {
        color: ${(props) => (props.isSelected ? 'black' : blue[4])};
        cursor: pointer;
    }
    transform: rotate(90deg);
`;

const BrowseRow = styled(Row)`
    padding: 10px;
    border-bottom: 1px solid #dcdcdc;
    background-color: ${(props) => props.theme.styles['body-background']};
    display: flex;
    justify-content: space-between;
`;

const LineageNavContainer = styled.div`
    display: inline-flex;
`;

const LineageSummary = styled.div`
    margin-left: 12px;
`;

/**
 * Responsible for rendering a clickable browse path view.
 */
// TODO(Gabe): use this everywhere
export const ProfileNavBrowsePath = ({ type, path, upstreams, downstreams }: Props): JSX.Element => {
    const entityRegistry = useEntityRegistry();
    const history = useHistory();
    const location = useLocation();
    const isLineageMode = useIsLineageMode();

    const createPartialPath = (parts: Array<string>) => {
        return parts.join('/');
    };

    const baseBrowsePath = `${PageRoutes.BROWSE}/${entityRegistry.getPathName(type)}`;

    const pathCrumbs = path.map((part, index) => (
        <Breadcrumb.Item key={`${part || index}`}>
            <Link
                to={
                    index === path.length - 1 ? '#' : `${baseBrowsePath}/${createPartialPath(path.slice(0, index + 1))}`
                }
            >
                {part}
            </Link>
        </Breadcrumb.Item>
    ));

    return (
        <BrowseRow>
            <Breadcrumb style={{ fontSize: '16px' }}>
                <Breadcrumb.Item>
                    <Link to={baseBrowsePath}>{entityRegistry.getCollectionName(type)}</Link>
                </Breadcrumb.Item>
                {pathCrumbs}
            </Breadcrumb>
            {(upstreams > 0 || downstreams > 0) && (
                <LineageNavContainer>
                    <LineageIconGroup>
                        <HoverableVscPreview
                            isSelected={!isLineageMode}
                            size={26}
                            onClick={() => navigateToLineageUrl({ location, history, isLineageMode: false })}
                        />
                        <HoverableVscRepoForked
                            size={26}
                            isSelected={isLineageMode}
                            onClick={() => navigateToLineageUrl({ location, history, isLineageMode: true })}
                        />
                    </LineageIconGroup>
                    <LineageSummary>
                        <Badge count="2 upstream, 3 downstream" />
                    </LineageSummary>
                </LineageNavContainer>
            )}
        </BrowseRow>
    );
};
