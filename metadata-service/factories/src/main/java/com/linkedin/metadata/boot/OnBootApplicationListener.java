package com.linkedin.metadata.boot;

import javax.annotation.Nonnull;

import com.linkedin.metadata.version.GitVersion;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;


/**
 * Responsible for coordinating starting steps that happen before the application starts up.
 */
@Slf4j
@Component
public class OnBootApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

  private static final String ROOT_WEB_APPLICATION_CONTEXT_ID = String.format("%s:", WebApplicationContext.class.getName());

  @Autowired
  @Qualifier("bootstrapManager")
  private BootstrapManager _bootstrapManager;

  @Autowired
  @Qualifier("gitVersion")
  private GitVersion gitVersion;

  @Value("${sentry.enabled}")
  private Boolean sentryEnabled;

  @Value("${sentry.dsn}")
  private String sentryDsn;

  @Value("${sentry.env}")
  private String sentryEnv;

  @Value("${sentry.debug}")
  private Boolean sentryDebug;

  @Override
  public void onApplicationEvent(@Nonnull ContextRefreshedEvent event) {
    if (ROOT_WEB_APPLICATION_CONTEXT_ID.equals(event.getApplicationContext().getId())) {
      if (sentryEnabled) {
        Sentry.init(options -> {
          options.setDsn(sentryDsn);
          options.setRelease(gitVersion.getVersion());
          options.setEnvironment(sentryEnv);
          options.setTracesSampleRate(0.0);
          options.setDebug(sentryDebug);
        });
        if (sentryDebug) {
          try {
            throw new Exception("This is a test.");
          } catch (Exception e) {
            Sentry.captureException(e);
          }
        }
      }
      _bootstrapManager.start();
    }
  }
}
