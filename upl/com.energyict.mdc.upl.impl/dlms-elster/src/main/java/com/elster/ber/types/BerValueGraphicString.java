/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/types/BerValueGraphicString.java $
 * Version:     
 * $Id: BerValueGraphicString.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 13:31:49
 */
package com.elster.ber.types;

/**
 * BER Value for an "graphic string".
 *
 * @author osse
 */
public class BerValueGraphicString extends BerValueBase<String>
{
  public BerValueGraphicString(BerId identifier, String value)
  {
    super(identifier, value);
  }

}
