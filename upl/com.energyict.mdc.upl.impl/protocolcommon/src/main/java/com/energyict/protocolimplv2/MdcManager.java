package com.energyict.protocolimplv2;

import com.energyict.mdc.meterdata.CollectedDataFactory;
import com.energyict.mdc.meterdata.CollectedDataFactoryProvider;
import com.energyict.util.IssueFactory;
import com.energyict.util.IssueFactoryProvider;

/**
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 12:04
 */
public class MdcManager {

    public static CollectedDataFactory getCollectedDataFactory(){
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
    }

    public static IssueFactory getIssueFactory(){
        return IssueFactoryProvider.instance.get().getIssueFactory();
    }
}
