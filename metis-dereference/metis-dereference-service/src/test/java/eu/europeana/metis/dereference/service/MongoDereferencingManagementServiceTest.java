package eu.europeana.metis.dereference.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by ymamakis on 2/22/16.
 */
class MongoDereferencingManagementServiceTest {

  private MongoDereferencingManagementService service;
  private EmbeddedLocalhostMongo embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
  private Datastore vocDaoDatastore;

  @BeforeEach
  void prepare() {
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();

    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));

    VocabularyDao vocDao = new VocabularyDao(mongoClient, "voctest") {
      {
        vocDaoDatastore = this.getDatastore();
      }
    };
    ProcessedEntityDao processedEntityDao = mock(ProcessedEntityDao.class);
    service = new MongoDereferencingManagementService(vocDao, processedEntityDao);
  }

  @Test
  void testGetAllVocabularies() {
    Vocabulary voc = new Vocabulary();
    voc.setIterations(0);
    voc.setName("testName");
    voc.setUris(Collections.singleton("http://www.test.uri/"));
    voc.setXslt("testXSLT");
    vocDaoDatastore.save(voc);
    List<Vocabulary> retVoc = service.getAllVocabularies();
    assertEquals(1, retVoc.size());
  }

  @AfterEach
  void destroy() {
    embeddedLocalhostMongo.stop();
  }
}
