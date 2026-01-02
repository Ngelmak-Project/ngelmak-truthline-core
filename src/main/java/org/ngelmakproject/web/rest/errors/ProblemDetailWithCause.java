package org.ngelmakproject.web.rest.errors;

import java.net.URI;
import java.util.Map;

/**
 * Represents detailed problem information.
 */
public class ProblemDetailWithCause {

    private final int status;
    private final URI type;
    private final String title;
    private final Map<String, Object> properties;

    public ProblemDetailWithCause(int status, URI type, String title, Map<String, Object> properties) {
        this.status = status;
        this.type = type;
        this.title = title;
        this.properties = properties;
    }

    // Getters for status, type, title, and properties

    public int getStatus() {
        return status;
    }

    public URI getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
