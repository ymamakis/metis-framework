package eu.europeana.enrichment.service;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.internal.model.AgentEnrichmentEntity;
import eu.europeana.enrichment.internal.model.ConceptEnrichmentEntity;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.internal.model.OrganizationEnrichmentEntity;
import eu.europeana.enrichment.internal.model.PlaceEnrichmentEntity;
import eu.europeana.enrichment.internal.model.TimespanEnrichmentEntity;
import eu.europeana.enrichment.utils.EntityType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

/**
 * Contains functionality for converting from an incoming Object to a different one.
 */
public final class Converter {

  private Converter() {
  }

  public static List<EnrichmentBase> convert(List<EnrichmentTerm> enrichmentTerms) {
    return enrichmentTerms.stream().map(Converter::convert).collect(Collectors.toList());
  }

  public static EnrichmentBase convert(EnrichmentTerm enrichmentTerm) {
    final EntityType entityType = enrichmentTerm.getEntityType();
    if (entityType == null) {
      return null;
    }
    final EnrichmentBase result;
    switch (entityType) {
      case AGENT:
        result = convertAgent((AgentEnrichmentEntity) enrichmentTerm.getEnrichmentEntity());
        break;
      case CONCEPT:
        result = convertConcept((ConceptEnrichmentEntity) enrichmentTerm.getEnrichmentEntity());
        break;
      case PLACE:
        result = convertPlace((PlaceEnrichmentEntity) enrichmentTerm.getEnrichmentEntity());
        break;
      case TIMESPAN:
        result = convertTimespan((TimespanEnrichmentEntity) enrichmentTerm.getEnrichmentEntity());
        break;
      default:
        result = null;
        break;
    }
    return result;
  }

  private static Timespan convertTimespan(TimespanEnrichmentEntity timespanEnrichmentEntity) {

    Timespan output = new Timespan();

    output.setAbout(timespanEnrichmentEntity.getAbout());
    output.setPrefLabelList(convert(timespanEnrichmentEntity.getPrefLabel()));
    output.setAltLabelList(convert(timespanEnrichmentEntity.getAltLabel()));
    output.setBeginList(convert(timespanEnrichmentEntity.getBegin()));
    output.setEndList(convert(timespanEnrichmentEntity.getEnd()));
    output.setHasPartsList(convertPart(timespanEnrichmentEntity.getDctermsHasPart()));
    output.setHiddenLabel(convert(timespanEnrichmentEntity.getHiddenLabel()));
    output.setNotes(convert(timespanEnrichmentEntity.getNote()));
    output.setSameAs(convertToPartsList(timespanEnrichmentEntity.getOwlSameAs()));

    if (StringUtils.isNotBlank(timespanEnrichmentEntity.getIsPartOf())) {
      output.setIsPartOf(new Part(timespanEnrichmentEntity.getIsPartOf()));
    }

    if (StringUtils.isNotBlank(timespanEnrichmentEntity.getIsNextInSequence())) {
      output.setIsNextInSequence(new Part(timespanEnrichmentEntity.getIsNextInSequence()));
    }

    return output;
  }

  private static Concept convertConcept(ConceptEnrichmentEntity conceptEnrichmentEntity) {
    Concept output = new Concept();

    output.setAbout(conceptEnrichmentEntity.getAbout());
    output.setPrefLabelList(convert(conceptEnrichmentEntity.getPrefLabel()));
    output.setAltLabelList(convert(conceptEnrichmentEntity.getAltLabel()));
    output.setHiddenLabel(convert(conceptEnrichmentEntity.getHiddenLabel()));
    output.setNotation(convert(conceptEnrichmentEntity.getNotation()));
    output.setNotes(convert(conceptEnrichmentEntity.getNote()));
    output.setBroader(convertToResourceList(conceptEnrichmentEntity.getBroader()));
    output.setBroadMatch(convertToResourceList(conceptEnrichmentEntity.getBroadMatch()));
    output.setCloseMatch(convertToResourceList(conceptEnrichmentEntity.getCloseMatch()));
    output.setExactMatch(convertToResourceList(conceptEnrichmentEntity.getExactMatch()));
    output.setInScheme(convertToResourceList(conceptEnrichmentEntity.getInScheme()));
    output.setNarrower(convertToResourceList(conceptEnrichmentEntity.getNarrower()));
    output.setNarrowMatch(convertToResourceList(conceptEnrichmentEntity.getNarrowMatch()));
    output.setRelated(convertToResourceList(conceptEnrichmentEntity.getRelated()));
    output.setRelatedMatch(convertToResourceList(conceptEnrichmentEntity.getRelatedMatch()));

    return output;
  }


