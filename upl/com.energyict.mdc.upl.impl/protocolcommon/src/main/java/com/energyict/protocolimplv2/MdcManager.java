package com.energyict.protocolimplv2;

import com.energyict.mdc.exceptions.ComServerExceptionFactory;
import com.energyict.mdc.exceptions.ComServerExceptionFactoryProvider;
import com.energyict.mdc.meterdata.CollectedDataFactory;
import com.energyict.mdc.meterdata.CollectedDataFactoryProvider;
import com.energyict.util.IssueCollector;
import com.energyict.util.IssueCollectorProvider;

/**
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 12:04
 */
public class MdcManager {

    public static ComServerExceptionFactory getComServerExceptionFactory() {
        return ComServerExceptionFactoryProvider.instance.get().getComServerExceptionFactory();
    }

    public static CollectedDataFactory getCollectedDataFactory(){
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
    }

    public static IssueCollector getIssueCollector(){
        return IssueCollectorProvider.instance.get().getIssueCollector();
    }
}
