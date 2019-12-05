package com.elster.dlms.types.data;

import java.util.ArrayList;
import java.util.List;

public final class TypeDescriptionStructure extends TypeDescription
{
  private final List<TypeDescription> elements =
          new ArrayList<TypeDescription>();

  public TypeDescriptionStructure()
  {
    super(DlmsData.DataType.STRUCTURE,true);
  }

  public List<TypeDescription> getElements()
  {
    return elements;
  }

  @Override
  public boolean checkType(final DlmsData data)
  {
    if (!super.checkType(data))
    {
      return false;
    }

    final DlmsDataCollection collection = (DlmsDataCollection)data;


    if (elements.size() != collection.size())
    {
      return false;
    }

    for (int i = 0; i < elements.size(); i++)
    {
      if (!elements.get(i).checkType(collection.get(i)))
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
    sb.append(prefix).append(getType().getOrgName()).append("(").append(elements.size()).append(" elements)=" + EOL);
    sb.append(prefix).append("{" + EOL);
    for (TypeDescription typeDescription : elements)
    {
      sb.append(typeDescription.toString(prefix + "\t")).append(EOL);
    }
    sb.append(prefix).append("}");
    return sb.toString();
  }

}
