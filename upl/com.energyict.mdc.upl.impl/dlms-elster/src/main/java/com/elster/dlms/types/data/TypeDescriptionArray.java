package com.elster.dlms.types.data;

public final class TypeDescriptionArray extends TypeDescription
{
  private final int count;
  private final TypeDescription typeDescription;

  public TypeDescriptionArray(final int count,final TypeDescription typeDescription)
  {
    super(DlmsData.DataType.ARRAY,true);
    this.count = count;
    this.typeDescription = typeDescription;
  }

  public int getCount()
  {
    return count;
  }

  public TypeDescription getTypeDescription()
  {
    return typeDescription;
  }

  @Override
  public boolean checkType(final DlmsData data)
  {

    if (!super.checkType(data))
    {
      return false;
    }


    final DlmsDataCollection collection = (DlmsDataCollection)data;

    if (collection.size() != count)
    {
      return false;
    }


    for (DlmsData arrayElement : collection)
    {
      if (!typeDescription.checkType(arrayElement))
      {
        return false;
      }
    }

    return true;
  }

  @Override
  public String toString(final String prefix)
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(prefix).append(getType().getOrgName()).append("(").append(getCount()).append(" elements)=" + EOL);
    sb.append(prefix).append("{" + EOL);
    sb.append(typeDescription.toString(prefix + "\t")).append(EOL);
    sb.append(prefix).append("}");
    return sb.toString();
  }

}
