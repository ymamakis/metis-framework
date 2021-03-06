package eu.europeana.normalization.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.normalization.languages.LanguagesVocabulary;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class NormalizerSettingsTest {

  @Test
  void testNonNullValues() throws NormalizationConfigurationException {

    // Check default values.
    final NormalizerSettings settings = new NormalizerSettings();
    assertEquals(NormalizerSettings.DEFAULT_CLEAN_MARKUP_TAGS_MODE,
        settings.getCleanMarkupTagsMode());
    assertEquals(NormalizerSettings.DEFAULT_LANGUAGE_AMBIGUITY_HANDLING,
        settings.getLanguageAmbiguityHandling());
    assertEquals(NormalizerSettings.DEFAULT_MIN_LANGUAGE_LABEL_LENGTH,
        settings.getMinLanguageLabelLength());
    assertEquals(NormalizerSettings.DEFAULT_MINIMUM_CONFIDENCE, settings.getMinimumConfidence(),
        0.000001);
    assertEquals(NormalizerSettings.DEFAULT_DC_LANGUAGE_TARGET_VOCABULARIES,
        settings.getTargetDcLanguageVocabularies());
    assertEquals(NormalizerSettings.DEFAULT_XML_LANG_TARGET_VOCABULARIES,
        settings.getTargetXmlLangVocabularies());

    // Check setters
    NormalizerSettings newSettings = settings.setCleanMarkupTagsMode(CleanMarkupTagsMode.HTML_ONLY);
    assertEquals(CleanMarkupTagsMode.HTML_ONLY, settings.getCleanMarkupTagsMode());
    assertSame(settings, newSettings);
    newSettings = settings.setLanguageAmbiguityHandling(AmbiguityHandling.CHOOSE_FIRST);
    assertEquals(AmbiguityHandling.CHOOSE_FIRST, settings.getLanguageAmbiguityHandling());
    assertSame(settings, newSettings);
    assertSame(settings, newSettings);
    newSettings = settings.setMinLanguageLabelLength(6);
    assertEquals(6, settings.getMinLanguageLabelLength());
    assertSame(settings, newSettings);
    newSettings = settings.setMinimumConfidence(0.1F);
    assertEquals(0.1F, settings.getMinimumConfidence(), 0.000001);
    assertSame(settings, newSettings);
    final List<LanguagesVocabulary> dcLanguageVocabularyList = Arrays
        .asList(LanguagesVocabulary.ISO_639_2B, LanguagesVocabulary.ISO_639_2T);
    newSettings = settings.setTargetDcLanguageVocabularies(dcLanguageVocabularyList);
    assertEquals(dcLanguageVocabularyList, settings.getTargetDcLanguageVocabularies());
    assertSame(settings, newSettings);
    final List<LanguagesVocabulary> xmlLangVocabularyList = Collections
        .singletonList(LanguagesVocabulary.ISO_639_3);
    newSettings = settings.setTargetXmlLangVocabularies(xmlLangVocabularyList);
    assertEquals(xmlLangVocabularyList, settings.getTargetXmlLangVocabularies());
    assertSame(settings, newSettings);

    // Check negative label length
    settings.setMinLanguageLabelLength(-6);
    assertEquals(0, settings.getMinLanguageLabelLength());
  }

  @Test
  void testSetMinimumConfidenceToNegativeValue() {
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setMinimumConfidence(-1.0F));
  }

  @Test
  void testSetMinimumConfidenceToLargeValue() {
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setMinimumConfidence(2.0F));
  }

  @Test
  void testSetTargetLanguageVocabularyToNull() {

    // For dc:language
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setTargetDcLanguageVocabularies(null));
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setTargetDcLanguageVocabularies(new ArrayList<>()));
    assertThrows(NormalizationConfigurationException.class, () -> new NormalizerSettings()
        .setTargetDcLanguageVocabularies(Arrays.asList(LanguagesVocabulary.ISO_639_3, null)));

    // For xml:lang
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setTargetXmlLangVocabularies(null));
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setTargetXmlLangVocabularies(new ArrayList<>()));
    assertThrows(NormalizationConfigurationException.class, () -> new NormalizerSettings()
        .setTargetXmlLangVocabularies(Arrays.asList(LanguagesVocabulary.ISO_639_3, null)));
  }

  @Test
  void testSetLanguageAmbiguityHandlingToNull() {
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setLanguageAmbiguityHandling(null));
  }

  @Test
  void testSetCleanMarkupTagsModeToNull() {
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setCleanMarkupTagsMode(null));
  }

}
