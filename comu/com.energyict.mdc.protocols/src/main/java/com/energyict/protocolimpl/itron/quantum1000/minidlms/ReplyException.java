/*
 * ReplyException.java
 *
 * Created on 7 december 2006, 16:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ReplyException extends IOException {

  private AbstractReplyError abstractReplyDataError;

  public String toString() {
      return "ReplyException: "+super.toString()+", "+getAbstractReplyDataError();
  }

  public ReplyException(String str)
  {
      super(str);
      this.setAbstractReplyDataError(null);
  } // public ReplyException(String str)

  public ReplyException()
  {
      super();
      this.setAbstractReplyDataError(null);

  } // public ReplyException()

  public ReplyException(AbstractReplyError abstractReplyDataError)
  {
      super();
      this.setAbstractReplyDataError(abstractReplyDataError);

  } // public ReplyException(String str)

  public ReplyException(String str, AbstractReplyError abstractReplyDataError)
  {
      super(str);
      this.setAbstractReplyDataError(abstractReplyDataError);

  } // public ReplyException(String str)

    public AbstractReplyError getAbstractReplyDataError() {
        return abstractReplyDataError;
    }

    private void setAbstractReplyDataError(AbstractReplyError abstractReplyDataError) {
        this.abstractReplyDataError = abstractReplyDataError;
    }
}
