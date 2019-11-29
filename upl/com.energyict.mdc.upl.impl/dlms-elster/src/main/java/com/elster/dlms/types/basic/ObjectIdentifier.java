/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/ObjectIdentifier.java $
 * Version:     
 * $Id: ObjectIdentifier.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  07.05.2010 13:27:35
 */
package com.elster.dlms.types.basic;

import java.util.Arrays;
import java.util.List;

/**
 * An Object Identifier (OID)<P>
 * Objects of this class are immutable.
 *
 * @author osse
 */
public class ObjectIdentifier
{
  private final int[] elements;

  /**
   * Creates an OID with the given elements.
   *
   * @param elements A list with elements.
   */
  public ObjectIdentifier(List<Integer> elements)
  {
    this.elements = new int[elements.size()];
    for (int i = 0; i < elements.size(); i++)
    {
      this.elements[i] = elements.get(i);
    }
  }

  /**
   * Creates an the OID with the given elements.
   *
   * @param elements The elements.
   */
  public ObjectIdentifier(int... elements)
  {
    this.elements = new int[elements.length];
    System.arraycopy(elements, 0, this.elements, 0, elements.length);
  }

  /**
   * Creates an the OID by extending an other OID with the given elements.
   *
   * @param base The base of the new OID
   * @param elements The elements added to the elements from the base.
   */
  public ObjectIdentifier(ObjectIdentifier base, int... elements)
  {

    int l1 = base.getElements().length;
    int l2 = elements.length;

    this.elements = new int[l1 + l2];

    System.arraycopy(base.getElements(), 0, this.elements, 0, l1);
    System.arraycopy(elements, 0, this.elements, l1, l2);
  }

  /**
   * Creates the object identifier by
   *
   * @param identifier
   */
  public ObjectIdentifier(String identifier)
  {
    String[] strElements = identifier.split("\\.");

    elements = new int[strElements.length];

    for (int i = 0; i < strElements.length; i++)
    {
      elements[i] = Integer.parseInt(strElements[i]);
    }
  }

  /**
   * Returns the last element of this OID.
   *
   * @return The last element.
   */
  public int getLastElement()
  {
    return elements[elements.length - 1];
  }

  /**
   * Check if this OID starts with the elements of a second OID.
   *
   * @param second The second (shorter) OID.
   * @param allExceptLast Then true this OID must have exact one element more than the second OID.
   * @return Return true if this OID starts with elements of the second OID.
   */
  public boolean startsWith(ObjectIdentifier second, boolean allExceptLast)
  {
    if (second.elements.length >= elements.length)
    {
      return false;
    }

    if (allExceptLast && second.elements.length + 1 != elements.length)
    {
      return false;
    }

    for (int i = 0; i < second.elements.length; i++)
    {
      if (elements[i] != second.elements[i])
      {
        return false;
      }
    }

    return true;
  }



  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    if (elements.length > 0)
    {
      sb.append(elements[0]);
    }


    for (int i = 1; i < elements.length; i++)
    {
      sb.append(".");
      sb.append(elements[i]);
    }

    return sb.toString();
  }

  /**
   * Returns the elements of this OID as array of integers.
   *
   * @return The elements.
   */
  public int[] getElements()
  {
    return elements.clone();
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
    final ObjectIdentifier other = (ObjectIdentifier)obj;
    if (!Arrays.equals(this.elements, other.elements))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 67 * hash + Arrays.hashCode(this.elements);
    return hash;
  }
}
