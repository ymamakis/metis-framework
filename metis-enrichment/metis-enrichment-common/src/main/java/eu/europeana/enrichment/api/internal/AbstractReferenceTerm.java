package eu.europeana.enrichment.api.internal;


import java.net.URL;

public abstract class AbstractReferenceTerm implements ReferenceTerm{

  private final URL reference;

  public AbstractReferenceTerm(URL reference){
    this.reference = reference;
  }

  public URL getReference(){
    return reference;
  }

  public abstract int hashCode();
}