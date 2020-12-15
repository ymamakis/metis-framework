package eu.europeana.enrichment.rest.client.enrichment;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermContext;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.api.internal.FieldType;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.metis.schema.jibx.RDF;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnricherImpl implements Enricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnricherImpl.class);

  private final EntityMergeEngine entityMergeEngine;
  private final RemoteEntityResolver remoteEntityResolver;

  public EnricherImpl(EntityMergeEngine entityMergeEngine,
      RemoteEntityResolver remoteEntityResolver) {
    this.entityMergeEngine = entityMergeEngine;
    this.remoteEntityResolver = remoteEntityResolver;
  }


  @Override
  public void enrichment(RDF rdf) throws EnrichmentException {
    // Extract values and references from the RDF for enrichment
    LOGGER.debug("Extracting values and references from RDF for enrichment...");
    final List<Pair<SearchValue, FieldType>> valuesForEnrichment = extractValuesForEnrichment(rdf);
    final Map<String, Set<FieldType>> referencesForEnrichment = extractReferencesForEnrichment(rdf);

    // Get the information with which to enrich the RDF using the extracted values and references
    LOGGER.debug("Using extracted values and references to gather enrichment information...");
    final List<Pair<EnrichmentBase, Set<FieldType>>> valueEnrichmentInformation = enrichValues(
        valuesForEnrichment);
    final List<EnrichmentBase> referenceEnrichmentInformation = enrichReferences(
        referencesForEnrichment);

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Enrichment Information...");
    if (valueEnrichmentInformation != null) {
      for (Pair<EnrichmentBase, Set<FieldType>> pair : valueEnrichmentInformation) {
        entityMergeEngine.mergeEntities(rdf, List.of(pair.getKey()), pair.getValue());

      }
    }

    if (!referenceEnrichmentInformation.isEmpty()) {
      entityMergeEngine.mergeEntities(rdf, referenceEnrichmentInformation, referencesForEnrichment);
    }

    // Setting additional field values and set them in the RDF.
    LOGGER.debug("Setting additional data in the RDF...");
    EnrichmentUtils.setAdditionalData(rdf);

    // Done.
    LOGGER.debug("Enrichment completed.");

  }

  @Override
  public List<Pair<EnrichmentBase, Set<FieldType>>> enrichValues(
      List<Pair<SearchValue, FieldType>> valuesForEnrichment) throws EnrichmentException {
    try {
      if (CollectionUtils.isEmpty(valuesForEnrichment)) {
        return Collections.emptyList();
      }
      List<SearchTermContext> searchTerms = valuesForEnrichment.stream().map(
          pair -> new SearchTermContext(pair.getKey().getValue(), pair.getKey().getLanguage(),
              Set.of(pair.getValue()))).collect(Collectors.toList());
      Map<SearchTerm, List<EnrichmentBase>> result = retryableExternalRequestForNetworkExceptions(
          () -> remoteEntityResolver.resolveByText(Set.copyOf(searchTerms)));
      final List<Pair<EnrichmentBase, Set<FieldType>>> output = new ArrayList<>();

      for (SearchTermContext searchTerm : searchTerms) {
        Optional.ofNullable(result.get(searchTerm)).stream().flatMap(Collection::stream)
            .forEach(base -> output.add(new MutablePair<>(base, searchTerm.getFieldTypes())));
      }
      return output;

    } catch (Exception e) {
      throw new EnrichmentException("Exception occurred while trying to perform enrichment.", e);
    }
  }

  @Override
  public List<EnrichmentBase> enrichReferences(Map<String, Set<FieldType>> referencesForEnrichment)
      throws EnrichmentException {
    try {
      Set<ReferenceTerm> referenceTermSet = new HashSet<>();

      for (Map.Entry<String, Set<FieldType>> entry : referencesForEnrichment.entrySet()) {
        ReferenceTermContext value;
        try {
          value = new ReferenceTermContext(new URL(entry.getKey()), entry.getValue());
          referenceTermSet.add(value);
        } catch (MalformedURLException e) {
          LOGGER.debug(
              "There was a problem creating converting input values for enriching references");
        }
      }

      Map<ReferenceTerm, List<EnrichmentBase>> result = retryableExternalRequestForNetworkExceptions(
          () -> remoteEntityResolver.resolveByUri(referenceTermSet));

      return CollectionUtils.isEmpty(referenceTermSet) ? Collections.emptyList()
          : result.values().stream().flatMap(List::stream).filter(Objects::nonNull)
              .collect(Collectors.toList());
    } catch (Exception e) {
      throw new EnrichmentException("Exception occurred while trying to perform enrichment.", e);
    }
  }

  @Override
  public List<Pair<SearchValue, FieldType>> extractValuesForEnrichment(RDF rdf) {
    return EnrichmentUtils.extractValuesForEnrichmentFromRDF(rdf);
  }

  @Override
  public Map<String, Set<FieldType>> extractReferencesForEnrichment(RDF rdf) {
    return EnrichmentUtils.extractReferencesForEnrichmentFromRDF(rdf);

  }
}
