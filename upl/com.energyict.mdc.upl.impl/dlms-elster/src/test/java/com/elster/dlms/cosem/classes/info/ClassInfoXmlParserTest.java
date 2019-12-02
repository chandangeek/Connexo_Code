/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.classes.info;

import java.util.Comparator;
import java.util.ArrayList;
import com.elster.dlms.cosem.classes.info.CosemClassInfo.IdAndVersion;
import java.util.Collections;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class ClassInfoXmlParserTest
{
  public ClassInfoXmlParserTest()
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
   * Test of parse method, of class CosemClassInfos.
   */
  @Test
  public void testParse() throws Exception
  {
    System.out.println("parse");
    CosemClassInfos instance = CosemClassInfos.getInstance();

    assertEquals(31, instance.getCosemClassMap().size());

    CosemClassInfo classInfo1 = instance.getCosemClassMap().get(new CosemClassInfo.IdAndVersion(1, 0));
    assertNotNull(classInfo1);

    assertEquals("Data", classInfo1.getClassName());
    assertEquals(2, classInfo1.getAttributes().size());

    CosemClassInfo classInfo3 = instance.getCosemClassMap().get(new CosemClassInfo.IdAndVersion(3, 0));
    assertNotNull(classInfo3);
    assertEquals("Register", classInfo3.getClassName());
    assertEquals(3, classInfo3.getAttributes().size());
    assertEquals(1, classInfo3.getMethods().size());
  }

  /**
   * Helper to get the English names out the xml file.
   */
  //@Test
  public void list() throws Exception
  {
    System.out.println("list");
    CosemClassInfos instance = CosemClassInfos.getInstance();

    Map<IdAndVersion, CosemClassInfo> cosemClassMap = instance.getCosemClassMap();

    ArrayList<CosemClassInfo> list = new ArrayList<CosemClassInfo>(cosemClassMap.values());

    Collections.sort(list, new Comparator<CosemClassInfo>()
    {
      //@Override
      public int compare(CosemClassInfo o1, CosemClassInfo o2)
      {
        if (o1.getClassId() < o2.getClassId())
        {
          return -1;
        }
        if (o1.getClassId() > o2.getClassId())
        {
          return 1;
        }
        return 0;
      }

    });

    for (CosemClassInfo info : list)
    {
      //System.out.println("" + info.getClassId() + " -" + info.getClassName()+"");
      System.out.println("#+++   " + info.getClassName());

      System.out.println("ClassName_CID_" + info.getClassId() + "=" + info.getClassName());
      for (CosemAttributeInfo attributeInfo : info.getAttributes())
      {
        System.out.println("AttributeName_CID_" + info.getClassId() + "_AID_" + attributeInfo.getId() + "="
                           +makeNameBetter(attributeInfo.getName()));
      }
      for (CosemMethodInfo methodInfo : info.getMethods())
      {
        System.out.println("MethodName_CID_" + info.getClassId() + "_MID_" + methodInfo.getNumber() + "="
                           + makeNameBetter(methodInfo.getName()));
      }
    }
  }

  private String makeNameBetter(String orginal)
  {
    String newName = orginal.replace("_", " ");

    if (newName.length() > 0)
    {
      String first = newName.substring(0, 1).toUpperCase();
      newName = first + newName.substring(1);
    }
    return newName;
  }

}