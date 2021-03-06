package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import java.net.URI;
import java.util.List;

/**
 * Interface for managing vocabularies Created by ymamakis on 2/11/16.
 */
public interface DereferencingManagementService {

  /**
   * List all the vocabularies
   *
   * @return The mapped vocabularies
   */
  List<Vocabulary> getAllVocabularies();

  /**
   * Empty the cache
   */
  void emptyCache();

  /**
   * Load the vocabularies from an online source. This does NOT purge the cache.
   *
   * @param directoryUrl The online location of the vocabulary directory.
   * @throws VocabularyImportException In case some issue occurred while importing the
   * vocabularies.
   */
  void loadVocabularies(URI directoryUrl) throws VocabularyImportException;
}
