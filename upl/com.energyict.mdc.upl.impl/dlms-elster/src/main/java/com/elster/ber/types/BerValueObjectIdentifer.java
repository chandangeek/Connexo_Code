/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/types/BerValueObjectIdentifer.java $
 * Version:     
 * $Id: BerValueObjectIdentifer.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 13:30:30
 */
package com.elster.ber.types;

import com.elster.dlms.types.basic.ObjectIdentifier;

/**
 * BER value for an {@link ObjectIdentifier }
 *
 * @author osse
 */
public class BerValueObjectIdentifer extends BerValueBase<ObjectIdentifier>
{
  public BerValueObjectIdentifer(BerId identifier, ObjectIdentifier value)
  {
    super(identifier, value);
  }

}
