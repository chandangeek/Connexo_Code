/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/open/AuthenticationValue.java $
 * Version:     
 * $Id: AuthenticationValue.java 6722 2013-06-11 10:17:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.07.2010 12:24:13
 */
package com.elster.dlms.cosem.application.services.open;

import com.elster.coding.CodingUtils;
import com.elster.dlms.types.basic.BitString;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;


/**
 * Class for authentication values.<P>
 * To create an instance one of the static creation methods must
 * be used.
 * <P>
 * Instances of this class are immutable.
 * <P>
 * The types {@code EXTERNAL} and {@code OTHER} are (currently) not supported.
 *
 * @author osse
 */
public abstract class AuthenticationValue
{
  public enum Type
  {
    CHARSTRING, BITSTRING, EXTERNAL, OTHER
  };

  /**
   * Creates an char string authentication value.
   *
   * @return The created authentication value.
   */
  public static AuthenticationValue createCharstring(final byte[] stringBytes)
  {
    return new AuthenticationValueCharstring(stringBytes);
  }

  /**
   * Creates an char string authentication value.
   *
   * @param string The value.
   * @return The created authentication value.
   */
  public static AuthenticationValue createCharstring(final String string)
  {
    try
    {
      return new AuthenticationValueCharstring(string.getBytes("ASCII"));
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new IllegalArgumentException("string can not be converted to bytes",ex);
    }
  }


  /**
   * Creates an bit string authentication value.
   *
   * @param value The bit string.
   * @return The authentication value.
   */
  public static AuthenticationValue createBitString(BitString value)
  {
    return new AuthenticationValueBitstring(value);
  }


  /**
   * Returns the type of the authentication value
   *
   * @return
   */
  public abstract Type getType();

  public abstract byte[] toBytes();
  public abstract Object getValue();

  @Override
  public String toString()
  {
    return getType() + " " + CodingUtils.byteArrayToString(toBytes());
  }


  private static class AuthenticationValueCharstring extends AuthenticationValue
  {
    private final byte[] stringBytes;

    public AuthenticationValueCharstring(byte[] stringBytes)
    {
      this.stringBytes = stringBytes.clone();
    }

    @Override
    public Type getType()
    {
      return Type.CHARSTRING;
    }

    @Override
    public byte[] toBytes()
    {
       return stringBytes.clone();
    }

    @Override
    public byte[] getValue()
    {
      return stringBytes.clone();
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 83 * hash + Arrays.hashCode(this.stringBytes);
      return hash;
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
      final AuthenticationValueCharstring other = (AuthenticationValueCharstring)obj;
      if (!Arrays.equals(this.stringBytes, other.stringBytes))
      {
        return false;
      }
      return true;
    }

    

  }

  private static class AuthenticationValueBitstring extends AuthenticationValue
  {
    private final BitString value;

    public AuthenticationValueBitstring(final BitString value)
    {
      this.value = value;
    }


    @Override
    public Type getType()
    {
      return Type.BITSTRING;
    }

    @Override
    public byte[] toBytes()
    {
       return value.getData();
    }

    @Override
    public BitString getValue()
    {
      return value;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 29 * hash + (this.value != null ? this.value.hashCode() : 0);
      return hash;
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
      final AuthenticationValueBitstring other = (AuthenticationValueBitstring)obj;
      if (this.value != other.value && (this.value == null || !this.value.equals(other.value)))
      {
        return false;
      }
      return true;
    }
  }


}
