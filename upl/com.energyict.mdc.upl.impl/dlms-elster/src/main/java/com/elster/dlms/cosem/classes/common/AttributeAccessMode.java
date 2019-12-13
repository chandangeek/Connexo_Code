package com.elster.dlms.cosem.classes.common;

/**
 * Attribute access mode.<P>
 * (See BB ed.9 p.53)
 *
 * @author osse
 */
public enum AttributeAccessMode
{
  NO_ACCESS, READ_ONLY, WRITE_ONLY, READ_AND_WRITE,AUTHENTICATED_READ_ONLY, AUTHENTICATED_WRITE_ONLY, AUTHENTICATED_READ_AND_WRITE;
}
