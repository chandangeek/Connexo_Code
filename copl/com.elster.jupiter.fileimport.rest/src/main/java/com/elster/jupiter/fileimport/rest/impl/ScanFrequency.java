package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;

/**
 * Created by Lucian on 5/15/2015.
 */
public class ScanFrequency {

    public static Integer toScanFrequency(CronExpression cronExpression) {
        return 1;
    }
    public static CronExpression fromFrequency(Integer scanFrequency, CronExpressionParser cronExpressionParser){
        return cronExpressionParser.parse(String.format("0 0/1 * 1/1 * ? *")).get();
    }
}
