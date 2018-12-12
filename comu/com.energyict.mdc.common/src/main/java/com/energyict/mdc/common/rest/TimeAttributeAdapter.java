/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.cbo.TimeAttribute;

public class TimeAttributeAdapter extends MapBasedXmlAdapter<TimeAttribute> {

    public TimeAttributeAdapter() {
        register("", TimeAttribute.NOTAPPLICABLE);
        register("Not applicable", TimeAttribute.NOTAPPLICABLE);
        register("Present", TimeAttribute.PRESENT);
        register("Previous", TimeAttribute.PREVIOUS);
        register("Specified fixed block", TimeAttribute.SPECIFIEDFIXEDBLOCK);
        register("Specified interval", TimeAttribute.SPECIFIEDINTERVAL);
        register("Specified rolling block", TimeAttribute.SPECIFIEDROLLINGBLOCK);
        register("Fixed block 1 minute", TimeAttribute.FIXEDBLOCK1MIN);
        register("Fixed block 5 minutes", TimeAttribute.FIXEDBLOCK5MIN);
        register("Fixed block 10 minutes", TimeAttribute.FIXEDBLOCK10MIN);
        register("Fixed block 15 minutes", TimeAttribute.FIXEDBLOCK15MIN);
        register("Fixed block 20 minutes", TimeAttribute.FIXEDBLOCK20MIN);
        register("Fixed block 30 minutes", TimeAttribute.FIXEDBLOCK30MIN);
        register("Fixed block 60 minutes", TimeAttribute.FIXEDBLOCK60MIN);
        register("1 minute", TimeAttribute.MINUTE1);
        register("2 minutes", TimeAttribute.MINUTE2);
        register("3 minutes", TimeAttribute.MINUTE3);
        register("5 minutes", TimeAttribute.MINUTE5);
        register("10 minutes", TimeAttribute.MINUTE10);
        register("12 minutes", TimeAttribute.MINUTE12);
        register("15 minutes", TimeAttribute.MINUTE15);
        register("20 minutes", TimeAttribute.MINUTE20);
        register("30 minutes", TimeAttribute.MINUTE30);
        register("60 minutes", TimeAttribute.MINUTE60);
        register("2 hours", TimeAttribute.HOUR2);
        register("3 hours", TimeAttribute.HOUR3);
        register("4 hours", TimeAttribute.HOUR4);
        register("6 hours", TimeAttribute.HOUR6);
        register("12 hours", TimeAttribute.HOUR12);
        register("24 hours", TimeAttribute.HOUR24);
        register("Rolling block 5 minutes with 1 minute sub intervals", TimeAttribute.ROLLING5_1);
        register("Rolling block 10 minutes with 1 minute sub intervals", TimeAttribute.ROLLING10_1);
        register("Rolling block 10 minutes with 2 minute sub intervals", TimeAttribute.ROLLING10_2);
        register("Rolling block 10 minutes with 5 minute sub intervals", TimeAttribute.ROLLING10_5);
        register("Rolling block 15 minutes with 1 minute sub intervals", TimeAttribute.ROLLING15_1);
        register("Rolling block 15 minutes with 3 minute sub intervals", TimeAttribute.ROLLING15_3);
        register("Rolling block 15 minutes with 5 minute sub intervals", TimeAttribute.ROLLING15_5);
        register("Rolling block 30 minutes with 2 minute sub intervals", TimeAttribute.ROLLING30_2);
        register("Rolling block 30 minutes with 3 minute sub intervals", TimeAttribute.ROLLING30_3);
        register("Rolling block 30 minutes with 5 minute sub intervals", TimeAttribute.ROLLING30_5);
        register("Rolling block 30 minutes with 6 minute sub intervals", TimeAttribute.ROLLING30_6);
        register("Rolling block 30 minutes with 10 minute sub intervals", TimeAttribute.ROLLING30_10);
        register("Rolling block 30 minutes with 15 minute sub intervals", TimeAttribute.ROLLING30_15);
        register("Rolling block 60 minutes with 4 minute sub intervals", TimeAttribute.ROLLING60_4);
        register("Rolling block 60 minutes with 5 minute sub intervals", TimeAttribute.ROLLING60_5);
        register("Rolling block 60 minutes with 6 minute sub intervals", TimeAttribute.ROLLING60_6);
        register("Rolling block 60 minutes with 10 minute sub intervals", TimeAttribute.ROLLING60_10);
        register("Rolling block 60 minutes with 12 minute sub intervals", TimeAttribute.ROLLING60_12);
        register("Rolling block 60 minutes with 15 minute sub intervals", TimeAttribute.ROLLING60_15);
        register("Rolling block 60 minutes with 20 minute sub intervals", TimeAttribute.ROLLING60_20);
        register("Rolling Block 60 minutes with 30 minute sub intervals", TimeAttribute.ROLLING60_30);
    }
}
