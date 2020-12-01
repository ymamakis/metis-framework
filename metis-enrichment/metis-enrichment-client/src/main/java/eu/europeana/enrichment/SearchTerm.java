package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.Language;
import eu.europeana.metis.schema.jibx.LanguageCodes;
import java.util.List;

public abstract class SearchTerm {

  private String textValue;
  private LanguageCodes language;

  public SearchTerm (String textValue, LanguageCodes language){
    this.textValue = textValue;
    this.language = language;
  }

  public abstract List<EntityType> getFieldType();

  public abstract boolean equals(SearchTerm searchTerm);

  public abstract int hashCode();

  public String getTextValue(){
    return textValue;
  }

  public LanguageCodes getLanguage(){
    return language;
  }
}
