/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/ApplicationAssociationFailedException.java $
 * Version:     
 * $Id: ApplicationAssociationFailedException.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  09.08.2010 16:37:50
 */

package com.elster.dlms.cosem.applicationlayer;

import com.elster.dlms.cosem.application.services.open.OpenResponse;
import java.io.IOException;

/**
 * IOException for an association failure by an OpenResponse as reason.
 *
 * @author osse
 */
public class ApplicationAssociationFailedException extends IOException
{
  private final OpenResponse openResponse;

  public ApplicationAssociationFailedException(String message, OpenResponse openResponse)
  {
    super(message);
    this.openResponse = openResponse;
  }

  public ApplicationAssociationFailedException(OpenResponse openResponse)
  {
    super();
    this.openResponse = openResponse;
  }

  public ApplicationAssociationFailedException(Throwable cause, OpenResponse openResponse)
  {
    this.openResponse = openResponse;
    initCause(cause);
  }

  public ApplicationAssociationFailedException(String message, Throwable cause, OpenResponse openResponse)
  {
    super(message);
    this.openResponse = openResponse;
    initCause(cause);
  }

  public OpenResponse getOpenResponse()
  {
    return openResponse;
  }

}
