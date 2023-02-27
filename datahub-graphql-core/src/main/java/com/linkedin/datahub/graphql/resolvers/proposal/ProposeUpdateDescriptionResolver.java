package com.linkedin.datahub.graphql.resolvers.proposal;

import com.datahub.authentication.proposal.ProposalService;
import com.linkedin.common.urn.CorpuserUrn;
import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.exception.AuthorizationException;
import com.linkedin.datahub.graphql.generated.DescriptionUpdateInput;
import com.linkedin.datahub.graphql.generated.SubResourceType;
import com.linkedin.metadata.Constants;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.linkedin.datahub.graphql.resolvers.ResolverUtils.*;


@Slf4j
@RequiredArgsConstructor
public class ProposeUpdateDescriptionResolver implements DataFetcher<CompletableFuture<Boolean>> {
  private final ProposalService _proposalService;

  @Override
  public CompletableFuture<Boolean> get(DataFetchingEnvironment environment) throws Exception {
    final DescriptionUpdateInput input = bindArgument(environment.getArgument("input"), DescriptionUpdateInput.class);
    final QueryContext context = environment.getContext();
    Urn resourceUrn = Urn.createFromString(input.getResourceUrn());
    String description = input.getDescription();
    String subresource = input.getSubResource();
    SubResourceType subresourceType = input.getSubResourceType();

    if (!ProposalUtils.isAuthorizedToProposeDescription(environment.getContext(), resourceUrn, subresource)) {
      throw new AuthorizationException("Unauthorized to perform this action. Please contact your DataHub administrator.");
    }

    Urn actor = CorpuserUrn.createFromString(context.getActorUrn());
    String entityType = resourceUrn.getEntityType();

    log.info("Proposing a description update. input: {}", input);
    return CompletableFuture.supplyAsync(() -> {
      try {
        switch (entityType) {
          case Constants.GLOSSARY_TERM_ENTITY_NAME:
          case Constants.GLOSSARY_NODE_ENTITY_NAME:
          case Constants.DATASET_ENTITY_NAME:
          case Constants.SCHEMA_FIELD_ENTITY_NAME:
            return _proposalService.proposeUpdateResourceDescription(actor, resourceUrn, subresource, subresourceType != null ? subresourceType.toString() : null, description,
                context.getAuthorizer());
          default:
            return _proposalService.proposeUpdateResourceDescription(actor, resourceUrn, subresource, subresourceType != null ? subresourceType.toString() : null, description,
                context.getAuthorizer());
        }
      } catch (Exception e) {
        throw new RuntimeException("Failed to update description", e);
      }
    });
  }
}
