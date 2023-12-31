/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.channelRegisterSpecifications;

public class SqlStatements {

    public String DEVICE_CONFIGURATION_SQL = "select DEVICECONFIGID from " +
            "(" +
                    "select DEVICECONFIGID, MODTIME from ( " +
                        "select DEVICECONFIGID, MODTIME from DDC_DEVICE WHERE id = {0, number, #} and MODTIME<= {1, number, #} " +
                        "union all " +
                        "select DEVICECONFIGID, MODTIME from DDC_DEVICEJRNL WHERE id = {0, number, #} and MODTIME<= {1, number, #} " +
            ") " +
            "order by MODTIME desc) " +
            "where ROWNUM = 1";

    public String MEASUREMENTTYPE_SQL = "select ID from " +
            "( " +
                    "select ID, MODTIME from ( " +
                        "select ID, MODTIME from MDS_MEASUREMENTTYPE WHERE readingtype = ''{0}'' and MODTIME <= {1,number, #} " +
                        "union all " +
                        "select ID, MODTIME from MDS_MEASUREMENTTYPEJRNL WHERE readingtype = ''{0}'' and MODTIME <= {1, number, #} " +
            ") " +
            "order by MODTIME desc) " +
            "where ROWNUM = 1";

    public String SPEC_SQL = "select obiscode, SourceType from" +
            "(" +
                    "select obiscode, MODTIME, SourceType from ( " +
                        "select obiscode, MODTIME, ''CHANNEL'' SourceType from DTC_CHANNELSPEC WHERE DEVICECONFIGID = {0, number, #} and channeltypeid = {1, number, #} and MODTIME <= {2, number, #} " +
                        "union all " +
                        "select obiscode, MODTIME, ''CHANNEL'' SourceType from DTC_CHANNELSPECJRNL WHERE DEVICECONFIGID = {0, number, #} and channeltypeid = {1, number, #} and MODTIME <= {2, number, #} " +
                        "union all " +
                        "select deviceobiscode, MODTIME, ''REGISTER'' SourceType from DTC_REGISTERSPEC WHERE DEVICECONFIGID = {0, number, #} and registertypeid = {1, number, #} and MODTIME<= {2, number, #} " +
                        "union all " +
                        "select deviceobiscode, MODTIME, ''REGISTER'' SourceType from DTC_REGISTERSPECJRNL WHERE DEVICECONFIGID = {0, number, #} and registertypeid = {1, number, #} and MODTIME<= {2, number, #} " +
           ") " +
            "order by MODTIME desc) " +
            "where ROWNUM = 1";
}
