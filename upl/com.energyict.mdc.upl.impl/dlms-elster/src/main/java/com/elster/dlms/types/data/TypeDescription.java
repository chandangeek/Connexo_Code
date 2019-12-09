package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;

/**
 * Type description used by the compact array (see {@link DlmsDataCompactArray}).
 *
 * @author osse
 */
public class TypeDescription
{
  protected static final String EOL = "\r\n";
  protected final DlmsData.DataType type;

  /**
   * Creates a {@code TypeDescription} for specified {@code DataType} <P>
   * For the data types {@code ARRAY} and {@code STRUCTURE} the {@link TypeDescriptionArray} and
   * {@link TypeDescriptionStructure } must be used.
   *
   * @param type The data type.
   */
  public TypeDescription(final DataType type)
  {
    this(type, false);
  }

  protected TypeDescription(final DataType type, final boolean allowCollections)
  {
    if (!allowCollections && (type == DataType.ARRAY || type == DataType.STRUCTURE
                              || type == DataType.COMPACT_ARRAY))
    {
      throw new IllegalArgumentException("Illegal data type for this class: " + type);
    }

    this.type = type;
  }

  /**
   * Returns the data type for this {@code TypeDescription}.
   *
   * @return The data type.
   */
  public DataType getType()
  {
    return type;
  }

  /**
   * Checks the data type of specified {@code DlmsData} matches the data type of this type description.<P> In
   * the sub classes for structures and (inner) arrays the complete structure (/array) will be checked.<P> To
   * check if an array can be encoded as compact array each element must be checked.
   *
   * @param element The element to check.
   * @return {@code true} if the type(s) of the element are acceptable for this TypeDescription.
   */
  public boolean checkType(final DlmsData element)
  {
    return element.getType() == type;
  }

  /**
   * Builds a type description for the given element. <P> The type can also be an structure or an array but
   * not an compact array.<P> The element must not contain empty arrays. <P>
   *
   *
   * @param element The element.
   * @return The type description for the element.
   */
  public static TypeDescription buildFromElement(final DlmsData element)
  {
    switch (element.getType())
    {
      case ARRAY:
        return buildFromArray((DlmsDataArray)element);
      case STRUCTURE:
        return buildFromStructure((DlmsDataStructure)element);
      case COMPACT_ARRAY:
        throw new IllegalArgumentException("The element must not be an compact array");
      default:
        return new TypeDescription(element.getType());
    }
  }

  private static TypeDescription buildFromArray(final DlmsDataArray array)
  {
    final int count = array.size();

    if (count == 0)
    {
      throw new IllegalArgumentException("The type description cannot be build from an empty array");
    }

    final TypeDescription typeDescription = buildFromElement(array.get(0));

    //--- check if all ohter array elements have the same type --- 
    for (int i = 1; i < count; i++)
    {
      if (!typeDescription.checkType(array.get(i)))
      {
        throw new IllegalArgumentException("The inner array has inconsistent types");
      }
    }

    return new TypeDescriptionArray(count, typeDescription);
  }

  private static TypeDescription buildFromStructure(final DlmsDataStructure structure)
  {
    final TypeDescriptionStructure result = new TypeDescriptionStructure();

    for (DlmsData data : structure)
    {
      result.getElements().add(buildFromElement(data));
    }

    return result;
  }

  @Override
  public String toString()
  {
    return toString("");
  }

  public String toString(final String prefix)
  {
    return prefix + getType().getOrgName();
  }

}
