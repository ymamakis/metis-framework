package eu.europeana.metis.mediaprocessing.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnrichedRdfImplTest {

  private static RDF rdf;
  private static EnrichedRdfImpl enrichedRdf;

  @BeforeAll
  static void createMocks() {
    rdf = spy(new RDF());
    enrichedRdf = spy(new EnrichedRdfImpl(rdf));
  }

  @BeforeEach
  void resetMocks() {
    reset(rdf, enrichedRdf);
  }

  private static ResourceMetadata createResourceMetadata(String resourceUrl,
      Set<String> thumbnailNames) {
    final ResourceMetadata metadata = mock(ResourceMetadata.class);
    doReturn(resourceUrl).when(metadata).getResourceUrl();
    doReturn(thumbnailNames).when(metadata).getThumbnailTargetNames();
    final AbstractResourceMetadata source = mock(AbstractResourceMetadata.class);
    doReturn(source).when(metadata).getMetaData();
    return metadata;
  }

  @Test
  void testEnrichResource() {

    // Add first resource: need to create new list and new resource in RDF.
    rdf.setWebResourceList(null);
    final String url1 = "url1";
    final Set<String> names1 = new HashSet<>(Arrays.asList("name1", "name2"));
    final ResourceMetadata resource1 = createResourceMetadata(url1, names1);
    enrichedRdf.enrichResource(resource1);

    // Verify adding the first resource.
    verify(rdf, times(1)).setWebResourceList(notNull());
    assertEquals(1, rdf.getWebResourceList().size());
    assertEquals(url1, rdf.getWebResourceList().get(0).getAbout());
    assertEquals(1, enrichedRdf.getResourceUrls().size());
    assertEquals(names1, enrichedRdf.getThumbnailTargetNames(url1));
    verify(resource1.getMetaData(), times(1)).updateResource(any());

    // Add the second resource: need to create new resource in RDF.
    reset(rdf);
    final String url2 = "url2";
    final Set<String> names2 = new HashSet<>(Arrays.asList("name3", "name4"));
    final ResourceMetadata resource2 = createResourceMetadata(url2, names2);
    enrichedRdf.enrichResource(resource2);

    // Verify adding the second resource.
    verify(rdf, never()).setWebResourceList(notNull());
    assertEquals(2, rdf.getWebResourceList().size());
    assertEquals(url2, rdf.getWebResourceList().get(1).getAbout());
    assertEquals(names2, enrichedRdf.getThumbnailTargetNames(url2));
    verify(resource2.getMetaData(), times(1)).updateResource(any());

    // Add the third resource: make sure it already exists in RDF first.
    reset(rdf);
    final String url3 = "url3";
    final Set<String> names3 = new HashSet<>(Arrays.asList("name5", "name6"));
    final ResourceMetadata resource3 = createResourceMetadata(url3, names3);
    final WebResourceType webResourceType3 = new WebResourceType();
    webResourceType3.setAbout(url3);
    rdf.getWebResourceList().add(webResourceType3);
    enrichedRdf.enrichResource(resource3);

    // Verify adding the second resource.
    verify(rdf, never()).setWebResourceList(notNull());
    assertEquals(3, rdf.getWebResourceList().size());
    assertEquals(url3, rdf.getWebResourceList().get(2).getAbout());
    assertEquals(names3, enrichedRdf.getThumbnailTargetNames(url3));
    verify(resource3.getMetaData(), times(1)).updateResource(any());
  }


  @Test
  void testGetEdmPreview() {

    // Try with object link first
    final String objectUrl = "object url";
    doReturn(Collections.singletonMap(objectUrl, Collections.emptyList())).when(enrichedRdf)
        .getResourceUrls(eq(Collections.singleton(UrlType.OBJECT)));
    assertEquals(objectUrl, enrichedRdf.getEdmPreviewUrl());

    // Now without an object link. Mock values
    doReturn(Collections.emptyMap()).when(enrichedRdf)
        .getResourceUrls(eq(Collections.singleton(UrlType.OBJECT)));
    final String url1 = "url1";
    final String url2 = "url2";
    final String url3 = "url3";
    final String thumbnail1 = "thumbnail1";
    final String thumbnail2Medium = "thumbnail2-MEDIUM";
    final String thumbnail2Large = "thumbnail2-LARGE";
    doReturn(Stream.of(url1, url2)
        .collect(Collectors.toMap(Function.identity(), url -> Collections.emptyList())))
        .when(enrichedRdf).getResourceUrls(eq(EnumSet.of(UrlType.IS_SHOWN_BY, UrlType.HAS_VIEW)));
    doReturn(Stream.of(url1, url2, url3).collect(Collectors.toSet())).when(enrichedRdf)
        .getResourceUrls();
    doReturn(Collections.singleton(thumbnail1)).when(enrichedRdf).getThumbnailTargetNames(url1);
    doReturn(Stream.of(thumbnail2Medium, thumbnail2Large).collect(Collectors.toSet()))
        .when(enrichedRdf).getThumbnailTargetNames(url2);
    assertEquals(thumbnail2Large, enrichedRdf.getEdmPreviewUrl());

  }

  @Test
  void testUpdateEdmPreview() {

    // No aggregations: nothing should happen
    rdf.setEuropeanaAggregationList(null);
    enrichedRdf.updateEdmPreview("url");
    assertNull(rdf.getEuropeanaAggregationList());
    rdf.setEuropeanaAggregationList(new ArrayList<>());
    enrichedRdf.updateEdmPreview("url");
    assertTrue(rdf.getEuropeanaAggregationList().isEmpty());

    // If there is an aggregation
    final EuropeanaAggregationType aggregation1 = new EuropeanaAggregationType();
    rdf.getEuropeanaAggregationList().add(aggregation1);
    final EuropeanaAggregationType aggregation2 = new EuropeanaAggregationType();
    rdf.getEuropeanaAggregationList().add(aggregation2);
    final String url = "url value";
    enrichedRdf.updateEdmPreview(url);
    assertEquals(url, aggregation1.getPreview().getResource());
    assertNull(aggregation2.getPreview());

    // If there is a null argument
    enrichedRdf.updateEdmPreview(null);
    assertEquals(url, aggregation1.getPreview().getResource());
  }

  @Test
  void testFinalizeRdf() {
    final String url = "url value";
    doReturn(url).when(enrichedRdf).getEdmPreviewUrl();
    assertEquals(rdf, enrichedRdf.finalizeRdf());
    verify(enrichedRdf, times(1)).updateEdmPreview(eq(url));
    verify(enrichedRdf, times(1)).updateEdmPreview(anyString());
  }
}