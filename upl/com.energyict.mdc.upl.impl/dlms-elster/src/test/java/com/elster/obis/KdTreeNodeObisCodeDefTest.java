/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.obis;

import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import com.elster.dlms.types.basic.ObisCode;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class KdTreeNodeObisCodeDefTest
{
  public KdTreeNodeObisCodeDefTest()
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
   * Test of findDef method, of class KdTreeNodeObisCodeDef.
   */
  @Test
  public void testFindDef()
  {
    System.out.println("findDef");
    ObisCode obisCode = new ObisCode(0, 0, 25, 4, 0, 255);
    KdTreeRangeMap<ObisCodeDef> instance = new KdTreeRangeMap<ObisCodeDef>(ObisCodeDefFactory.getObisCodeDefList());
    assertNotNull(ObisCodeDefFactory.getObisCodeDefList().find(obisCode)); // ensure that the def can be found
    ObisCodeDef result = instance.find(obisCode);
    assertNotNull(result);
  }

//  @Test
  public void testMapItalyCodes() throws IOException
  {
    System.out.println("find Italy OBIS codes");
    InputStream stream = ObisFileReaderTest.class.getResourceAsStream(
            "/com/elster/testfiles/codeListItaly_1.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    try
    {
      KdTreeRangeMap<ObisCodeDef> instance = new KdTreeRangeMap<ObisCodeDef>(ObisCodeDefFactory.getObisCodeDefList());

      String codeLine = reader.readLine();


      //int lineNo=1;

      while (codeLine != null)
      {
        //System.out.println( (lineNo++) +": "+code);
        String[] codeLineParts = codeLine.split("!");
        ObisCode obisCode = new ObisCode(codeLineParts[0]);

        ObisCodeDef def = instance.find(obisCode);
        ObisCodeDef def2 = ObisCodeDefFactory.getObisCodeDefList().find(obisCode);

        assertEquals(def2, def);

        if (def == null)
        {
          System.out.println("not found!" + codeLineParts[0] + "!" + codeLineParts[1] + "!");
        }

        codeLine = reader.readLine();
      }
    }
    finally
    {
      reader.close();
    }
  }

  @Test
  public void testKdTreeSpeed() throws IOException
  {
    System.out.println("speed test");
    InputStream stream = ObisFileReaderTest.class.getResourceAsStream(
            "/com/elster/testfiles/codeListItaly_1.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    try
    {

      KdTreeRangeMap<ObisCodeDef> instance = new KdTreeRangeMap<ObisCodeDef>(ObisCodeDefFactory.getObisCodeDefList());
      IRangeMap<ObisCodeDef> obisCodeDefList = ObisCodeDefFactory.getObisCodeDefList();

      String codeLine = reader.readLine();

      List<ObisCode> codeList = new ArrayList<ObisCode>();

      long timeList;
      long timeKDTree;

      while (codeLine != null)
      {
        String[] codeLineParts = codeLine.split("!");
        ObisCode obisCode = new ObisCode(codeLineParts[0]);
        codeList.add(obisCode);
        codeLine = reader.readLine();
      }

      long startTime = System.currentTimeMillis();
      for (int i = 0; i < 100; i++)
      {
        for (ObisCode obisCode : codeList)
        {
          obisCodeDefList.find(obisCode);
        }
      }
      long endTime = System.currentTimeMillis();
      long duration = endTime - startTime;
      timeList = duration;

      System.out.println("Search time list: " + duration + "ms");

      startTime = System.currentTimeMillis();
      instance.propagateAll();
      endTime = System.currentTimeMillis();
      duration = endTime - startTime;

      System.out.println("Propagation time: " + duration + "ms");


      startTime = System.currentTimeMillis();
      for (int i = 0; i < 10000; i++) //die höhere Durchlaufzahl wird bei der Zeitangabe wieder herausgerechnet.
      {
        for (ObisCode obisCode : codeList)
        {
          instance.find(obisCode);
        }
      }
      endTime = System.currentTimeMillis();
      duration = endTime - startTime;
      timeKDTree = duration / 100;
      System.out.println(String.format("Search time KD-Tree: %d.%02dms", (duration / 100), (duration % 100)));


      assertTrue("The performance of the KD-Tree should be much better than the performance of the list",
                 timeKDTree * 10 < timeList);
    }
    finally
    {
      reader.close();
    }
  }

//  @Test
//  public void testKdTreeStructure() throws IOException
//  {
//    System.out.println("structure test");
//
//    KdTreeRangeMap<ObisCodeDef> instance = new KdTreeRangeMap<ObisCodeDef>(ObisCodeDefFactory.getObisCodeDefList());
//
//    List<String> leaves = new ArrayList<String>();
//    List<String> inner = new ArrayList<String>();
//
//    instance.analyse(leaves, inner);
//
//    for (String s : inner)
//    {
//      System.out.println(s);
//    }
//
//    for (String s : leaves)
//    {
//      System.out.println(s);
//    }
//  }

}
