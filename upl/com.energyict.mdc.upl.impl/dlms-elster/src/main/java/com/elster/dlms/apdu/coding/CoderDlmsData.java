/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderDlmsData.java $
 * Version:     
 * $Id: CoderDlmsData.java 4793 2012-07-06 10:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 13:54:46
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataBcd;
import com.elster.dlms.types.data.DlmsDataBitString;
import com.elster.dlms.types.data.DlmsDataBoolean;
import com.elster.dlms.types.data.DlmsDataCollection;
import com.elster.dlms.types.data.DlmsDataCompactArray;
import com.elster.dlms.types.data.DlmsDataDate;
import com.elster.dlms.types.data.DlmsDataDateTime;
import com.elster.dlms.types.data.DlmsDataDontCare;
import com.elster.dlms.types.data.DlmsDataDoubleLong;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataEnum;
import com.elster.dlms.types.data.DlmsDataFloat32;
import com.elster.dlms.types.data.DlmsDataFloat64;
import com.elster.dlms.types.data.DlmsDataFloatingPoint;
import com.elster.dlms.types.data.DlmsDataInteger;
import com.elster.dlms.types.data.DlmsDataLong;
import com.elster.dlms.types.data.DlmsDataLong64;
import com.elster.dlms.types.data.DlmsDataLong64Unsigned;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataNull;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataTime;
import com.elster.dlms.types.data.DlmsDataUnsigned;
import com.elster.dlms.types.data.DlmsDataVisibleString;
import com.elster.dlms.types.data.TypeDescription;
import com.elster.dlms.types.data.TypeDescriptionArray;
import com.elster.dlms.types.data.TypeDescriptionStructure;
import com.elster.protocols.streams.CountingInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * En-/decoder for DlmsData elements.<P>
 * These are elements described by the {@code Data ::= CHOICE} rules
 * on p.211 GB ed.7
 *
 * @author osse
 */
public class CoderDlmsData extends AbstractAXdrCoder<DlmsData>
{
  private static final int MAXIMUM_DEEPNESS = 300;

  @Override
  public DlmsData decodeObject(final AXdrInputStream in) throws IOException
  {
    return decodeObject(in, 0);
  }

  @Override
  public void encodeObject(final DlmsData object, final AXdrOutputStream out) throws IOException
  {
    encodeObject(object, out, true);
  }

  private DlmsData decodeObject(final AXdrInputStream in, final int deepness) throws IOException
  {
    if (deepness > MAXIMUM_DEEPNESS)
    {
      throw new IOException("Decoding DLMS-Data. Maximum deepness of structures and arrays exceeded");
    }

    final int tag = in.readTag();
    final DlmsData.DataType dataType = DlmsData.DataType.findDataType(tag);

    if (dataType == null)
    {
      throw new IOException("Unknown data type. Tag:" + tag);
    }
    return decodeObject(in, dataType, deepness);
  }

  private DlmsData decodeObject(final AXdrInputStream in, final DlmsData.DataType dataType, final int deepness)
          throws IOException
  {
    switch (dataType)
    {
      case NULL_DATA:
        return new DlmsDataNull();
      case ARRAY:
        return new DlmsDataArray(decodeCollection(in, deepness + 1));
      case STRUCTURE:
        return new DlmsDataStructure(decodeCollection(in, deepness + 1));
      case BOOLEAN:
        return new DlmsDataBoolean(in.readBoolean());
      case BIT_STRING:
        return new DlmsDataBitString(in.readBitString());
      case DOUBLE_LONG:
        return new DlmsDataDoubleLong(in.readInteger32());
      case DOUBLE_LONG_UNSIGNED:
        return new DlmsDataDoubleLongUnsigned(in.readUnsigned32());
      case FLOATING_POINT:
        return new DlmsDataFloatingPoint(Float.intBitsToFloat(in.readInteger32()));
      case OCTET_STRING:
        return new DlmsDataOctetString(in.readOctetString());
      case VISIBLE_STRING:
        return new DlmsDataVisibleString(in.readVisibleString());
      case BCD:
        return new DlmsDataBcd(in.readInteger8());
      case INTEGER:
        return new DlmsDataInteger(in.readInteger8());
      case LONG:
        return new DlmsDataLong(in.readInteger16());
      case UNSIGNED:
        return new DlmsDataUnsigned(in.readUnsigned8());
      case LONG_UNSIGNED:
        return new DlmsDataLongUnsigned(in.readUnsigned16());
      case COMPACT_ARRAY:
        return decodeCompactArray(in, deepness + 1);
      case LONG64:
        return new DlmsDataLong64(in.readInteger64());
      case LONG64_UNSIGNED:
        return new DlmsDataLong64Unsigned(in.readUnsigned64());
      case ENUM:
        return new DlmsDataEnum(in.readUnsigned8());
      case FLOAT32:
        return new DlmsDataFloat32(Float.intBitsToFloat(in.readInteger32()));
      case FLOAT64:
        return new DlmsDataFloat64(Double.longBitsToDouble(in.readInteger64()));
      case DATE_TIME:
        return new DlmsDataDateTime(in.readOctetString(12));
      case DATE:
        return new DlmsDataDate(in.readOctetString(5));
      case TIME:
        return new DlmsDataTime(in.readOctetString(4));
      case DONT_CARE:
        return new DlmsDataDontCare();
      default:
        throw new IOException("Unknown DLMS data type. Tag: " + dataType);
    }
  }

