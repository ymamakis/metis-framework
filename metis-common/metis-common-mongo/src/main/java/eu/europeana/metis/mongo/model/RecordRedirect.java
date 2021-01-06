package eu.europeana.metis.mongo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import eu.europeana.metis.mongo.utils.ObjectIdSerializer;
import java.util.Date;
import org.bson.types.ObjectId;

/**
 * Record redirect model class.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2020-01-13
 */
@Entity(useDiscriminator = false)
public class RecordRedirect implements HasMongoObjectId {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  @Indexed
  private String newId;
  @Indexed
  private String oldId;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date timestamp;

  public RecordRedirect() {
  }

  /**
   * Constructor with all fields provided.
   *
   * @param newId the new record identifier
   * @param oldId the old record identifier
   * @param timestamp the creation timestamp of the redirect
   */
  public RecordRedirect(String newId, String oldId, Date timestamp) {
    this.newId = newId;
    this.oldId = oldId;
    this.timestamp = new Date(timestamp.getTime());
  }

  @Override
  public ObjectId getId() {
    return id;
  }

  @Override
  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getNewId() {
    return newId;
  }

  public void setNewId(String newId) {
    this.newId = newId;
  }

  public String getOldId() {
    return oldId;
  }

  public void setOldId(String oldId) {
    this.oldId = oldId;
  }

  public Date getTimestamp() {
    return timestamp == null ? null : new Date(timestamp.getTime());
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = new Date(timestamp.getTime());
  }
}
