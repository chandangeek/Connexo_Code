package com.elster.protocolimpl.dlms.util;

import com.elster.dlms.types.basic.*;
import com.energyict.cbo.BusinessException;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * User: heuckeg
 * Date: 07.10.11
 * Time: 09:02
 */
public class TestRepetitiveDate {

    @Test
    public void dateStringToDlmsDateTestOk1() throws BusinessException {

        DlmsDate date = RepetitiveDate.dateStringToDlmsDate(" 2010 - 09 - 01 ");
        DlmsDate comp = new DlmsDate(2010, 9, 1);
        assertEquals(comp, date);
    }

    @Test
    public void dateStringToDlmsDateTestOk2() throws BusinessException {

        DlmsDate date = RepetitiveDate.dateStringToDlmsDate("*-09-01");
        DlmsDate comp = new DlmsDate(DlmsDate.YEAR_NOT_SPECIFIED, 9, 1);
        assertEquals(comp, date);
    }

    @Test
    public void dateStringToDlmsDateTestOk3() throws BusinessException {

        DlmsDate date = RepetitiveDate.dateStringToDlmsDate("*-*-01");
        DlmsDate comp = new DlmsDate(DlmsDate.YEAR_NOT_SPECIFIED, DlmsDate.MONTH_NOT_SPECIFIED, 1);
        assertEquals(comp, date);
    }

    @Test
    public void dateStringToDlmsDateTestOk4() throws BusinessException {

        DlmsDate date = RepetitiveDate.dateStringToDlmsDate("*-*-*");
        DlmsDate comp = new DlmsDate(DlmsDate.YEAR_NOT_SPECIFIED, DlmsDate.MONTH_NOT_SPECIFIED, DlmsDate.DAY_OF_MONTH_NOT_SPECIFIED);
        assertEquals(comp, date);
    }

    @Test
    public void dateStringToDlmsDateTestOk5() throws BusinessException {

        DlmsDate date = RepetitiveDate.dateStringToDlmsDate("SU");
        DlmsDate comp = new DlmsDate(DlmsDate.YEAR_NOT_SPECIFIED, DlmsDate.MONTH_NOT_SPECIFIED, DlmsDate.DAY_OF_MONTH_NOT_SPECIFIED, 7);
        assertEquals(comp, date);
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsDateTestNotOk1() throws BusinessException {

        RepetitiveDate.dateStringToDlmsDate("1*-*-*");
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsDateTestNotOk2() throws BusinessException {

        RepetitiveDate.dateStringToDlmsDate("*-1*-*");
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsDateTestNotOk3() throws BusinessException {

        RepetitiveDate.dateStringToDlmsDate("*-*-1*");
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsDateTestNotOk4() throws BusinessException {

        RepetitiveDate.dateStringToDlmsDate("--");
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsDateTestNotOk5() throws BusinessException {

        RepetitiveDate.dateStringToDlmsDate("2010-09");
    }


    @Test
    public void dateStringToDlmsTimeTestOk1() throws BusinessException {

        DlmsTime time = RepetitiveDate.dateStringToDlmsTime(" 10 : 11 : 12 ");
        DlmsTime comp = new DlmsTime(10, 11, 12, DlmsTime.NOT_SPECIFIED);
        assertEquals(comp, time);
    }

    @Test
    public void dateStringToDlmsTimeTestOk2() throws BusinessException {

        DlmsTime time = RepetitiveDate.dateStringToDlmsTime(" 10 : 11 ");
        DlmsTime comp = new DlmsTime(10, 11, 0, DlmsTime.NOT_SPECIFIED);
        assertEquals(comp, time);
    }

    @Test
    public void dateStringToDlmsTimeTestOk3() throws BusinessException {

        DlmsTime time = RepetitiveDate.dateStringToDlmsTime(" * : 11 : 12 ");
        DlmsTime comp = new DlmsTime(DlmsTime.NOT_SPECIFIED, 11, 12, DlmsTime.NOT_SPECIFIED);
        assertEquals(comp, time);
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsTimeTestNotOk1() throws BusinessException {

        RepetitiveDate.dateStringToDlmsTime(" 10 - 11");
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsTimeTestNotOk2() throws BusinessException {

        RepetitiveDate.dateStringToDlmsTime(" 10 ");
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsTimeTestNotOk3() throws BusinessException {

        RepetitiveDate.dateStringToDlmsTime("*:*");
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsTimeTestNotOk4() throws BusinessException {

        RepetitiveDate.dateStringToDlmsTime("*");
    }

    @Test
    public void dateStringToDlmsDateTimeTestOk1() throws BusinessException {

        DlmsDateTime dateTime = RepetitiveDate.dateStringToDlmsDateTime("2010-09-01 10:11:12");
        DlmsDate date = new DlmsDate(2010, 9, 1);
        DlmsTime time = new DlmsTime(10, 11, 12, DlmsTime.NOT_SPECIFIED);
        DlmsDateTime comp = new DlmsDateTime(date, time);
        assertEquals(comp, dateTime);
    }

    @Test
    public void dateStringToDlmsDateTimeTestOk2() throws BusinessException {

        DlmsDateTime dateTime = RepetitiveDate.dateStringToDlmsDateTime("MO 10:11");
        DlmsDate date = new DlmsDate(DlmsDate.YEAR_NOT_SPECIFIED, DlmsDate.MONTH_NOT_SPECIFIED, DlmsDate.DAY_OF_MONTH_NOT_SPECIFIED, 1);
        DlmsTime time = new DlmsTime(10, 11, 0, DlmsTime.NOT_SPECIFIED);
        DlmsDateTime comp = new DlmsDateTime(date, time);
        assertEquals(comp, dateTime);
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsDateTimeTestNotOk1() throws BusinessException {

        RepetitiveDate.dateStringToDlmsDateTime("2010-09-01;10:11:12");
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsDateTimeTestNotOk2() throws BusinessException {

        RepetitiveDate.dateStringToDlmsDateTime(" 10:11:12");
    }

    @Test(expected = BusinessException.class)
    public void dateStringToDlmsDateTimeTestNotOk3() throws BusinessException {

        RepetitiveDate.dateStringToDlmsDateTime("2010-09-01 ");
    }

    @Test
    public void substringTest() {
        String tag = "test";
        String test = "12345<test>1234567890<\\test>12345";
        int start = test.indexOf("<" + tag + ">");
        int end = test.indexOf("<\\" + tag + ">");
        System.out.println(test.substring(start + 2 + "test".length(),
                                          end));
    }
}
