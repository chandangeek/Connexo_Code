/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/types/BerId.java $
 * Version:     
 * $Id: BerId.java 2506 2011-01-06 15:52:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.04.2010 10:22:17
 */
package com.elster.ber.types;

import java.security.InvalidParameterException;

/**
 * Identifier for BER.<P>
 * This is the T of an TLV structure.
 *
 * @author osse
 */
public final class BerId
{
  /**
   * BER Tags.
   */
  public enum Tag
  {
    UNIVERSAL,
    APPLICATION,
    CONTEXT_SPECIFIC,
    PRIVATE
  };

  private final Tag tag;
  private final boolean constructed;
  private final long tagNumber;

  /**
   * Creates the id from an single byte.<P>
   *
   * @param tagByte
   */
  public BerId(int tagByte)
  {
    int tagPart = (tagByte >> 6) & 0x03;

    tag = Tag.values()[tagPart];

    constructed = 0 != (tagByte & 0x20);

    if (0x1F != (tagByte & 0x1F))
    {
      tagNumber = tagByte & 0x1F;
    }
    else
    {
      throw new InvalidParameterException("Subsequent bytes are not supported by this method");
    }
  }

  /**
   * Creates the id with the given information.
   *
   * @param tag The tag.
   * @param constructed {@code true} if "constructed"
   * @param tagNumber The tag number.
   */
  public BerId(Tag tag, boolean constructed, long tagNumber)
  {
    this.tag = tag;
    this.constructed = constructed;
    this.tagNumber = tagNumber;
  }

  public boolean isConstructed()
  {
    return constructed;
  }

//  public void setConstructed(boolean constructed)
//  {
//    this.constructed = constructed;
//  }
  public Tag getTag()
  {
    return tag;
  }

//  public void setTag(Tag tag)
//  {
//    this.tag = tag;
//  }
  public long getTagNumber()
  {
    return tagNumber;
  }

//  public void setTagNumber(long tagNumber)
//  {
//    this.tagNumber = tagNumber;
//  }
  @Override
  public String toString()
  {
    if (constructed)
    {
      return "Identifier( " + tag + " " + tagNumber + "  constructed)";
    }
    else
    {
      return "Identifier( " + tag + " " + tagNumber + ")";
    }
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final BerId other = (BerId)obj;
    if (this.tag != other.tag && (this.tag == null || !this.tag.equals(other.tag)))
    {
      return false;
    }
    if (this.constructed != other.constructed)
    {
      return false;
    }
    if (this.tagNumber != other.tagNumber)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 37 * hash + (this.tag != null ? this.tag.hashCode() : 0);
    hash = 37 * hash + (this.constructed ? 1 : 0);
    hash = 37 * hash + (int)(this.tagNumber ^ (this.tagNumber >>> 32));
    return hash;
  }

}
