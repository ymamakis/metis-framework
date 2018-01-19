package eu.europeana.enrichment.rest.client;

import java.io.IOException;
import org.jibx.runtime.JiBXException;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.metis.dereference.DereferenceUtils;

public class EnrichmentWorkerTestWithMain {

  private static final String DEREFERENCE_URL = "http://metis-dereference-rest-test.eanadev.org";
  private static final String ENRICHMENT_URL = "http://metis-enrichment-rest-test.eanadev.org";

  private static final String SAMPLE_INPUT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
      + "<rdf:RDF xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
      + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
      + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
      + "xmlns:edm=\"http://www.europeana.eu/schemas/edm/\""
      + "xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\""
      + "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\""
      + "xmlns:rdaGr2=\"http://rdvocab.info/ElementsGr2/\""
      + "xmlns:oai=\"http://www.openarchives.org/OAI/2.0/\""
      + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
      + "xmlns:ore=\"http://www.openarchives.org/ore/terms/\""
      + "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\""
      + "xmlns:dcterms=\"http://purl.org/dc/terms/\""
      + "xmlns:sch=\"http://purl.oclc.org/dsdl/schematron\""
      + "xmlns:cc=\"http://creativecommons.org/ns#\"" + "xmlns:dcat=\"http://www.w3.org/ns/dcat#\""
      + "xmlns:odrl=\"http://www.w3.org/ns/odrl/2/\"" + "xmlns:adms=\"http://www.w3.org/ns/adms#\""
      + "xmlns:svcs=\"http://rdfs.org/sioc/services#\""
      + "xmlns:doap=\"http://usefulinc.com/ns/doap#\""
      + "xmlns:wdrs=\"http://www.w3.org/2007/05/powder-s#\">"
      + "<edm:ProvidedCHO rdf:about=\"ProvidedCHO_Bolton_Council_1993_83_27_19\"/>"
      + "<edm:WebResource rdf:about=\"http://boltonworktown.co.uk/wp-content/uploads/1993.83.27.19.jpg\"/>"
      + "<edm:TimeSpan rdf:about=\"#Timespan_Photoconsortium_1937-1938\">"
      + "<skos:prefLabel xml:lang=\"en\">1937-1938</skos:prefLabel>"
      + "<edm:begin>1937-01-01</edm:begin>" + "<edm:end>1938-12-31</edm:end>" + "</edm:TimeSpan>"
      + "<ore:Aggregation rdf:about=\"Bolton Council/1993.83.27.19\">"
      + "<edm:aggregatedCHO rdf:resource=\"ProvidedCHO_Bolton_Council_1993_83_27_19\"/>"
      + "<edm:dataProvider>Bolton Council</edm:dataProvider>"
      + "<edm:isShownAt rdf:resource=\"http://boltonworktown.co.uk/photograph/washing-day-2\"/>"
      + "<edm:isShownBy rdf:resource=\"http://boltonworktown.co.uk/wp-content/uploads/1993.83.27.19.jpg\"/>"
      + "<edm:object rdf:resource=\"http://boltonworktown.co.uk/wp-content/uploads/1993.83.27.19.jpg\"/>"
      + "<edm:provider>AthenaPlus</edm:provider>" + "<dc:rights>Bolton Council</dc:rights>"
      + "<edm:rights rdf:resource=\"http://rightsstatements.org/vocab/InC/1.0/\"/>"
      + "</ore:Aggregation>" + "<ore:Proxy rdf:about=\"ProvidedCHO_Bolton_Council_1993_83_27_19\">"
      + "<dc:creator rdf:resource=\"http://vocab.getty.edu/ulan/500021114\"/>"
      + "<dc:description xml:lang=\"pl\">Washing day near Snowden St. Park Mill is visible in the"
      + "background.</dc:description>"
      + "<dc:description xml:lang=\"pl\">Jour de lessive près de Snowden Street. L'usine Park Mills est visible en arrière-plan.</dc:description>"
      + "<dc:title xml:lang=\"en\">Washing Day</dc:title>"
      + "<dc:title xml:lang=\"fr\">Jour de lessive</dc:title>"
      + "<dc:identifier>1993.83.27.19</dc:identifier>"
      + "<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300008247\"/>"
      + "<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300386103\"/>"
      + "<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300247617\"/>"
      + "<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300006321\"/>"
      + "<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300264626\"/>"
      + "<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300008436\"/>"
      + "<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300053042\"/>"
      + "<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300005730\"/>"
      + "<dc:type rdf:resource=\"http://vocab.getty.edu/aat/300046300\"/>"
      + "<dc:format rdf:resource=\"http://vocab.getty.edu/aat/300128361\"/>"
      + "<dcterms:medium rdf:resource=\"http://vocab.getty.edu/aat/300127149\"/>"
      + "<dcterms:created rdf:resource=\"#Timespan_Photoconsortium_1937-1938\"/>"
      + "<dcterms:provenance xml:lang=\"en\">Bolton Library and Museum Services</dcterms:provenance>"
      + "<dcterms:spatial rdf:resource=\"http://sws.geonames.org/2655237\"/>"
      + "<ore:proxyFor rdf:resource=\"ProvidedCHO_Bolton_Council_1993_83_27_19\"/>"
      + "<ore:proxyIn rdf:resource=\"Bolton Council/1993.83.27.19\"/>"
      + "<edm:type>IMAGE</edm:type>" + "</ore:Proxy>"
      + "<edm:EuropeanaAggregation rdf:about=\"Bolton Council/1993.83.27.19\">"
      + "<edm:aggregatedCHO rdf:resource=\"ProvidedCHO_Bolton_Council_1993_83_27_19\"/>"
      + "<edm:country>Poland</edm:country>" + "<edm:language>pl</edm:language>"
      + "</edm:EuropeanaAggregation>" + "</rdf:RDF>";

  public static void main(String[] args)
      throws JiBXException, DereferenceOrEnrichException, IOException {
    EnrichmentWorker worker = new EnrichmentWorker(DEREFERENCE_URL, ENRICHMENT_URL);
    RDF input = DereferenceUtils.toRDF(SAMPLE_INPUT);
    RDF output = worker.process(input);
    System.out.println(EnrichmentUtils.convertRDFtoString(output));
  }
}
