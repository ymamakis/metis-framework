package eu.europeana.indexing.solr.crf;

import java.util.HashSet;
import java.util.Set;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

/**
 * Extracts the pure tags from a video resource and generates the fake tags.
 */
public class VideoTagExtractor extends TagExtractor {

  // TODO JOCHEN Don't create extractor for every single web resource! Goes also for other extractors.

    @Override
    public Set<Integer> getFilterTags(WebResourceType webResource) {
        final Set<Integer> filterTags = new HashSet<>();
      
        // TODO JOCHEN See todo below.

        final Integer mediaTypeCode = MediaType.VIDEO.getEncodedValue();

        final Set<Integer> mimeTypeCodes = new HashSet<>();
        mimeTypeCodes.addAll(TechnicalFacet.MIME_TYPE.evaluateAndShift(webResource));
        mimeTypeCodes.add(0);

        final Set<Integer> qualityCodes = new HashSet<>();
        qualityCodes.addAll(TechnicalFacet.VIDEO_QUALITY.evaluateAndShift(webResource));
        qualityCodes.add(0);

        final Set<Integer> durationCodes = new HashSet<>();
        durationCodes.addAll(TechnicalFacet.VIDEO_DURATION.evaluateAndShift(webResource));
        durationCodes.add(0);

        for (Integer mimeType : mimeTypeCodes) {
            for (Integer quality : qualityCodes) {
                for (Integer duration : durationCodes) {
                    filterTags.add(mediaTypeCode | mimeType | quality | duration);
                }
            }
        }

        return filterTags;
    }

    @Override
    public Set<Integer> getFacetTags(WebResourceType webResource) {
        final Set<Integer> facetTags = new HashSet<>();
      
        // TODO JOCHEN Find generic way to do this for all four media types (don't forget common tag
        // extractor!) and two kinds (facet and filter). Plan:
        // 2. Use TechnicalFacet.evaluateAndShift method instead of taking to TechnicalFaceUtils directly.
        // 3. Make two methods (that share some code) for extracting filter and facet tags: use facets list in MediaType.
        // 4. No need for different extractors anymore!  
        // 5. Remove unnecessary code! (run unused code detector)
        // 6. Convert CommonTagExtractor into MimeType Enum.
        // 7. Make sure that mime types are always used startsWith instead of equals: mimetypes may be followed by specifications. 
  
        final Integer mediaTypeCode = MediaType.VIDEO.getEncodedValue();

        for (Integer mimeTypeCode: TechnicalFacet.MIME_TYPE.evaluateAndShift(webResource)) {
            facetTags.add(mediaTypeCode | mimeTypeCode);
        }

        for(Integer qualityCode: TechnicalFacet.VIDEO_QUALITY.evaluateAndShift(webResource)) {
            facetTags.add(mediaTypeCode | qualityCode);
        }

        for (Integer durationCode: TechnicalFacet.VIDEO_DURATION.evaluateAndShift(webResource)) {
            facetTags.add(mediaTypeCode | durationCode);
        }
        
        return facetTags;
    }

}
