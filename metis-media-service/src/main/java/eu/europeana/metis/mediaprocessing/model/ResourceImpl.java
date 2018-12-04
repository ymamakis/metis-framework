package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.UrlType;
import eu.europeana.metis.mediaprocessing.exception.MediaException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ResourceImpl extends TemporaryResourceFileImpl implements Resource {

  private String mimeType;
  private Set<UrlType> urlTypes;

  public ResourceImpl(RdfResourceEntry rdfResourceEntry, String mimeType)
      throws MediaException {
    super(rdfResourceEntry.getResourceUrl(), "media_resources", "media", null);
    this.mimeType = mimeType;
    this.urlTypes = new HashSet<>(rdfResourceEntry.getUrlTypes());
  }

  @Override
  public Set<UrlType> getUrlTypes() {
    return Collections.unmodifiableSet(urlTypes);
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
}
