package org.ngelmakproject.web.rest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * Utility class for HTTP headers creation.
 */
public final class HeaderUtil {

    private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);

    private HeaderUtil() {
    }

    public static HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-ngelmak-core-alert", message);
        headers.add("X-ngelmak-core-params", param);
        return headers;
    }

    public static HttpHeaders createEntityCreationAlert(String applicationName, String entityName, String param) {
        return createAlert(applicationName + "." + entityName + ".created", param);
    }

    public static HttpHeaders createEntityUpdateAlert(String applicationName, String entityName, String param) {
        return createAlert(applicationName + "." + entityName + ".updated", param);
    }

    public static HttpHeaders createEntityDeletionAlert(String applicationName, String entityName, String param) {
        return createAlert(applicationName + "." + entityName + ".deleted", param);
    }

    public static HttpHeaders createFailureAlert(String applicationName, String entityName, String errorKey, String defaultMessage) {
        log.error("Entity processing failed, {}", defaultMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-ngelmak-core-error", "error." + errorKey);
        headers.add("X-ngelmak-core-params", entityName);
        return headers;
    }
}