  private void encodeObject(final DlmsData object, final AXdrOutputStream out, final boolean writeTags) throws
          IOException
  {
    if (writeTags)
    {
      out.write(object.getType().getTag());
    }
    switch (object.getType())
    {
      case NULL_DATA:
        break;
      case ARRAY:
        encodeCollection((DlmsDataArray)object, out, writeTags);
        break;
      case STRUCTURE:
        encodeCollection((DlmsDataStructure)object, out, writeTags);
        break;
      case BOOLEAN:
        out.writeBoolean(((DlmsDataBoolean)object).getValue());
        break;
      case BIT_STRING:
        out.writeBitString(((DlmsDataBitString)object).getValue());
        break;
      case DOUBLE_LONG:
        out.writeInteger32(((DlmsDataDoubleLong)object).getValue());
        break;
      case DOUBLE_LONG_UNSIGNED:
        out.writeUnsigned32(((DlmsDataDoubleLongUnsigned)object).getValue());
        break;
      case FLOATING_POINT:
        Float dataFloatingPoint = ((DlmsDataFloatingPoint)object).getValue(); //.floatValue();
        out.writeInteger32(Float.floatToIntBits(dataFloatingPoint));
        break;
      case OCTET_STRING:
        out.writeOctetStringVariableLength(((DlmsDataOctetString)object).getValue());
        break;
      case VISIBLE_STRING:
        out.writeVisibleString(((DlmsDataVisibleString)object).getValue());
        break;
      case BCD:
        out.writeInteger8(((DlmsDataBcd)object).getValue());
        break;
      case INTEGER:
        out.writeInteger8(((DlmsDataInteger)object).getValue());
        break;
      case LONG:
        out.writeInteger16(((DlmsDataLong)object).getValue());
        break;
      case UNSIGNED:
        out.writeUnsigned8(((DlmsDataUnsigned)object).getValue());
        break;
      case LONG_UNSIGNED:
        out.writeUnsigned16(((DlmsDataLongUnsigned)object).getValue());
        break;
      case COMPACT_ARRAY:
        encodeCompactArray((DlmsDataCompactArray)object, out);
        break;
      case LONG64:
        out.writeInteger64(((DlmsDataLong64)object).getValue());
        break;
      case LONG64_UNSIGNED:
        out.write(((DlmsDataLong64Unsigned)object).toBytes());
        break;
      case ENUM:
        out.writeUnsigned8(((DlmsDataEnum)object).getValue());
        break;
      case FLOAT32:
        Float dataFloat32 = ((DlmsDataFloat32)object).getValue(); //.floatValue();
        out.writeInteger32(Float.floatToIntBits(dataFloat32));
        break;
      case FLOAT64:
        Double dataFloat64 = ((DlmsDataFloat64)object).getValue();
        out.writeInteger64(Double.doubleToLongBits(dataFloat64));
        break;
      case DATE_TIME:
        out.writeOctetStringFixLength(((DlmsDataDateTime)object).getValue().toBytes());
        break;
      case DATE:
        out.writeOctetStringFixLength(((DlmsDataDate)object).getValue().toBytes());
        break;
      case TIME:
        out.writeOctetStringFixLength(((DlmsDataTime)object).getValue().toBytes());
        break;
      case DONT_CARE:
        break;
      default:
        throw new UnsupportedOperationException("Type not supported: " + object.getType());
    }
  }

  private static final DlmsData[] EMPTY_DLMS_DATA_ARRAY = new DlmsData[0];

  private DlmsData[] decodeCollection(final AXdrInputStream in, final int deepness) throws
          IOException
  {
    int count = in.readLength();
    List<DlmsData> elements = new ArrayList<DlmsData>(count);
    for (int i = 0; i < count; i++)
    {
      elements.add(decodeObject(in, deepness));
    }
    return elements.toArray(EMPTY_DLMS_DATA_ARRAY);
  }

  private void encodeCollection(final DlmsDataCollection collection, final AXdrOutputStream out,
                                final boolean writeTags) throws
          IOException
  {
    out.writeLength(collection.size());
    for (DlmsData d : collection)
    {
      encodeObject(d, out, writeTags);
    }
  }

// <editor-fold defaultstate="collapsed" desc="CompactArray support">
  private DlmsDataCompactArray decodeCompactArray(final AXdrInputStream in, final int deepness) throws
          IOException
  {

    final TypeDescription typeDescription = decodeTypeDescription(in, deepness);

    final int count = in.readLength();

    final CountingInputStream countingIn = new CountingInputStream(in);
    final AXdrInputStream countedAXdrIn = new AXdrInputStream(countingIn);

    final List<DlmsData> elements = new ArrayList<DlmsData>();

    while (countingIn.getCount() < count)
    {
      elements.add(decodeType(countedAXdrIn, typeDescription, deepness));
    }

    if (countingIn.getCount() > count)
    {
      throw new IOException("Compact array content is to long. Expected:" + count + " Actual:" + countingIn.
              getCount());
    }

    return new DlmsDataCompactArray(typeDescription, elements.toArray(EMPTY_DLMS_DATA_ARRAY));
  }

