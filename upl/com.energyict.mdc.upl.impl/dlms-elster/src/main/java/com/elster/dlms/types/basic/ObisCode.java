/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/ObisCode.java $
 * Version:     
 * $Id: ObisCode.java 3833 2011-12-12 10:51:57Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 09:48:15
 */
package com.elster.dlms.types.basic;

import java.security.InvalidParameterException;

/**
 * This class represents an OBIS code.<P>
 * An OBIS code consists of 6 value groups:<br>
 * <pre>
 *    A.B.C.D.E.F
 * </pre>
 * Where each value groups is an unsigned byte.
 *
 * @author osse
 */
public class ObisCode implements Comparable<ObisCode>
{

  public int compareTo(final ObisCode o)
  {
    if (data < o.data)
    {
      return -1;
    } else if (data > o.data)
    {
      return 1;
    }
    return 0;
  }

  public enum Groups
  {
    A, B, C, D, E, F
  };
  private final long data;

  /**
   * Constructs the OBIS code from an string.<P>
   *
   * The string must have the following format:<br>
   * A.B.C.D.E.F<br>
   * Where A-F are bytes in decimal form.
   *
   * @param code The OBIS code as string.
   */
  public ObisCode(final String code)
  {
    data=parse(code);
  }

  private ObisCode(long data)
  {
    this.data = data;
  }
  
  

  /**
   * Constructs the OBIS code from an byte array.<P>
   *
   * The array must have an length of 6.<P>
   * The first element will be value group A.
   *
   * @param elements The elements of the OBIS code.
   */
  public ObisCode(final byte[] elements)
  {
    data=fromByteArray(elements);
  }

  /**
   * Constructs the OBIS code from the individually specified value groups.<P>
   *
   * @param a Value group A
   * @param b Value group B
   * @param c Value group C
   * @param d Value group D
   * @param e Value group E
   * @param f Value group F
   */
  public ObisCode(final int a, final int b, final int c, final int d, final int e, final int f)
  {
    data=(long)f | ((long)e<<8) | ((long)d<<16) | ((long)c<<24) | (((long)b)<<32) | (((long)a)<<40);
  }

  /**
   * Returns the value group.<P>
   *
   * @param no Zero based index for the value group: 0=A,1=B,2=C,3=D,4=E,5=F
   * @return The value of the value group.
   */
  public int getValueGroup(final int no)
  {
    return (int)(0xff & (data>> (8*(5-no))));
  }

  /**
   * Returns (the value of) the value group.
   *
   * @param valueGroup The value group.
   * @return The (value of) the value group.
   */
  public int getValueGroup(final Groups valueGroup)
  {
    return getValueGroup(valueGroup.ordinal());
  }

  /**
   * Set the OBIS code from an byte array.<P>
   * The array must have an length of 6.<P>
   * The first element will be value group A.
   *
   * @param valueGroups
   */
  private long fromByteArray(final byte[] valueGroups)
  {
    if (valueGroups.length != 6)
    {
      throw new InvalidParameterException("valueGroups must be an array of 6 bytes");
    }
    
    return ((0xFFL & valueGroups[0]) <<40)
            | ((0xFFL & valueGroups[1]) <<32)
            | ((0xFFL & valueGroups[2]) <<24)
            | ((0xFFL & valueGroups[3]) <<16)
            | ((0xFFL & valueGroups[4]) <<8)
            | (0xFFL & valueGroups[5]);    
  }

  /**
   * Returns a byte array containing the value groups.
   *
   * @return The array containing the value groups.
   */
  public byte[] toByteArray()
  {
    byte[] result= new byte[6];
    result[5]=(byte)(0xff& data);
    result[4]=(byte)(0xff & (data>> (8*1)));
    result[3]=(byte)(0xff & (data>> (8*2)));
    result[2]=(byte)(0xff & (data>> (8*3)));
    result[1]=(byte)(0xff & (data>> (8*4)));
    result[0]=(byte)(0xff & (data>> (8*5)));
    return result;
  }

  public int getA()
  {
    return getValueGroup(0);
  }

  public int getB()
  {
    return getValueGroup(1);
  }

  public int getC()
  {
    return getValueGroup(2);
  }

  public int getD()
  {
    return getValueGroup(3);
  }

  public int getE()
  {
    return getValueGroup(4);
  }

  public int getF()
  {
    return getValueGroup(5);
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (obj == this)
    {
      return true;
    }

    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final ObisCode other = (ObisCode) obj;

    return data==other.data;
  }

  @Override
  public int hashCode()
  {
    return (int) data ^ (int) (data>>19);
  }

  @Override
  public String toString()
  {
    return getValueGroup(0) + "." + getValueGroup(1) + "." + getValueGroup(2) + "." + getValueGroup(3)
            + "." + getValueGroup(4) + "." + getValueGroup(5);
  }

  /**
   * Sets the value groups from an string.<P>
   * The string must have the following format:<br>
   * A.B.C.D.E.F<br>
   * Where A-F are bytes in decimal form.
   *
   * @param code The string to parse
   */
  private long parse(final String code)
  {
    final String parts[] = code.split("\\.");

    if (parts.length != 6)
    {
      throw new IllegalArgumentException("The obis code must have 6 parts (value groups)");
    }

    long result=Integer.parseInt(parts[0]);
    for (int i = 1; i < 6; i++)
    {
      result =result<<8;
      result |=Integer.parseInt(parts[i]) ;
    }
    return result;
  }

  /**
   * Create a new OBIS code by changing one group value.
   * 
   * @param group The group to change (zero based)
   * @param newGroupValue The new group value.
   * @return The derived OBIS code
   */
  public ObisCode derive(final int group, final int newGroupValue)
  {
    final int bitoffset=40-group*8;
    long dv= data & ~(0xFFL<< bitoffset); //relevant bits to zero
    return new ObisCode(dv| ((long)newGroupValue <<bitoffset));
  }

  /**
   * Create a new OBIS code by changing one group value.
   * 
   * @param group The group to change 
   * @param newGroupValue The new group value.
   * @return The derived OBIS code
   */
  public ObisCode derive(final Groups group, final int newGroupValue)
  {
    return derive(group.ordinal(), newGroupValue);
  }
}
