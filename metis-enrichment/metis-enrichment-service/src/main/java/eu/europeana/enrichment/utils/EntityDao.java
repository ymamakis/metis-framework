package eu.europeana.enrichment.utils;

import com.mongodb.MongoClient;
import dev.morphia.AdvancedDatastore;
import dev.morphia.Key;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;
import eu.europeana.corelib.solr.entity.ContextualClassImpl;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;

/**
 * @author Simon Tzanakis
 * @since 2020-07-07
 */
public class EntityDao {

  private final AdvancedDatastore datastore;

  public static final String ID_FIELD = "_id";
  private static final String ENTITY_TYPE_FIELD = "entityType";
  public static final String CODE_URI_FIELD = "codeUri";
  private static final String MODIFIED_FIELD = "modified";
  private static final String OWL_SAME_AS = "owlSameAs";

  //Entity types
  private static final String CONCEPT_TYPE = "ConceptImpl";
  private static final String PLACE_TYPE = "PlaceImpl";
  private static final String AGENT_TYPE = "AgentImpl";
  private static final String TIMESPAN_TYPE = "TimespanImpl";
  public static final String ORGANIZATION_TYPE = "OrganizationImpl";

  //Table names
  private static final String AGENT_TABLE = "people";
  private static final String CONCEPT_TABLE = "concept";
  private static final String PLACE_TABLE = "place";
  private static final String TIMESPAN_TABLE = "period";
  public static final String ORGANIZATION_TABLE = "organization";
  private final MongoClient mongoClient;


  public EntityDao(MongoClient mongoClient, String databaseName) {
    this.mongoClient = mongoClient;

    // Register the mappings and set up the data store.
    final Morphia morphia = new Morphia();
    morphia.map(MongoTermList.class);
    morphia.map(AgentTermList.class);
    morphia.map(ConceptTermList.class);
    morphia.map(PlaceTermList.class);
    morphia.map(TimespanTermList.class);
    morphia.map(OrganizationTermList.class);
    this.datastore = (AdvancedDatastore) morphia.createDatastore(this.mongoClient, databaseName);
  }

