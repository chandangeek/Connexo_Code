/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData.DataType;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author osse
 */
public class ObisFileReaderTest
{
  private InputStream createTestFileStream(final String name)
  {

    InputStream stream = ObisFileReader.class.getClassLoader().getResourceAsStream(
            name + ".txt");
    return stream;
  }

  /**
   * Test of read method, of class ObisFileReader.
   */
  @Test
  public void testReadGas() throws Exception
  {
    System.out.println("read Gas");
    ObisFileReader instance = new ObisFileReader(createTestFileStream("obisCodesGas"));
    //    ObisFileReader instance = new ObisFileReader(createTestFileStream("obisCodesAbstract"));
    //    List expResult = null;
   IRangeMap<ObisCodeDef> result=  new FlatRangeMap<ObisCodeDef>(instance.read());


    ObisCodeDef def2 = result.find(new ObisCode(7, 0, 70, 60, 0, 255));
    assertNotNull(def2);
    System.out.println(def2.toString());
    System.out.println(def2.describe(new ObisCode(7, 0, 70, 60, 0, 255)));

    ObisCodeDef def3 = result.find(new ObisCode(7, 0, 41, 0, 0, 255));
    assertNotNull(def3);
    System.out.println(def3.toString());
    System.out.println(def3.describe(new ObisCode(7, 0, 41, 0, 0, 255)));

    //	7	b	0	0	0-9	255	Ch. $B	Complete combined gas ID		#$(E+1)		1	3, 4	6, 9, 10, 17, 18		The gas ID numbers are instances of the IC Data. If IC = 3 "Register" or IC = 4 "Extended register" is used, scaler = 0 and unit = 255.				
    ObisCodeDef def4 = result.find(new ObisCode(7, 5, 0, 0, 9, 255));
    assertNotNull(def4);
    Set<DataType> expectedDataTypes =
            EnumSet.of(DataType.DOUBLE_LONG_UNSIGNED, DataType.OCTET_STRING, DataType.VISIBLE_STRING,
                       DataType.UNSIGNED, DataType.LONG_UNSIGNED);
    assertEquals(expectedDataTypes, def4.getValueAttributeTypes());
    System.out.println(def3.toString());
    System.out.println(def3.describe(new ObisCode(7, 0, 41, 0, 0, 255)));


//    int numbersOnly = 0;
//
//    for (ObisCodeDef d : result)
//    {
//      if (d.getValueAttributeTypes() != null && d.getValueAttributeTypes().hasNumberTypesOnly())
//      {
//        numbersOnly++;
//      }
//    }
//
//    System.out.println("");
//    System.out.println("---- Numbers only: " + numbersOnly);


//    System.out.println("");
//    System.out.println("---- All definitions ----");
//
//    int counter=0;
//
//    for (ObisCodeDef d: result)
//    {
//      System.out.println(counter+" "+d.toString());
//      counter++;
//    }
    assertNotNull(result.find(new ObisCode(7, 0, 12, 2, 3, 255)));
  }

  //@Test
  public void testCompareAgainstItaly() throws Exception
  {
    System.out.println("compare against italy");
    ObisFileReader instance = new ObisFileReader(createTestFileStream("obisCodesGas"));
    IRangeMap<ObisCodeDef> result=  new FlatRangeMap<ObisCodeDef>(instance.read());    



//    System.out.println("");
//    System.out.println("---- All definitions ----");
//
//    int counter=0;
//
//    for (ObisCodeDef d: result)
//    {
//      System.out.println(counter+" "+d.toString());
//      counter++;
//    }

    System.out.println("");
    System.out.println("---- against List Italy  ----");

    InputStream stream = ObisFileReaderTest.class.getResourceAsStream(
            "/com/elster/testfiles/codeListItaly.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    try
    {

      String code = reader.readLine();

      //int lineNo=1;

      while (code != null)
      {
        ObisCode obisCode = new ObisCode(code);
        //System.out.println( (lineNo++) +": "+code);
        ObisCodeDef def = result.find(obisCode);

        if (def == null)
        {
          System.out.println("not found: " + code);
        }

        code = reader.readLine();
      }
    }
    finally
    {
      reader.close();
    }
  }

  
  @Test
  public void testReadAbstract() throws Exception
  {
    System.out.println("read Abstract");
    ObisFileReader instance = new ObisFileReader(createTestFileStream("obisCodesAbstract"));
    IRangeMap<ObisCodeDef> result=  new FlatRangeMap<ObisCodeDef>(instance.read());    
    ObisCodeDef timeOfPowerFailure = result.find(new ObisCode(0,1,96,7,10,255));

    assertNotNull(timeOfPowerFailure);
    assertEquals(";Ch. $B;Time of power failure;in all three phases;;", timeOfPowerFailure.getDescription());
  }
  
  @Test
  public void testReadAbstract2() throws Exception
  {
    final ObisCode clockObject = new ObisCode(0, 0, 1, 0, 0, 255);

    System.out.println("read Abstract");
    ObisFileReader instance = new ObisFileReader(createTestFileStream("obisCodesAbstract"));
    IRangeMap<ObisCodeDef> result=  new FlatRangeMap<ObisCodeDef>(instance.read());    

    
//    System.out.println("");
//    System.out.println("---- All abstract definitions ----");
//    for (Pair<ObisCodeDef> d: result.getPairs())
//    {
//      System.out.println(d.toString());
//    }
    
    
    


    assertNotNull(result.find(clockObject));
    assertEquals("Clock object", result.find(clockObject).getGroupDescription(2));




  }

}
