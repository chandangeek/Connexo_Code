/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.classes.class03;

import com.elster.dlms.types.data.DlmsDataLong;
import com.elster.dlms.types.data.DlmsDataFloat64;
import com.elster.dlms.types.data.DlmsDataFloat32;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataFloatingPoint;
import com.elster.dlms.types.data.DlmsDataLong64Unsigned;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class ScalerUnitTest
{
  public ScalerUnitTest()
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
   * Test of scale method, of class ScalerUnit.
   */
  @Test
  public void testScaleFloat32()
  {
    System.out.println("scale");
    DlmsData value = new DlmsDataFloat32(20272.2F);
    ScalerUnit instance = new ScalerUnit(0, Unit.COUNT);
    BigDecimal expResult = new BigDecimal("20272.2");
    BigDecimal result = instance.scale(value);
    assertEquals(expResult, result);
  }

  @Test
  public void testScaleFloat32_2()
  {
    System.out.println("scale 2");
    DlmsData value = new DlmsDataFloat32(20272.2F);
    ScalerUnit instance = new ScalerUnit(-1, Unit.COUNT);
    BigDecimal expResult = new BigDecimal("2027.22");
    BigDecimal result = instance.scale(value);
    assertEquals(expResult, result);
  }

  /**
   * Test of scale method, of class ScalerUnit.
   */
  @Test
  public void testScaleFloatingPoint()
  {
    System.out.println("scale 3");
    DlmsData value = new DlmsDataFloatingPoint(20272.2F);
    ScalerUnit instance = new ScalerUnit(0, Unit.COUNT);
    BigDecimal expResult = new BigDecimal("20272.2");
    BigDecimal result = instance.scale(value);
    assertEquals(expResult, result);
  }

  /**
   * Test of scale method, of class ScalerUnit.
   */
  @Test
  public void testScaleFloat64()
  {
    System.out.println("scale 4");
    double dblValue = 20272.233416564;
    DlmsData value = new DlmsDataFloat64(dblValue);
    ScalerUnit instance = new ScalerUnit(0, Unit.COUNT);
    BigDecimal expResult = BigDecimal.valueOf(dblValue);
    BigDecimal result = instance.scale(value);
    assertEquals(expResult, result);
  }

  /**
   * Test of scale method, of class ScalerUnit.
   */
  @Test
  public void testScaleFloat64_2()
  {
    System.out.println("scale 5");
    double dblValue = 20272.233416564;
    DlmsData value = new DlmsDataFloat64(dblValue);
    ScalerUnit instance = new ScalerUnit(-3, Unit.COUNT);
    BigDecimal expResult = BigDecimal.valueOf(dblValue).scaleByPowerOfTen(-3);
    BigDecimal result = instance.scale(value);
    assertEquals(expResult, result);
  }

  /**
   * Test of scale method, of class ScalerUnit.
   */
  @Test
  public void testScaleLong64Unsigned()
  {
    System.out.println("scale 6");
    DlmsData value = new DlmsDataLong64Unsigned(DlmsDataLong64Unsigned.MAX_VALUE);
    ScalerUnit instance = new ScalerUnit(-4, Unit.COUNT);
    BigDecimal expResult = new BigDecimal(DlmsDataLong64Unsigned.MAX_VALUE).scaleByPowerOfTen(-4);
    BigDecimal result = instance.scale(value);
    assertEquals(expResult, result);
  }

  /**
   * Test of scale method, of class ScalerUnit.
   */
  @Test
  public void testScaleLong64Unsigned_2()
  {
    System.out.println("scale 7");
    DlmsData value = new DlmsDataLong64Unsigned(DlmsDataLong64Unsigned.MAX_VALUE.subtract(BigInteger.ONE));
    ScalerUnit instance = new ScalerUnit(-4, Unit.COUNT);
    BigDecimal expResult = new BigDecimal(DlmsDataLong64Unsigned.MAX_VALUE.subtract(BigInteger.ONE)).
            scaleByPowerOfTen(-4);
    BigDecimal result = instance.scale(value);
    assertEquals(expResult, result);
  }

  /**
   * Test of scale method, of class ScalerUnit.
   */
  @Test
  public void testScaleLong()
  {
    System.out.println("scale 8");
    DlmsData value = new DlmsDataLong(32767);
    ScalerUnit instance = new ScalerUnit(-4, Unit.COUNT);
    BigDecimal expResult = new BigDecimal("3.2767");
    BigDecimal result = instance.scale(value);
    assertEquals(expResult, result);
  }

  /**
   * Test of scale method, of class ScalerUnit.
   */
  @Test
  public void testScaleLong2()
  {
    System.out.println("scale 9");
    DlmsData value = new DlmsDataLong(-767);
    ScalerUnit instance = new ScalerUnit(-4, Unit.COUNT);
    BigDecimal expResult = new BigDecimal("-0.0767");
    BigDecimal result = instance.scale(value);
    assertEquals(expResult, result);
  }

}
