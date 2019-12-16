/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/obis/IObisDefFinder.java $
 * Version:
 * $Id: IObisDefFinder.java 4487 2012-05-10 16:34:42Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.07.2010 13:35:45
 */
package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;

/**
 * Find methods for {@link ObisCodeDef }
 *
 * @author osse
 */
interface IObisDefFinder
{
  /**
   * Searches for a definition for the specified OBIS code.<P>
   * The first definition found, will be returned.
   *
   * @param obisCode The OBIS code
   * @return The found definition or {@code null } if no definition was found.
   */
  ObisCodeDef findDef(ObisCode obisCode);

  /**
   * Searches for definitions for the specified OBIS code.<P>
   * All definitions which satisfies the OBIS code will be returned.
   * @return An array with the definitions. This array will be empty if no definition was found.
   */
  ObisCodeDef[] findAll(ObisCode obisCode);

}
