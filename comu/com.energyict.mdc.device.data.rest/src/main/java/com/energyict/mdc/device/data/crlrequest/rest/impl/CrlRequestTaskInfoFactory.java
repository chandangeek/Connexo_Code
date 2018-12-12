package com.energyict.mdc.device.data.crlrequest.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.rest.CrlRequestTaskPropertyInfo;

import java.util.logging.Level;

public class CrlRequestTaskInfoFactory {

    public CrlRequestTaskInfoFactory() {
    }

    public CrlRequestTaskPropertyInfo asInfo(CrlRequestTaskProperty crlRequestTaskProperty) {
        CrlRequestTaskPropertyInfo info = new CrlRequestTaskPropertyInfo();
        info.securityAccessor = new IdWithNameInfo(crlRequestTaskProperty.getSecurityAccessor().getKeyAccessorType().getId(), crlRequestTaskProperty.getSecurityAccessor()
                .getKeyAccessorType()
                .getName());
        info.caName = crlRequestTaskProperty.getCaName();
        int logLevel = crlRequestTaskProperty.getRecurrentTask().getLogLevel();
        info.logLevel = new IdWithNameInfo(logLevel, getLogLevel(logLevel));
        PeriodicalScheduleExpression periodicalScheduleExpression = (PeriodicalScheduleExpression) crlRequestTaskProperty.getRecurrentTask().getScheduleExpression();
        info.periodicalExpressionInfo = PeriodicalExpressionInfo.from(periodicalScheduleExpression);
        info.nextRun = crlRequestTaskProperty.getRecurrentTask().getNextExecution();
        info.task = new IdWithNameInfo(crlRequestTaskProperty.getRecurrentTask().getId(), crlRequestTaskProperty.getRecurrentTask().getName());
        return info;
    }

    private String getLogLevel(int level) {
        if (level == Level.SEVERE.intValue()) {
            return "Error";
        } else if (level == Level.WARNING.intValue()) {
            return "Warning";
        } else if (level == Level.INFO.intValue()) {
            return "Information";
        } else if (level == Level.FINE.intValue()) {
            return "Debug";
        } else if (level == Level.FINEST.intValue()) {
            return "Trace";
        } else {
            return "";
        }
    }
}
