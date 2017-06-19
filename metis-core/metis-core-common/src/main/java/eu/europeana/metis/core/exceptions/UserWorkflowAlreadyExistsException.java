package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-02
 */
@ResponseStatus(value= HttpStatus.CONFLICT, reason="User workflow execution already exists")
public class UserWorkflowAlreadyExistsException extends Exception {
  private static final long serialVersionUID = -3332292346834265371L;
  public UserWorkflowAlreadyExistsException(String message){
    super(message);
  }
}