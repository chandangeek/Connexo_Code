/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elster.jupiter.util.cron.impl;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class CronExpressionTest {

    private static final String[] VERSIONS = new String[]{"1.5.2"};

    private static final TimeZone EST_TIME_ZONE = TimeZone.getTimeZone("US/Eastern");

    /**
     * Get the object to serialize when generating serialized file for future
     * tests, and against which to validate deserialized object.
     */
    protected Object getTargetObject() throws ParseException {
        QuartzCronExpression cronExpression = new QuartzCronExpression("0 15 10 * * ? 2005");
        cronExpression.setTimeZone(EST_TIME_ZONE);

        return cronExpression;
    }

    /**
     * Get the Quartz versions for which we should verify
     * serialization backwards compatibility.
     */
    protected String[] getVersions() {
        return VERSIONS;
    }

    /**
     * Verify that the target object and the object we just deserialized
     * match.
     */
    protected void verifyMatch(Object target, Object deserialized) {
        QuartzCronExpression targetCronExpression = (QuartzCronExpression) target;
        QuartzCronExpression deserializedCronExpression = (QuartzCronExpression) deserialized;

        assertNotNull(deserializedCronExpression);
        assertEquals(targetCronExpression.getCronExpression(), deserializedCronExpression.getCronExpression());
        assertEquals(targetCronExpression.getTimeZone(), deserializedCronExpression.getTimeZone());
    }

    /*
     * Test method for 'org.quartz.CronExpression.isSatisfiedBy(Date)'.
     */
    @Test
    public void testIsSatisfiedBy() throws Exception {
        QuartzCronExpression cronExpression = new QuartzCronExpression("0 15 10 * * ? 2005");

        Calendar cal = Calendar.getInstance();

        cal.set(2005, Calendar.JUNE, 1, 10, 15, 0);
        assertTrue(cronExpression.isSatisfiedBy(cal.getTime()));

        cal.set(Calendar.YEAR, 2006);
        assertFalse(cronExpression.isSatisfiedBy(cal.getTime()));

        cal = Calendar.getInstance();
        cal.set(2005, Calendar.JUNE, 1, 10, 16, 0);
        assertFalse(cronExpression.isSatisfiedBy(cal.getTime()));

        cal = Calendar.getInstance();
        cal.set(2005, Calendar.JUNE, 1, 10, 14, 0);
        assertFalse(cronExpression.isSatisfiedBy(cal.getTime()));
    }

    /*
     * QUARTZ-571: Showing that expressions with months correctly serialize.
     */
    @Test
    public void testQuartz571() throws Exception {
        QuartzCronExpression cronExpression = new QuartzCronExpression("19 15 10 4 Apr ? ");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(cronExpression);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        QuartzCronExpression newExpression = (QuartzCronExpression) ois.readObject();

        assertEquals(newExpression.getCronExpression(), cronExpression.getCronExpression());

        // if broken, this will throw an exception
        newExpression.getNextValidTimeAfter(new Date());
    }

    /*
     * QUARTZ-574: Showing that storeExpressionVals correctly calculates the month number
     */
    @Test(expected = IllegalArgumentException.class)
    public void testQuartz574() throws Exception {
        new QuartzCronExpression("* * * * Foo ? ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuartz574_1() throws Exception {
        new QuartzCronExpression("* * * * Jan-Foo ? ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuartz621_0() throws Exception {
        new QuartzCronExpression("0 0 * * * *");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuartz621_1() throws ParseException {
        new QuartzCronExpression("0 0 * 4 * *");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuartz621_2() throws ParseException {
        new QuartzCronExpression("0 0 * * * 4");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuartz640_0() throws ParseException {
        new QuartzCronExpression("0 43 9 1,5,29,L * ?");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuartz640_1() throws ParseException {
        new QuartzCronExpression("0 43 9 ? * SAT,SUN,L");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuartz640_2() throws ParseException {
        new QuartzCronExpression("0 43 9 ? * 6,7,L");
    }

    @Test
    public void testQuartz640_3() throws ParseException {
        new QuartzCronExpression("0 43 9 ? * 5L");
    }


}