/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.classes.info;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.apdu.coding.CoderDlmsData;
import com.elster.coding.CodingUtils;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CosemClassInfosTest
{
  private static final String OBJECT_LIST_V0 =
          "01 17 02 04 12 00 08 11 00 09 06 00 00 01 00 00 FF 02 02 01 09 02 03 0F 01 16 01"
          + "00 02 03 0F 02 16 03 00 02 03 0F 03 16 03 00 02 03 0F 04 16 01 00 02 03 0F 05 16 01 00 02 03 0F 06 16"
          + "01 00 02 03 0F 07 16 01 00 02 03 0F 08 16 03 00 02 03 0F 09 16 01 00 01 06 02 02 0F 01 03 00 02 02 0F"
          + "02 03 00 02 02 0F 03 03 00 02 02 0F 04 03 00 02 02 0F 05 03 00 02 02 0F 06 03 00 02 04 12 00 03 11 00"
          + "09 06 07 00 61 61 00 FF 02 02 01 03 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 01"
          + "01 02 02 0F 01 03 00 02 04 12 00 03 11 00 09 06 07 00 0D 02 00 FF 02 02 01 03 02 03 0F 01 16 01 00 02"
          + "03 0F 02 16 01 00 02 03 0F 03 16 01 00 01 01 02 02 0F 01 03 00 02 04 12 00 03 11 00 09 06 07 00 0D 00"
          + "00 FF 02 02 01 03 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 01 01 02 02 0F 01 03"
          + "00 02 04 12 00 03 11 00 09 06 07 00 2A 00 00 FF 02 02 01 03 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00"
          + "02 03 0F 03 16 01 00 01 01 02 02 0F 01 03 00 02 04 12 00 03 11 00 09 06 07 00 29 00 00 FF 02 02 01 03"
          + "02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 01 01 02 02 0F 01 03 00 02 04 12 00 03"
          + "11 00 09 06 07 00 35 00 00 FF 02 02 01 03 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01"
          + "00 01 01 02 02 0F 01 03 00 02 04 12 00 0C 11 01 09 06 00 00 28 00 00 FF 02 02 01 02 02 03 0F 01 16 01"
          + "00 02 03 0F 02 16 01 00 01 08 02 02 0F 01 03 00 02 02 0F 02 03 00 02 02 0F 03 03 00 02 02 0F 04 03 00"
          + "02 02 0F 05 03 00 02 02 0F 06 03 00 02 02 0F 07 03 00 02 02 0F 08 03 00 02 04 12 00 01 11 00 09 06 00"
          + "00 2A 00 00 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 01 11 00 09 06"
          + "07 00 00 08 01 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 01 11 00 09"
          + "06 00 00 60 01 00 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 01 11 00"
          + "09 06 07 01 00 00 01 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 01 11"
          + "00 09 06 07 00 00 02 02 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 01"
          + "11 00 09 06 07 00 00 02 0B FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00"
          + "01 11 00 09 06 07 00 00 02 0C FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12"
          + "00 07 11 01 09 06 07 00 63 01 80 FF 02 02 01 08 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03"
          + "16 01 00 02 03 0F 04 16 01 00 02 03 0F 05 16 01 00 02 03 0F 06 16 01 00 02 03 0F 07 16 01 00 02 03 0F"
          + "08 16 01 00 01 04 02 02 0F 01 03 00 02 02 0F 02 03 00 02 02 0F 03 03 00 02 02 0F 04 03 00 02 04 12 00"
          + "03 11 00 09 06 07 00 2B 00 00 FF 02 02 01 03 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16"
          + "01 00 01 01 02 02 0F 01 03 00 02 04 12 00 03 11 00 09 06 07 00 2B 02 00 FF 02 02 01 03 02 03 0F 01 16"
          + "01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 01 01 02 02 0F 01 03 00 02 04 12 00 0F 11 00 09 06 00"
          + "00 28 00 00 FF 02 02 01 08 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 02 03 0F 04"
          + "16 01 00 02 03 0F 05 16 01 00 02 03 0F 06 16 01 00 02 03 0F 07 16 01 00 02 03 0F 08 16 01 00 01 04 02"
          + "02 0F 01 03 00 02 02 0F 02 03 00 02 02 0F 03 03 00 02 02 0F 04 03 00 02 04 12 00 11 11 00 09 06 00 00"
          + "29 00 00 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 01 02 02 0F 01 03 00 02 04 12 00"
          + "17 11 01 09 06 00 00 16 00 00 FF 02 02 01 09 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16"
          + "01 00 02 03 0F 04 16 01 00 02 03 0F 05 16 01 00 02 03 0F 06 16 01 00 02 03 0F 07 16 01 00 02 03 0F 08"
          + "16 01 00 02 03 0F 09 16 01 00 01 00 02 04 12 00 01 11 00 09 06 00 00 60 01 0A FF 02 02 01 02 02 03 0F"
          + "01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 07 11 01 09 06 07 00 63 63 00 FF 02 02 01 08 02 03"
          + "0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 02 03 0F 04 16 01 00 02 03 0F 05 16 01 00 02"
          + "03 0F 06 16 01 00 02 03 0F 07 16 01 00 02 03 0F 08 16 01 00 01 04 02 02 0F 01 03 00 02 02 0F 02 03 00"
          + "02 02 0F 03 03 00 02 02 0F 04 03 00";

  public CosemClassInfosTest()
  {
  }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
  }

  @AfterClass
  public static void tearDownClass() throws Exception
  {
  }

  /**
   * Test of getInstance method, of class CosemClassInfos.
   */
  @Test
  public void testGetInstance() throws IOException, ValidationExecption
  {
    CoderDlmsData coderDlmsData = new CoderDlmsData();

    System.out.println("getInstance");
    CosemClassInfos result = CosemClassInfos.getInstance();
    assertNotNull(result);

    CosemAttributeInfo attributeInfo = result.getAttributeInfo(3, 0, 1);
    assertNotNull(attributeInfo.getValidator());

    attributeInfo = result.getAttributeInfo(15, 0, 2);
    assertNotNull(attributeInfo.getValidator());


    DlmsData data = coderDlmsData.decodeObject(CodingUtils.string2InputStream(OBJECT_LIST_V0));
    attributeInfo.getValidator().validate(data);


    attributeInfo = result.getAttributeInfo(15, 1, 2);
    assertNotNull(attributeInfo.getValidator());
    // attributeInfo.getValidator().validate(data);
  }
  
  
  
//
//  /**
//   * Just print all known class ids (helper)
//   */
//  @Test
//  public void testPrintClasses() throws IOException, ValidationExecption
//  {
//
//    System.out.println("getInstance");
//    CosemClassInfos result = CosemClassInfos.getInstance();
//    CosemClassInfo[] toArray =
//            result.cosemClassMap.values().toArray(new CosemClassInfo[0]);
//
//    Arrays.sort(toArray,new Comparator<CosemClassInfo>() {
//
//      @Override
//      public int compare(CosemClassInfo o1, CosemClassInfo o2)
//      {
//        return ((Integer) (o1.getClassId())).compareTo(o2.getClassId());
//      }
//    });
//
//
//    for (CosemClassInfo info : toArray)
//    {
//      String className=info.getClassName();
//      className= className.replace(' ', '_');
//      className= className.replace('-', '_');
//      className= className.toUpperCase();
//
//      System.out.println("public static final int " +className + " = " + info.getClassId()+";");
//
//    }
//
//
//
//  }

}