  private DlmsData decodeType(final AXdrInputStream in, final TypeDescription typeDescription,
                              final int deepness) throws
          IOException
  {
    switch (typeDescription.getType())
    {
      case ARRAY:
      {
        final TypeDescriptionArray tdArray = (TypeDescriptionArray)typeDescription;

        DlmsData[] elements = new DlmsData[tdArray.getCount()];
        for (int i = 0; i < tdArray.getCount(); i++)
        {
          elements[i] = decodeType(in, tdArray.getTypeDescription(), deepness + 1);
        }
        return new DlmsDataArray(elements);
      }

      case STRUCTURE:
        final TypeDescriptionStructure tdStruture = (TypeDescriptionStructure)typeDescription;
        DlmsData[] elements = new DlmsData[tdStruture.getElements().size()];
        for (int i = 0; i < tdStruture.getElements().size(); i++)
        {
          elements[i] = decodeObject(in, tdStruture.getElements().get(i).getType(), deepness + 1);
        }
        return new DlmsDataStructure(elements);
      default:
        return decodeObject(in, typeDescription.getType(), deepness);
    }
  }

  private void encodeCompactArray(final DlmsDataCompactArray collection, final AXdrOutputStream out) throws
          IOException
  {

    //--- get or build type description ---
    TypeDescription typeDescription = collection.getTypeDescription();

    if (typeDescription == null)
    {
      if (collection.size() == 0)
      {
        throw new IOException(
                "An compact array must either provide an type description or have at least one element");
      }
      typeDescription = TypeDescription.buildFromElement(collection.get(0));
    }

    encodeTypeDescription(typeDescription, out);

    //--- check ---
    for (DlmsData d : collection)
    {
      if (!typeDescription.checkType(d))
      {
        throw new IOException("An element of the compact array has an wrong type");
      }
    }

    //--- build and write octet string with content ---
    ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
    AXdrOutputStream bufferAXdrOutputStream = new AXdrOutputStream(bufferStream);

    for (DlmsData d : collection)
    {
      encodeObject(d, bufferAXdrOutputStream, false);
    }

    bufferAXdrOutputStream.flush();
    bufferAXdrOutputStream.close();

    out.writeOctetStringVariableLength(bufferStream.toByteArray());
  }

  private void encodeTypeDescription(final TypeDescription typeDescription, final AXdrOutputStream out) throws
          IOException
  {
    out.writeUnsigned8(typeDescription.getType().getTag());

    switch (typeDescription.getType())
    {
      case ARRAY:
        out.writeUnsigned16(((TypeDescriptionArray)typeDescription).getCount());
        encodeTypeDescription(((TypeDescriptionArray)typeDescription).getTypeDescription(), out);
        break;
      case STRUCTURE:
        out.writeLength(((TypeDescriptionStructure)typeDescription).getElements().size());
        for (TypeDescription tdStructure : ((TypeDescriptionStructure)typeDescription).getElements())
        {
          encodeTypeDescription(tdStructure, out);
        }
        break;
      default:
      //do nothing else -the tag is allready written.
    }
  }

  private TypeDescription decodeTypeDescription(final AXdrInputStream in, final int deepness) throws
          IOException
  {
    if (deepness > MAXIMUM_DEEPNESS)
    {
      throw new IOException("Decoding DLMS-Data. Maximum deepness of structures and arrays exceeded");
    }

    final int tag = in.readUnsigned8();
    final DlmsData.DataType dataType = DlmsData.DataType.findDataType(tag);

    if (dataType == null)
    {
      throw new IOException("Unknown tag for type description: " + tag);
    }


    switch (dataType)
    {
      case ARRAY:
        final int arrayLength = in.readUnsigned16();
        final TypeDescription arrayTd = decodeTypeDescription(in, deepness + 1);
        return new TypeDescriptionArray(arrayLength, arrayTd);
      case STRUCTURE:
        final TypeDescriptionStructure tdStructure = new TypeDescriptionStructure();
        final int structureLength = in.readLength();

        for (int i = 0; i < structureLength; i++)
        {
          tdStructure.getElements().add(decodeTypeDescription(in, deepness + 1));
        }
        return tdStructure;
      case COMPACT_ARRAY:
        throw new IOException("Illegal data type for type desciptions: " + dataType);
      case NULL_DATA:
      case DONT_CARE:
        throw new IOException("Illegal data type for compact arrays: " + dataType);
      default:
        return new TypeDescription(dataType);
    }
  }// </editor-fold>
}