  public <T extends MongoTermList<S>, S extends AbstractEdmEntityImpl> MongoTermList<S> findTermListByField(
      Class<T> mongoTermListType, String fieldName, String fieldValue) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> this.datastore.find(mongoTermListType).filter(fieldName, fieldValue).first());
  }


  private MongoTerm findMongoTermByField(String entityType, String fieldName, String fieldValue) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> this.datastore.find(entityType, MongoTerm.class).filter(fieldName, fieldValue)
            .first());
  }

  public List<MongoTerm> getAllMongoTerms(EntityType entityType) {
    Query<MongoTerm> query = this.datastore.createQuery(getTableName(entityType), MongoTerm.class);
    return getListOfQuery(query);
  }

  public Date getDateOfLastModifiedEntity(EntityType entityType) {
    Query<MongoTermList> query = datastore.createQuery(MongoTermList.class);
    query.filter(ENTITY_TYPE_FIELD, getEntityType(entityType));
    query.order(Sort.descending(MODIFIED_FIELD));
    final MongoTermList mongoTermList = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(query::first);

    Date dateModified = null;
    if (Objects.nonNull(mongoTermList)) {
      dateModified = mongoTermList.getModified();
    }
    return dateModified;
  }

  public String saveTermList(MongoTermList<? extends AbstractEdmEntityImpl> mongoTermList) {
    Key<MongoTermList<? extends AbstractEdmEntityImpl>> datasetKey = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> this.datastore.save(mongoTermList));
    return datasetKey == null ? StringUtils.EMPTY : datasetKey.getId().toString();
  }

  public void saveMongoTerms(List<MongoTerm> mongoTerms, EntityType entityType) {
    ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> this.datastore.save(getTableName(entityType), mongoTerms));
  }

  public int storeMongoTermsFromEntity(ContextualClassImpl entity, EntityType entityType) {
    List<MongoTerm> mongoTerms = createListOfMongoTerms(entity);
    saveMongoTerms(mongoTerms, entityType);
    return mongoTerms.size();
  }


  private static List<MongoTerm> createListOfMongoTerms(ContextualClassImpl entity) {
    MongoTerm mongoTerm;
    List<MongoTerm> mongoTerms = new ArrayList<>();

    for (Map.Entry<String, List<String>> prefLabel : entity.getPrefLabel().entrySet()) {
      for (String label : prefLabel.getValue()) {
        mongoTerm = new MongoTerm();
        mongoTerm.setCodeUri(entity.getAbout());
        mongoTerm.setLabel(label.toLowerCase());
        mongoTerm.setOriginalLabel(label);
        mongoTerm.setLang(prefLabel.getKey());
        mongoTerms.add(mongoTerm);
      }
    }
    return mongoTerms;
  }

  public List<String> deleteAllEntitiesMatching(List<String> codeUris) {
    List<String> removedCodeUris = new ArrayList<>();
    for (String codeUri : codeUris) {
      removedCodeUris.add(codeUri);
      removedCodeUris.addAll(deleteEntities(getTableName(EntityType.PLACE), PLACE_TYPE, codeUri));
      removedCodeUris
          .addAll(deleteEntities(getTableName(EntityType.CONCEPT), CONCEPT_TYPE, codeUri));
      removedCodeUris.addAll(deleteEntities(getTableName(EntityType.AGENT), AGENT_TYPE, codeUri));
      removedCodeUris
          .addAll(deleteEntities(getTableName(EntityType.TIMESPAN), TIMESPAN_TYPE, codeUri));
      removedCodeUris.addAll(
          deleteEntities(getTableName(EntityType.ORGANIZATION), ORGANIZATION_TYPE, codeUri));
    }
    return removedCodeUris;
  }

  public List<String> deleteEntities(String entityTable, String entityType, String codeUri) {
    List<String> extraUrisRemoved = new ArrayList<>();
    //Remove from Term List
    deleteMongoTermList(codeUri);
    //Remove from specific collection
    deleteMongoTerm(entityTable, codeUri);

    //Find all TermLists that have owlSameAs equals with codeUri
    final Query<MongoTermList> termListsSameAsQuery = this.datastore
        .createQuery(MongoTermList.class).filter(ENTITY_TYPE_FIELD, entityType)
        .filter(OWL_SAME_AS, codeUri);
    final List<MongoTermList> allTermListsSameAs = getListOfQuery(termListsSameAsQuery);
    for (MongoTermList mongoTermList : allTermListsSameAs) {
      final String sameAsCodeUri = mongoTermList.getCodeUri();
      extraUrisRemoved.add(sameAsCodeUri);
      //Remove from Term List
      deleteMongoTermList(codeUri);
      //Remove from specific collection
      deleteMongoTerm(entityTable, sameAsCodeUri);
    }
    return extraUrisRemoved;
  }

  public void deleteMongoTerm(String entityTable, String codeUri) {
    final MongoTerm mongoTermCoreUri = findMongoTermByField(entityTable, CODE_URI_FIELD, codeUri);
    ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> this.datastore.delete(entityTable, MongoTerm.class, mongoTermCoreUri.getId()));
  }

  private void deleteMongoTermList(String codeUri) {
    ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> this.datastore.delete(
            this.datastore.createQuery(MongoTermList.class).filter(CODE_URI_FIELD, codeUri)));
  }

  private String getTableName(EntityType entityType) {
    final String result;
    switch (entityType) {
      case AGENT:
        result = AGENT_TABLE;
        break;
      case CONCEPT:
        result = CONCEPT_TABLE;
        break;
      case PLACE:
        result = PLACE_TABLE;
        break;
      case TIMESPAN:
        result = TIMESPAN_TABLE;
        break;
      case ORGANIZATION:
        result = ORGANIZATION_TABLE;
        break;
      default:
        throw new IllegalStateException("Unknown entity: " + entityType);
    }
    return result;
  }

  private String getEntityType(EntityType entityType) {
    final String result;
    switch (entityType) {
      case AGENT:
        result = AGENT_TYPE;
        break;
      case CONCEPT:
        result = CONCEPT_TYPE;
        break;
      case PLACE:
        result = PLACE_TYPE;
        break;
      case TIMESPAN:
        result = TIMESPAN_TYPE;
        break;
      case ORGANIZATION:
        result = ORGANIZATION_TYPE;
        break;
      default:
        throw new IllegalStateException("Unknown entity: " + entityType);
    }
    return result;
  }

  private <T> List<T> getListOfQuery(Query<T> query) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> {
      try (MorphiaCursor<T> cursor = query.find()) {
        return cursor.toList();
      }
    });
  }

  public void close() {
    this.mongoClient.close();
  }

}