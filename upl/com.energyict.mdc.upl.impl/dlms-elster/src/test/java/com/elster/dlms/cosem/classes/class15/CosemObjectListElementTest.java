/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.classes.class15;

import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.apdu.coding.CoderDlmsData;
import com.elster.dlms.testutils.TestUtils;
import com.elster.dlms.types.data.DlmsData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CosemObjectListElementTest
{
  public CosemObjectListElementTest()
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
   * Test of buildElements method, of class CosemObjectListElement.<P>
   * Simply checks if the object list can be decoded without exceptions.
   */
  @Test
  public void testBuildElements() throws Exception
  {
    System.out.println("buildElements");
    CoderDlmsData coderDlmsData = new CoderDlmsData();


    DlmsData data = coderDlmsData.decodeObject(TestUtils.hexResourceFile2InputStream(
            "/com/elster/testfiles/objectlist.txt"));
//    CosemObjectListElement[] expResult = null;
    CosemObjectListElement[] result = CosemObjectListElement.buildElements(data);
    assertEquals(477, result.length);
  }

  /**
   * Test of buildElements method, of class CosemObjectListElement.<P>
   * Simply checks if the object list can be decoded without exceptions.
   */
  @Test
  public void testToDlmsData() throws Exception
  {
    System.out.println("to dlms data");
    CoderDlmsData coderDlmsData = new CoderDlmsData();

    DlmsDataArray data = (DlmsDataArray)coderDlmsData.decodeObject(TestUtils.hexResourceFile2InputStream(
            "/com/elster/testfiles/objectlist.txt"));
    CosemObjectListElement[] elements = CosemObjectListElement.buildElements(data);

    assertEquals(data.size(), elements.length);

    for (int i = 0; i < elements.length; i++)
    {
      DlmsData expected = data.get(i);
      DlmsData result = elements[i].toDlmsData();
      //Better output:     
//      if (!expected.equals(result))
//      {
//
//        System.out.println("Expected: " + expected);
//        System.out.println("Result: " + result);
//      }

      assertEquals(expected, result);
    }

  }

}
