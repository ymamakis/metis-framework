package eu.europeana.metis.core.workflow;

import java.util.List;
import java.util.Map;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
@Entity
public class VoidOaipmhHarvestPlugin implements AbstractMetisPlugin {
  private PluginStatus pluginStatus;
  private final PluginType pluginType = PluginType.OAIPMH_HARVEST;
  private int requestedOrder;
  @Transient
  private long sleepMillis = 10000;

  private long recordsProcessed;
  private long recordsFailed;
  private long recordsCreated;
  private long recordsUpdated;
  private long recordsDeleted;

  public VoidOaipmhHarvestPlugin() {
  }

  public VoidOaipmhHarvestPlugin(long sleepMillis) {
    this.sleepMillis = sleepMillis;
  }

  @Override
  public PluginStatus getPluginStatus() {
    return pluginStatus;
  }

  public PluginType getPluginType() {
    return pluginType;
  }

  @Override
  public int getRequestedOrder() {
    return requestedOrder;
  }

  @Override
  public void setRequestedOrder(int requestedOrder) {
    this.requestedOrder = requestedOrder;
  }

  @Override
  public long getRecordsProcessed() {
    return 0;
  }

  @Override
  public void setRecordsProcessed() {

  }

  @Override
  public long getRecordsFailed() {
    return 0;
  }

  @Override
  public void setRecordsFailed() {

  }

  @Override
  public long getRecordsUpdated() {
    return 0;
  }

  @Override
  public void setRecordsUpdated() {

  }

  @Override
  public long getRecordsCreated() {
    return 0;
  }

  @Override
  public void setRecordsCreated() {

  }

  @Override
  public long getRecordsDeleted() {
    return 0;
  }

  @Override
  public void setRecordsDeleted() {

  }

  @Override
  public void setPluginStatus(PluginStatus pluginStatus) {
    this.pluginStatus = pluginStatus;
  }

  @Override
  public void setParameters(Map<String, List<String>> parameters) {

  }

  @Override
  public Map<String, List<String>> getParameters() {
    return null;
  }

  @Override
  public void execute() {
    try {
      Thread.sleep(sleepMillis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public CloudStatistics monitor(String dataseId) {
    return null;
  }
}
