/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrValueBigDecimal.java $
 * Version:     
 * $Id: AgrValueBigDecimal.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.07.2010 11:19:16
 */
package com.elster.agrimport.agrreader;

import java.math.BigDecimal;

/**
 * This class saves an archive value as big decimal.
 *
 * @author osse
 */
public class AgrValueBigDecimal extends AbstractAgrValue<BigDecimal>
{
  public AgrValueBigDecimal()
  {
    super();
  }

  AgrValueBigDecimal(BigDecimal bigDecimal)
  {
    super(bigDecimal);
  }

}
