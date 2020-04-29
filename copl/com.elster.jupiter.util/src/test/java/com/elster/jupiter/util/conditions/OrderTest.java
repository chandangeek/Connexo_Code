package com.elster.jupiter.util.conditions;

import com.elster.jupiter.util.exception.NoFieldSpecifiedException;
import com.elster.jupiter.util.exception.SqlInjectionException;

import org.junit.Assert;
import org.junit.Test;

public class OrderTest {

    @Test(expected = NoFieldSpecifiedException.class)
    public void testEmptyString() {
        Order.ascending("   ").getName();
    }

    @Test
    public void testValidFieldNames() {
        String col1 = "col1";
        Assert.assertEquals(col1, Order.ascending(col1).getName());
        String col2 = " col2   ";
        Assert.assertEquals(col2, Order.ascending(col2).getName());
        String col3 = "   col_3     ";
        Assert.assertEquals(col3, Order.ascending(col3).getName());
        String colFunction1 = "f(col1,col2)";
        Assert.assertEquals(colFunction1, Order.ascending(colFunction1).getName());
        String colFunction2 = "   f(col1,col2)     ";
        Assert.assertEquals(colFunction2, Order.ascending(colFunction2).getName());
    }

    @Test
    public void testMultipleColumns() {
        String col1 = "col1,col2";
        Assert.assertEquals(col1, Order.ascending(col1).getName());
        String col2 = " col1,col2     ";
        Assert.assertEquals(col2, Order.ascending(col2).getName());
    }

    @Test(expected = SqlInjectionException.class)
    public void testMultiwordNotAllowed() {
        String col1 = "col1 col2";
        Assert.assertEquals(col1, Order.ascending(col1).getName());
    }

    @Test(expected = SqlInjectionException.class)
    public void testNewLineNotAllowed() {
        String col1 = "col1\ncol2";
        Assert.assertEquals(col1, Order.ascending(col1).getName());
    }

    @Test(expected = SqlInjectionException.class)
    public void testCommentNotAllowed() {
        String col1 = "col1 col2--";
        Assert.assertEquals(col1, Order.ascending(col1).getName());
    }

    @Test(expected = SqlInjectionException.class)
    public void testEndCommandNotAllowed() {
        String col1 = ";col1 col2";
        Assert.assertEquals(col1, Order.ascending(col1).getName());
    }






}
