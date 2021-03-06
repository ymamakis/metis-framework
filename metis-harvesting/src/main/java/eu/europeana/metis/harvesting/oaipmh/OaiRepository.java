package eu.europeana.metis.harvesting.oaipmh;

import java.io.Serializable;

/**
 * Immutable object representing an OAI-PMH repository.
 */
public class OaiRepository implements Serializable {

  private static final long serialVersionUID = -7857963246782477550L;

  private final String repositoryUrl;
  private final String metadataPrefix;

  /**
   * Constructor.
   *
   * @param repositoryUrl The base url of the repository.
   * @param metadataPrefix The metadata prefix (optional).
   */
  public OaiRepository(String repositoryUrl, String metadataPrefix) {
    this.repositoryUrl = repositoryUrl;
    this.metadataPrefix = metadataPrefix;
  }

  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  public String getMetadataPrefix() {
    return metadataPrefix;
  }

  @Override
  public String toString() {
    return "OaiRepository{" +
            "repositoryUrl='" + repositoryUrl + '\'' +
            ", metadataPrefix='" + metadataPrefix + '\'' +
            '}';
  }
}
