package eu.europeana.enrichment.service;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.internal.model.OrganizationEnrichmentEntity;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.InputValue;
import eu.europeana.enrichment.utils.SearchValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Contains functionality for accessing entities from the enrichment database using {@link
 * EnrichmentDao}.
 *
 * @author Simon Tzanakis
 * @since 2020-07-16
 */
@Service
public class EnrichmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentService.class);
  private static final Set<String> ALL_2CODE_LANGUAGES = new HashSet<>();
  private static final Map<String, String> ALL_3CODE_TO_2CODE_LANGUAGES = new HashMap<>();
  private static final Pattern PATTERN_MATCHING_VERY_BROAD_TIMESPANS = Pattern
      .compile("http://semium.org/time/(ChronologicalPeriod$|Time$|(AD|BC)[1-9]x{3}$)");
  private final EnrichmentDao enrichmentDao;

  static {
    Arrays.stream(Locale.getISOLanguages()).map(Locale::new).forEach(locale -> {
      ALL_2CODE_LANGUAGES.add(locale.getLanguage());
      ALL_3CODE_TO_2CODE_LANGUAGES.put(locale.getISO3Language(), locale.getLanguage());
    });
  }

  @Autowired
  public EnrichmentService(EnrichmentDao enrichmentDao) {
    this.enrichmentDao = enrichmentDao;
  }

  /**
   * Get an enrichment by providing a list of {@link SearchValue}s.
   *
   * @param searchValues a list of structured search values with parameters
   * @return the enrichment values in a structured list
   */
  public List<EnrichmentResultBaseWrapper> enrichByEnrichmentSearchValues(List<SearchValue> searchValues) {
    final List<EnrichmentResultBaseWrapper> enrichmentBases = new ArrayList<>();
    try {
      for (SearchValue searchValue : searchValues) {
        final List<EntityType> entityTypes = searchValue.getEntityTypes();
        //Language has to be a valid 2 or 3 code, otherwise we do not use it
        final String inputValueLanguage = searchValue.getLanguage();
        final String language;
        if (inputValueLanguage != null && inputValueLanguage.length() == 3) {
          language = ALL_3CODE_TO_2CODE_LANGUAGES.get(inputValueLanguage.toLowerCase(Locale.US));
        } else if (inputValueLanguage != null && inputValueLanguage.length() == 2) {
          language = ALL_2CODE_LANGUAGES.contains(inputValueLanguage) ? inputValueLanguage : null;
        } else {
          language = null;
        }

        final String value = searchValue.getValue().toLowerCase(Locale.US);

        if(StringUtils.isBlank(value)){
          continue;
        }

        if (CollectionUtils.isEmpty(entityTypes)) {
          enrichmentBases.add(new EnrichmentResultBaseWrapper(findEnrichmentTerms(null, value, language)));
        }
        else {
          for (EntityType entityType : entityTypes) {
            enrichmentBases.add(new EnrichmentResultBaseWrapper(findEnrichmentTerms(entityType, value, language)));
          }
        }
      }
    } catch (RuntimeException e) {
      LOGGER.warn("Unable to retrieve entity from tag", e);
    }
    return enrichmentBases;
  }

  /**
   * Get an enrichment by providing a list of {@link InputValue}s.
   * @deprecated The method will be replaced with enrichByEnrichmentSearchValues
   * @param inputValues a list of structured input values with parameters
   * @return the enrichment values in a wrapped structured list
   */
  @Deprecated
  public List<Pair<String, EnrichmentBase>> enrichByInputValueList(List<InputValue> inputValues) {
    final List<Pair<String, EnrichmentBase>> enrichmentBases = new ArrayList<>();
    try {
      for (InputValue inputValue : inputValues) {
        final String originalField = inputValue.getRdfFieldName();
        final List<EntityType> entityTypes = inputValue.getEntityTypes();
        //Language has to be a valid 2 or 3 code, otherwise we do not use it
        final String inputValueLanguage = inputValue.getLanguage();
        final String language;
        if (inputValueLanguage != null && inputValueLanguage.length() == 3) {
          language = ALL_3CODE_TO_2CODE_LANGUAGES.get(inputValueLanguage.toLowerCase(Locale.US));
        } else if (inputValueLanguage != null && inputValueLanguage.length() == 2) {
          language = ALL_2CODE_LANGUAGES.contains(inputValueLanguage) ? inputValueLanguage : null;
        } else {
          language = null;
        }

        final String value = inputValue.getValue().toLowerCase(Locale.US);

        if (CollectionUtils.isEmpty(entityTypes) || StringUtils.isBlank(value)) {
          continue;
        }
        for (EntityType entityType : entityTypes) {
          findEnrichmentTerms(entityType, value, language).stream()
              .map(enrichmentBase -> new ImmutablePair<>(originalField, enrichmentBase))
              .forEach(enrichmentBases::add);
        }
      }
    } catch (RuntimeException e) {
      LOGGER.warn("Unable to retrieve entity from tag", e);
    }
    return enrichmentBases;
  }

  /**
   * Get an enrichment by providing a URI, might match owl:sameAs.
   *
   * @param uri The URI to check for match
   * @return the structured result of the enrichment
   */
  public EnrichmentBase enrichByEquivalenceValues(String uri) {
    try {
      //First check entity about, otherwise owlSameAs
      List<EnrichmentBase> foundEnrichmentBases = getEnrichmentTermsAndConvert(
          Collections.singletonList(new ImmutablePair<>(EnrichmentDao.ENTITY_ABOUT_FIELD, uri)));
      if (CollectionUtils.isEmpty(foundEnrichmentBases)) {
        foundEnrichmentBases = getEnrichmentTermsAndConvert(Collections
            .singletonList(new ImmutablePair<>(EnrichmentDao.ENTITY_OWL_SAME_AS_FIELD, uri)));
      }
      if (CollectionUtils.isNotEmpty(foundEnrichmentBases)) {
        return foundEnrichmentBases.get(0);
      }
    } catch (RuntimeException e) {
      LOGGER.warn("Unable to retrieve entity from id", e);
    }
    return null;
  }

  /**
   * Get an enrichment by providing a URI, might match owl:sameAs.
   * @deprecated This method will be replaced with enrichByEquivalenceValues
   * @param uri The URI to check for match
   * @return the structured result of the enrichment
   */
  @Deprecated
  public EnrichmentBase enrichByAboutOrOwlSameAs(String uri) {
    try {
      //First check entity about, otherwise owlSameAs
      List<EnrichmentBase> foundEnrichmentBases = getEnrichmentTermsAndConvert(
          Collections.singletonList(new ImmutablePair<>(EnrichmentDao.ENTITY_ABOUT_FIELD, uri)));
      if (CollectionUtils.isEmpty(foundEnrichmentBases)) {
        foundEnrichmentBases = getEnrichmentTermsAndConvert(Collections
            .singletonList(new ImmutablePair<>(EnrichmentDao.ENTITY_OWL_SAME_AS_FIELD, uri)));
      }
      if (CollectionUtils.isNotEmpty(foundEnrichmentBases)) {
        return foundEnrichmentBases.get(0);
      }
    } catch (RuntimeException e) {
      LOGGER.warn("Unable to retrieve entity from id", e);
    }
    return null;
  }

  /**
   * Get an enrichment by providing a URI.
   *
   * @param entityAbout The URI to check for match
   * @return the structured result of the enrichment
   */
  public EnrichmentBase enrichById(String entityAbout) {
    try {
      List<EnrichmentBase> foundEnrichmentBases = getEnrichmentTermsAndConvert(Collections
          .singletonList(new ImmutablePair<>(EnrichmentDao.ENTITY_ABOUT_FIELD, entityAbout)));
      if (CollectionUtils.isNotEmpty(foundEnrichmentBases)) {
        return foundEnrichmentBases.get(0);
      }
    } catch (RuntimeException e) {
      LOGGER.warn("Unable to retrieve entity from entityAbout", e);
    }
    return null;
  }

  /**
   * Get an enrichment by providing a URI.
   * @deprecated This method will be replaced with enrichById
   * @param entityAbout The URI to check for match
   * @return the structured result of the enrichment
   */
  @Deprecated
  public EnrichmentBase enrichByAbout(String entityAbout) {
    try {
      List<EnrichmentBase> foundEnrichmentBases = getEnrichmentTermsAndConvert(Collections
          .singletonList(new ImmutablePair<>(EnrichmentDao.ENTITY_ABOUT_FIELD, entityAbout)));
      if (CollectionUtils.isNotEmpty(foundEnrichmentBases)) {
        return foundEnrichmentBases.get(0);
      }
    } catch (RuntimeException e) {
      LOGGER.warn("Unable to retrieve entity from entityAbout", e);
    }
    return null;
  }

  private List<EnrichmentBase> getEnrichmentTermsAndConvert(
      List<Pair<String, String>> fieldNamesAndValues) {
    final List<EnrichmentTerm> enrichmentTerms = getEnrichmentTerms(fieldNamesAndValues);
    return Converter.convert(enrichmentTerms);
  }

  public List<EnrichmentTerm> getEnrichmentTerms(List<Pair<String, String>> fieldNamesAndValues) {
    final HashMap<String, List<Pair<String, String>>> fieldNameMap = new HashMap<>();
    fieldNameMap.put(null, fieldNamesAndValues);
    return enrichmentDao.getAllEnrichmentTermsByFields(fieldNameMap);
  }

  private List<EnrichmentBase> findEnrichmentTerms(EntityType entityType, String termLabel,
      String termLanguage) {

    final HashMap<String, List<Pair<String, String>>> fieldNameMap = new HashMap<>();
    //Find all terms that match label and language. Order of Pairs matter for the query performance.
    final List<Pair<String, String>> labelInfosFields = new ArrayList<>();
    labelInfosFields.add(new ImmutablePair<>(EnrichmentDao.LABEL_FIELD, termLabel));
    //If language not defined we are searching without specifying the language
    if (StringUtils.isNotBlank(termLanguage)) {
      labelInfosFields.add(new ImmutablePair<>(EnrichmentDao.LANG_FIELD, termLanguage));
    }

    final List<Pair<String, String>> enrichmentTermFields = new ArrayList<>();

    if(entityType != null) {
      enrichmentTermFields
          .add(new ImmutablePair<>(EnrichmentDao.ENTITY_TYPE_FIELD, entityType.name()));
    }
    fieldNameMap.put(EnrichmentDao.LABEL_INFOS_FIELD, labelInfosFields);
    fieldNameMap.put(null, enrichmentTermFields);
    final List<EnrichmentTerm> enrichmentTerms = enrichmentDao
        .getAllEnrichmentTermsByFields(fieldNameMap);
    final List<EnrichmentTerm> parentEnrichmentTerms = enrichmentTerms.stream()
        .map(this::findParentEntities).flatMap(List::stream)
        .collect(Collectors.toList());

    final List<EnrichmentBase> enrichmentBases = new ArrayList<>();
    //Convert to EnrichmentBases
    enrichmentBases.addAll(Converter.convert(enrichmentTerms));
    enrichmentBases.addAll(Converter.convert(parentEnrichmentTerms));

    return enrichmentBases;
  }

  private List<EnrichmentTerm> findParentEntities(EnrichmentTerm enrichmentTerm) {
    Set<String> parentAbouts = findParentAbouts(enrichmentTerm);
    //Do not get entities for very broad TIMESPAN
      parentAbouts = parentAbouts.stream().filter(
          parentAbout -> !PATTERN_MATCHING_VERY_BROAD_TIMESPANS.matcher(parentAbout).matches())
          .collect(Collectors.toSet());


    final List<Pair<String, List<String>>> fieldNamesAndValues = new ArrayList<>();
    fieldNamesAndValues
        .add(new ImmutablePair<>(EnrichmentDao.ENTITY_ABOUT_FIELD, new ArrayList<>(parentAbouts)));
    return enrichmentDao.getAllEnrichmentTermsByFieldsInList(fieldNamesAndValues);
  }

  private Set<String> findParentAbouts(EnrichmentTerm enrichmentTerm) {
    final Set<String> parentEntities = new HashSet<>();
    EnrichmentTerm currentEnrichmentTerm = enrichmentTerm;
    while (StringUtils.isNotBlank(currentEnrichmentTerm.getParent())) {
      currentEnrichmentTerm = enrichmentDao
          .getEnrichmentTermByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
              currentEnrichmentTerm.getParent()).orElse(null);
      //Break when there is no other parent available or when we have already encountered the
      // same about
      if (currentEnrichmentTerm == null || !parentEntities
          .add(currentEnrichmentTerm.getEnrichmentEntity().getAbout())) {
        break;
      }
    }
    return parentEntities;
  }

  /* --- Organization specific methods, used by the annotations api --- */

  /**
   * Save an organization to the database
   *
   * @param organizationEnrichmentEntity the organization to save
   * @param created the created date to be used
   * @param updated the updated date to be used
   * @return the saved organization
   */
  public OrganizationEnrichmentEntity saveOrganization(
      OrganizationEnrichmentEntity organizationEnrichmentEntity, Date created, Date updated) {

    final EnrichmentTerm enrichmentTerm = EntityConverterUtils
        .organizationImplToEnrichmentTerm(organizationEnrichmentEntity, created, updated);

    final Optional<ObjectId> objectId = enrichmentDao
        .getEnrichmentTermObjectIdByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
            organizationEnrichmentEntity.getAbout());
    objectId.ifPresent(enrichmentTerm::setId);

    //Save term list
    final String id = enrichmentDao.saveEnrichmentTerm(enrichmentTerm);
    return enrichmentDao.getEnrichmentTermByField(EnrichmentDao.ID_FIELD, id)
        .map(EnrichmentTerm::getEnrichmentEntity).map(OrganizationEnrichmentEntity.class::cast)
        .orElse(null);
  }

  /**
   * Return the list of ids for existing organizations from database
   *
   * @param organizationIds The organization ids to check existence
   * @return list of ids of existing organizations
   */
  public List<String> findExistingOrganizations(List<String> organizationIds) {
    List<String> existingOrganizationIds = new ArrayList<>();
    for (String id : organizationIds) {
      Optional<OrganizationEnrichmentEntity> organization = getOrganizationByUri(id);
      organization.ifPresent(value -> existingOrganizationIds.add(value.getAbout()));
    }
    return existingOrganizationIds;
  }

  /**
   * Get an organization by uri
   *
   * @param uri The EDM organization uri
   * @return OrganizationImpl object
   */
  public Optional<OrganizationEnrichmentEntity> getOrganizationByUri(String uri) {
    final List<EnrichmentTerm> enrichmentTerm = getEnrichmentTerms(
        Collections.singletonList(new ImmutablePair<>(EnrichmentDao.ENTITY_ABOUT_FIELD, uri)));
    return enrichmentTerm.stream().findFirst().map(EnrichmentTerm::getEnrichmentEntity)
        .map(OrganizationEnrichmentEntity.class::cast);
  }

  /**
   * Delete organizations from database by given organization ids
   *
   * @param organizationIds The organization ids
   */
  public void deleteOrganizations(List<String> organizationIds) {
    enrichmentDao.deleteEnrichmentTerms(EntityType.ORGANIZATION, organizationIds);
  }

  /**
   * This method removes organization from database by given organization id.
   *
   * @param organizationId The organization id
   */
  public void deleteOrganization(String organizationId) {
    deleteOrganizations(Collections.singletonList(organizationId));
  }


  /**
   * Get the date of the latest updated organization.
   *
   * @return the date of the latest updated organization
   */
  public Date getDateOfLastUpdatedOrganization() {
    return enrichmentDao.getDateOfLastUpdatedEnrichmentTerm(EntityType.ORGANIZATION);
  }
}
