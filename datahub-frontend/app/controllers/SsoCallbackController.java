package controllers;

import auth.sso.SsoManager;
import auth.sso.SsoProvider;
import auth.sso.oidc.OidcCallbackLogic;
import client.AuthServiceClient;
import com.datahub.authentication.Authentication;
import com.linkedin.entity.client.EntityClient;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.play.CallbackController;
import org.pac4j.play.PlayWebContext;
import play.mvc.Result;


/**
 * A dedicated Controller for handling redirects to DataHub by 3rd-party Identity Providers after
 * off-platform authentication.
 *
 * Handles a single "callback/{protocol}" route, where the protocol (ie. OIDC / SAML) determines
 * the handling logic to invoke.
 */
@Slf4j
public class SsoCallbackController extends CallbackController {

  private final SsoManager _ssoManager;
  private final Config _config;

  @Inject
  public SsoCallbackController(@Nonnull SsoManager ssoManager, @Nonnull Authentication systemAuthentication,
      @Nonnull EntityClient entityClient, @Nonnull AuthServiceClient authClient, @Nonnull Config config) {
    _ssoManager = ssoManager;
    _config = config;
    setDefaultUrl("/"); // By default, redirects to Home Page on log in.
    setCallbackLogic(new SsoCallbackLogic(ssoManager, systemAuthentication, entityClient, authClient));
  }

  public CompletionStage<Result> handleCallback(String protocol) {
    if (shouldHandleCallback(protocol)) {
      log.debug(String.format("Handling SSO callback. Protocol: %s", protocol));
      return callback().handle((res, e) -> {
        if (e != null) {
          log.error("Caught exception while attempting to handle SSO callback! It's likely that SSO integration is mis-configured.", e);
          return redirect(
              String.format("/login?error_msg=%s",
                  URLEncoder.encode("Failed to sign in using Single Sign-On provider. Please contact your DataHub Administrator, "
                      + "or refer to server logs for more information.")));
        }
        return res;
      });
    }
    return CompletableFuture.completedFuture(internalServerError(
        String.format("Failed to perform SSO callback. SSO is not enabled for protocol: %s", protocol)));
  }


  /**
   * Logic responsible for delegating to protocol-specific callback logic.
   */
  public class SsoCallbackLogic implements CallbackLogic<Result, PlayWebContext> {

    private final OidcCallbackLogic _oidcCallbackLogic;

    SsoCallbackLogic(final SsoManager ssoManager, final Authentication systemAuthentication,
        final EntityClient entityClient, final AuthServiceClient authClient) {
      _oidcCallbackLogic = new OidcCallbackLogic(ssoManager, systemAuthentication, entityClient, authClient);
    }

    @Override
    public Result perform(PlayWebContext context, Config config,
        HttpActionAdapter<Result, PlayWebContext> httpActionAdapter, String defaultUrl, Boolean saveInSession,
        Boolean multiProfile, Boolean renewSession, String defaultClient) {
      if (SsoProvider.SsoProtocol.OIDC.equals(_ssoManager.getSsoProvider().protocol())) {
        return _oidcCallbackLogic.perform(context, config, httpActionAdapter, defaultUrl, saveInSession, multiProfile,
            renewSession, defaultClient);
      }
      // Should never occur.
      throw new UnsupportedOperationException("Failed to find matching SSO Provider. Only one supported is OIDC.");
    }
  }

  private boolean shouldHandleCallback(final String protocol) {
    if (!_ssoManager.isSsoEnabled()) {
      return false;
    }
    updateConfig();
    return _ssoManager.getSsoProvider().protocol().getCommonName().equals(protocol);
  }

  private void updateConfig() {
    final Clients clients = new Clients();
    final List<Client> clientList = new ArrayList<>();
    clientList.add(_ssoManager.getSsoProvider().client());
    clients.setClients(clientList);
    _config.setClients(clients);
  }
}