  private static Place convertPlace(PlaceEnrichmentEntity placeEnrichmentEntity) {

    Place output = new Place();

    output.setAbout(placeEnrichmentEntity.getAbout());
    output.setPrefLabelList(convert(placeEnrichmentEntity.getPrefLabel()));
    output.setAltLabelList(convert(placeEnrichmentEntity.getAltLabel()));

    output.setHasPartsList(convertPart(placeEnrichmentEntity.getDcTermsHasPart()));
    output.setNotes(convert(placeEnrichmentEntity.getNote()));
    output.setSameAs(convertToPartsList(placeEnrichmentEntity.getOwlSameAs()));

    if (StringUtils.isNotBlank(placeEnrichmentEntity.getIsPartOf())) {
      output.setIsPartOf(new Part(placeEnrichmentEntity.getIsPartOf()));
    }
    if ((placeEnrichmentEntity.getLatitude() != null && placeEnrichmentEntity.getLatitude() != 0)
        && (placeEnrichmentEntity.getLongitude() != null
        && placeEnrichmentEntity.getLongitude() != 0)) {
      output.setLat(placeEnrichmentEntity.getLatitude().toString());
      output.setLon(placeEnrichmentEntity.getLongitude().toString());
    }

    if (placeEnrichmentEntity.getAltitude() != null && placeEnrichmentEntity.getAltitude() != 0) {
      output.setAlt(placeEnrichmentEntity.getAltitude().toString());
    }
    return output;
  }

  private static Agent convertAgent(AgentEnrichmentEntity agentEntityEnrichment) {

    Agent output = new Agent();

    output.setAbout(agentEntityEnrichment.getAbout());
    output.setPrefLabelList(convert(agentEntityEnrichment.getPrefLabel()));
    output.setAltLabelList(convert(agentEntityEnrichment.getAltLabel()));
    output.setHiddenLabel(convert(agentEntityEnrichment.getHiddenLabel()));
    output.setFoafName(convert(agentEntityEnrichment.getFoafName()));
    output.setNotes(convert(agentEntityEnrichment.getNote()));

    output.setBeginList(convert(agentEntityEnrichment.getBegin()));
    output.setEndList(convert(agentEntityEnrichment.getEnd()));

    output.setIdentifier(convert(agentEntityEnrichment.getDcIdentifier()));
    output.setHasMet(convert(agentEntityEnrichment.getEdmHasMet()));
    output.setBiographicaInformation(
        convert(agentEntityEnrichment.getRdaGr2BiographicalInformation()));
    output.setPlaceOfBirth(convertResourceOrLiteral(agentEntityEnrichment.getRdaGr2PlaceOfBirth()));
    output.setPlaceOfDeath(convertResourceOrLiteral(agentEntityEnrichment.getRdaGr2PlaceOfDeath()));
    output.setDateOfBirth(convert(agentEntityEnrichment.getRdaGr2DateOfBirth()));
    output.setDateOfDeath(convert(agentEntityEnrichment.getRdaGr2DateOfDeath()));
    output.setDateOfEstablishment(convert(agentEntityEnrichment.getRdaGr2DateOfEstablishment()));
    output.setDateOfTermination(convert(agentEntityEnrichment.getRdaGr2DateOfTermination()));
    output.setGender(convert(agentEntityEnrichment.getRdaGr2Gender()));

    output.setDate(convertResourceOrLiteral(agentEntityEnrichment.getDcDate()));
    output.setProfessionOrOccupation(
        convertResourceOrLiteral(agentEntityEnrichment.getRdaGr2ProfessionOrOccupation()));

    output.setWasPresentAt(convertToResourceList(agentEntityEnrichment.getEdmWasPresentAt()));
    output.setSameAs(convertToPartsList(agentEntityEnrichment.getOwlSameAs()));

    return output;
  }

  static EnrichmentTerm organizationImplToEnrichmentTerm(
      OrganizationEnrichmentEntity organizationEnrichmentEntity, Date created, Date updated) {
    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
    enrichmentTerm.setEnrichmentEntity(organizationEnrichmentEntity);
    enrichmentTerm.setEntityType(EntityType.ORGANIZATION);
    enrichmentTerm.setCreated(Objects.requireNonNullElseGet(created, Date::new));
    enrichmentTerm.setUpdated(updated);

    return enrichmentTerm;
  }

  private static List<Label> convert(Map<String, List<String>> map) {
    List<Label> labels = new ArrayList<>();
    if (map == null) {
      return labels;
    }
    map.forEach(
        (key, entry) -> entry.stream().map(value -> new Label(key, value)).forEach(labels::add));
    return labels;
  }

  private static List<Part> convertPart(Map<String, List<String>> map) {
    List<Part> parts = new ArrayList<>();
    if (map == null) {
      return parts;
    }
    map.forEach((key, entry) -> entry.stream().map(Part::new).forEach(parts::add));
    return parts;
  }

  private static List<LabelResource> convertResourceOrLiteral(Map<String, List<String>> map) {
    List<LabelResource> parts = new ArrayList<>();
    if (map == null) {
      return parts;
    }
    map.forEach((key, entry) -> entry.stream()
        .map(value -> (isUri(key) ? new LabelResource(key) : new LabelResource(key, value)))
        .forEach(parts::add));
    return parts;
  }

  private static List<Resource> convertToResourceList(String[] resources) {
    if (resources == null) {
      return new ArrayList<>();
    }
    return Arrays.stream(resources).map(Resource::new).collect(Collectors.toList());
  }

  private static List<Part> convertToPartsList(List<String> resources) {
    if (resources == null) {
      return new ArrayList<>();
    }
    return resources.stream().map(Part::new).collect(Collectors.toList());
  }

  private static boolean isUri(String str) {
    return str.startsWith("http://");
  }
}
