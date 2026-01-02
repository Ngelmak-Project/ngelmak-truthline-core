package org.ngelmakproject.web.rest.errors;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class for creating ProblemDetailWithCause objects.
 */
public class ProblemDetailWithCauseBuilder {

  private final Map<String, Object> properties = new HashMap<>();
  private int status;
  private URI type;
  private String title;

  private ProblemDetailWithCauseBuilder() {
  }

  public static ProblemDetailWithCauseBuilder instance() {
    return new ProblemDetailWithCauseBuilder();
  }

  public ProblemDetailWithCauseBuilder withStatus(int status) {
    this.status = status;
    return this;
  }

  public ProblemDetailWithCauseBuilder withType(URI type) {
    this.type = type;
    return this;
  }

  public ProblemDetailWithCauseBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public ProblemDetailWithCauseBuilder withProperty(String key, Object value) {
    properties.put(key, value);
    return this;
  }

  public ProblemDetailWithCause build() {
    return new ProblemDetailWithCause(status, type, title, properties);
  }
}
