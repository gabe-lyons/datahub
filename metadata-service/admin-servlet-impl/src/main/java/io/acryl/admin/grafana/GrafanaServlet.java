package io.acryl.admin.grafana;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@Import({GrafanaConfiguration.class})
public class GrafanaServlet extends ProxyServlet {
    @Autowired
    @Qualifier("grafanaConfig")
    private GrafanaConfiguration.Config grafanaConfig;

    @Autowired
    @Qualifier("grafanaRequiredParameters")
    private List<Map.Entry<String, String[]>> requiredParameters;

    @Autowired
    @Qualifier("grafanaDashboards")
    private Map<String, String> grafanaDashboards;

    @Autowired
    @Qualifier("grafanaAllow")
    private Set<String> grafanaAllow;

    private WebApplicationContext springContext;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        final AutowireCapableBeanFactory beanFactory = springContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(this);
        super.init(config);
    }

    @Override
    protected String getConfigParam(String key) {
        switch (key) {
            case P_TARGET_URI:
                return grafanaConfig.getGrafanaUri().toString();
            case P_PRESERVEHOST:
            case P_LOG:
                return "false";
            default:
                return super.getConfigParam(key);
        }
    }

    private boolean hasRequiredParameters(HttpServletRequest servletRequest) {
        Map<String, String[]> requestParams = servletRequest.getParameterMap();
        boolean result = requiredParameters.stream().allMatch(expected -> requestParams.containsKey(expected.getKey())
                && Arrays.equals(requestParams.get(expected.getKey()), expected.getValue())
        );

        if (!result && doLog) {
            for (Map.Entry<String, String[]> expected : requiredParameters) {
                if (!requestParams.containsKey(expected.getKey())) {
                    log("Missing key: " + expected.getKey());
                } else if (!Arrays.equals(requestParams.get(expected.getKey()), expected.getValue())) {
                    log("Key: `" + expected.getKey() + "` value mismatch: `"
                            + String.join(",", expected.getValue()) + "` != `"
                            + String.join(",", requestParams.get(expected.getKey())) + "`");
                }
            }
        }

        return result;
    }

    private Stream<String> reduceParams(Stream<Map.Entry<String, String[]>> parameters) {
        return parameters.flatMap(paramEntry -> paramEntry.getValue().length < 1 ? Stream.of(paramEntry.getKey())
                : Arrays.stream(paramEntry.getValue())
                .map(val -> !val.isEmpty() ? String.format("%s=%s", paramEntry.getKey(), val)
                        : paramEntry.getKey()));
    }

    private String buildForcedQueryParams(HttpServletRequest servletRequest, String extraQueryParams) {
        final Set<String> restricted = requiredParameters.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
        Stream<String> originParams = reduceParams(servletRequest.getParameterMap().entrySet().stream()
                .filter(paramEntry -> !restricted.contains(paramEntry.getKey())));
        Stream<String> extraParams = Arrays.stream(Optional.ofNullable(extraQueryParams).orElse("").split("&"))
                .filter(paramEntry -> !restricted.contains(paramEntry.split("&", 2)[0]));
        Stream<String> requiredParams = reduceParams(requiredParameters.stream());

        return Stream.concat(
                Stream.concat(originParams, extraParams).filter(p -> !p.isEmpty()),
                requiredParams
        ).collect(Collectors.joining("&"));
    }

    private String buildRedirect(HttpServletRequest servletRequest, String path, String queryParameters) {
        String proto = servletRequest.getHeader("X-Forwarded-Proto") != null ? servletRequest.getHeader("X-Forwarded-Proto")
                : servletRequest.getScheme();
        String host = servletRequest.getHeader("X-Forwarded-Host") != null ? servletRequest.getHeader("X-Forwarded-Host")
                : servletRequest.getRemoteHost();
        String forward = proto + "://" + host;

        StringBuilder uri = new StringBuilder(500);
        uri.append(forward);
        uri.append(servletRequest.getServletPath());
        if (doLog) {
            log("redirect: " + uri.toString());
        }
        uri.append(Optional.ofNullable(path).orElse(""));
        if (doLog) {
            log("redirect: " + uri.toString());
        }

        // Handle the query string
        uri.append('?');
        // queryString is not decoded, so we need encodeUriQuery not to encode "%" characters, to avoid double-encoding
        uri.append(encodeUriQuery(queryParameters, false));
        if (doLog) {
            log("redirect: " + uri.toString());
        }

        return uri.toString();
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws ServletException, IOException {

        if (servletRequest.getMethod().equals("GET") && servletRequest.getPathInfo().startsWith("/d/")
                && !hasRequiredParameters(servletRequest)) {
            /*
             * Enforce required parameters by redirect
             */
            if (doLog) {
                log("Enforcing parameters");
            }
            servletResponse.sendRedirect(buildRedirect(servletRequest, servletRequest.getPathInfo(),
                    buildForcedQueryParams(servletRequest, null)));
        } else if (servletRequest.getMethod().equals("GET") && grafanaDashboards
                .keySet().stream().anyMatch(vanity -> servletRequest.getPathInfo().equals(vanity))) {
            /*
             * Handle vanity endpoints
             */
            if (doLog) {
                log("Handling vanity endpoint");
            }
            String dest = grafanaDashboards.entrySet().stream()
                    .filter(vanity -> servletRequest.getPathInfo().equals(vanity.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst().get();
            String[] splitDest = dest.split("[?]", 2);
            String destPath = splitDest[0];
            String destQuery = dest.contains("?") ? splitDest[1] : null;

            servletResponse.sendRedirect(buildRedirect(servletRequest, destPath,
                    buildForcedQueryParams(servletRequest, destQuery)));
        } else if (servletRequest.getPathInfo() == null
                || grafanaAllow.stream().anyMatch(servletRequest.getPathInfo()::startsWith)) {
            super.service(servletRequest, servletResponse);
        } else if (doLog) {
            log("proxy denied: " + servletRequest.getRequestURI());
        }
    }

    @Override
    protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
        super.copyRequestHeaders(servletRequest, proxyRequest);
        proxyRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + grafanaConfig.getGrafanaToken());
        proxyRequest.setHeader(HttpHeaders.HOST, grafanaConfig.getGrafanaUri().getHost());
    }

    @Override
    protected void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse,
                                      HttpRequest proxyRequest, HttpServletRequest servletRequest)
            throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if (entity != null) {
            OutputStream servletOutputStream = servletResponse.getOutputStream();
            if (isRewritable(proxyResponse)) {
                try {
                    String body = modifyResponseBody(servletRequest, new String(entity.getContent().readAllBytes(),
                            StandardCharsets.UTF_8));
                    servletOutputStream.write(body.getBytes(StandardCharsets.UTF_8));
                } finally {
                    servletOutputStream.flush();
                }
            } else {
                // parent's default behavior
                entity.writeTo(servletOutputStream);
            }
        }
    }

    protected String modifyResponseBody(HttpServletRequest servletRequest, String body) {
        // "appSubUrl":"/admin/dashboard","appUrl":"http://localhost:3000/admin/dashboard/"
        body = body.replaceFirst("\"appUrl\":\".*?\"", "\"appUrl\":\""
                + servletRequest.getScheme() + servletRequest.getHeader(HttpHeaders.HOST)
                + servletRequest.getServletPath() + "\"");
        body = body.replaceFirst("\"appSubUrl\":\".*?\"", "\"appSubUrl\":\""
                + servletRequest.getServletPath() + "\"");
        body = body.replaceFirst("<base href=\".*?\"/>", "<base href=\""
                + servletRequest.getServletPath() + "/\"/>");
        return body;
    }

    private boolean isRewritable(HttpResponse httpResponse) {
        boolean rewriteable = false;
        Header[] contentTypeHeaders = httpResponse.getHeaders("Content-Type");
        for (Header header : contentTypeHeaders) {
            // May need to accept other types
            if (header.getValue().contains("html")) {
                rewriteable = true;
            }
        }
        return rewriteable;
    }
}
