/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/open/DlmsConformance.java $
 * Version:     
 * $Id: DlmsConformance.java 1836 2010-08-06 17:08:57Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  05.05.2010 17:56:37
 */
package com.elster.dlms.cosem.application.services.open;

public enum DlmsConformance
{
  RESERVED_ZERO,
  RESERVED_ONE,
  RESERVED_TWO,
  READ,
  WRITE,
  UNCONFIRMED_WRITE,
  RESERVED_SIX,
  RESERVED_SEVEN,
  ATTRIBUTE0_SUPPORTED_WITH_SET,
  PRIORITY_MGMT_SUPPORTED,
  ATTRIBUTE0_SUPPORTED_WITH_GET,
  BLOCK_TRANSFER_WITH_GET_OR_READ,
  BLOCK_TRANSFER_WITH_SET_OR_WRITE,
  BLOCK_TRANSFER_WITH_ACTION,
  MULTIPLE_REFERENCES,
  INFORMATION_REPORT,
  RESERVED_SIXTEEN,
  RESERVED_SEVENTEEN,
  PARAMETERIZED_ACCESS,
  GET,
  SET,
  SELECTIVE_ACCESS,
  EVENT_NOTIFICATION,
  ACTION
};

