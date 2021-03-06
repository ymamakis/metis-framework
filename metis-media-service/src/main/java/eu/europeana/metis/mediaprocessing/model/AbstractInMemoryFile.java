package eu.europeana.metis.mediaprocessing.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

/**
 * This class represents a file in memory that's associated with a resource. Please see {@link
 * ResourceRelatedFile} for more information.
 */
abstract class AbstractInMemoryFile implements ResourceRelatedFile {

  private final String resourceUrl;
  private byte[] content = new byte[0];

  /**
   * Constructor.
   *
   * @param resourceUrl The URL of the resource for which this thumbnail is generated.
   */
  protected AbstractInMemoryFile(String resourceUrl) {
    this.resourceUrl = resourceUrl;
  }

  @Override
  public String getResourceUrl() {
    return resourceUrl;
  }

  @Override
  public boolean hasContent() {
    return content.length > 0;
  }

  @Override
  public InputStream getContentStream() {
    return new ByteArrayInputStream(content);
  }

  @Override
  public Long getContentSize() {
    return (long) content.length;
  }

  @Override
  public void markAsNoContent() {
    content = new byte[0];
  }

  @Override
  public void markAsWithContent(InputStream newContent) throws IOException {
    content = IOUtils.toByteArray(newContent);
  }

  @Override
  public void close() {
    this.markAsNoContent();
  }
}
