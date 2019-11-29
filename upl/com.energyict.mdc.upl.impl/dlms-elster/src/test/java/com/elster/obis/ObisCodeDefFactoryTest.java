/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;
import com.elster.obis.IRangeMap.Pair;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class ObisCodeDefFactoryTest {

    public ObisCodeDefFactoryTest() {
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
   * Test of getObisCodeDefList method, of class ObisCodeDefFactory.
   */
  @Test
  public void testBuildObisCodeDefList()
  {
    System.out.println("buildObisCodeDefList");
    //  ObisCodeDefFactory instance = new ObisCodeDefFactory();
    //ObisCodeDefList expResult = null;
    IRangeMap<ObisCodeDef> obisCodeDefList = ObisCodeDefFactory.getObisCodeDefList();

    ObisCodeDef def= obisCodeDefList.find(new ObisCode(0,0,25,4,0,255));
    assertNotNull(def);
    System.out.println(def);

  // assertEquals(341, result.size());
  }
  
    /**
   * Test of getObisCodeDefList method, of class ObisCodeDefFactory.
   */
  @Test
  public void testIntersections() throws IOException
  {
    System.out.println("intersections");
    //  ObisCodeDefFactory instance = new ObisCodeDefFactory();
    //ObisCodeDefList expResult = null;
    IRangeMap<ObisCodeDef> obisCodeDefList = ObisCodeDefFactory.getObisCodeDefList();

  //  FileWriter writer= new FileWriter("D:/test/istxt");

    for (int i=0; i<obisCodeDefList.getPairs().size();i++)
    {
      for (int j=i+1; j<obisCodeDefList.getPairs().size();j++)
      {
        Pair<ObisCodeDef> defA = obisCodeDefList.getPairs().get(i);
        Pair<ObisCodeDef> defB = obisCodeDefList.getPairs().get(j);
        if (defA.getRange().intersects(defB.getRange()))
        {
//          writer.append("intersection found:");
//          writer.append("\r\n");
//          writer.append(defA+"\t"+defA.getOrgLine());
//          writer.append("\r\n");
//          writer.append(defB+"\t"+defB.getOrgLine());
//          writer.append("\r\n");

          System.out.println("intersection found:");
          System.out.println(defA+"  |org:"+defA.toString());
          System.out.println(defB+"  |org:"+defB.toString());
        }
      }
    }
//    writer.close();

  // assertEquals(341, result.size());
  }



}