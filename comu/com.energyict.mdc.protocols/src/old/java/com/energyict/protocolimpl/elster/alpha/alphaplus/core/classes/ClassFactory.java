/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ClassFactory.java
 *
 * Created on 12 juli 2005, 10:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocolimpl.elster.alpha.core.Alpha;
import com.energyict.protocolimpl.elster.alpha.core.connection.CommandFactory;

import java.io.IOException;

//import com.energyict.protocolimpl.elster.alphaplus.*;

/**
 *
 * @author Koen
 */
public class ClassFactory {

    Alpha alpha;

    // uncached
    Class9Status1 class9Status1=null; // current time/date is in this class
    Class17LoadProfileData class17LoadProfileData=null;
    Class16EventLogData class16EventLogData=null;

    // cached lazy initialized classes
    Class0ComputationalConfiguration class0ComputationalConfiguration=null;
    Class2IdentificationAndDemandData class2IdentificationAndDemandData=null;
    Class6MeteringFunctionBlock class6MeteringFunctionBlock=null;
    Class7MeteringFunctionBlock class7MeteringFunctionBlock=null;
    Class8FirmwareConfiguration class8FirmwareConfiguration=null;
    Class33ModemConfigurationInfo class33ModemConfigurationInfo=null;
    Class32ModemAlarmCallConfiguration class32ModemAlarmCallConfiguration=null;
    Class31ModemBillingCallConfiguration class31ModemBillingCallConfiguration=null;
    Class11BillingData class11BillingData=null;
    Class12PreviousMonthBillingData class12PreviousMonthBillingData=null;
    Class13PreviousSeasonBillingData class13PreviousSeasonBillingData=null;
    Class14LoadProfileConfiguration class14LoadProfileConfiguration=null;
    Class10Status2 class10Status2=null;
    Class15EventLogConfiguration class15EventLogConfiguration=null;


    /** Creates a new instance of ClassFactory */
    public ClassFactory(Alpha alpha) {
        this.alpha=alpha;
    }

    public CommandFactory getCommandFactory() {
        return alpha.getCommandFactory();
    }

    public Alpha getAlpha() {
        return alpha;
    }

    // **************************************************************************************************
    // uncached classes
    public Class9Status1 getClass9Status1() throws IOException {
        Class9Status1 class9Status1 = new Class9Status1(this);
        class9Status1.build();
        return class9Status1;
    }

    public Class17LoadProfileData getClass17LoadProfileData(int nrOfDays) throws IOException {
        //getAlphaPlus().getCommandFactory().getFunctionWithDataCommand().PacketSize(4);
        Class17LoadProfileData class17LoadProfileData = new Class17LoadProfileData(this);
        class17LoadProfileData.setNrOfDays(nrOfDays);
        class17LoadProfileData.build();
        return class17LoadProfileData;
    }

    public Class16EventLogData getClass16EventLogData() throws IOException {
        Class16EventLogData class16EventLogData = new Class16EventLogData(this);
        class16EventLogData.build();
        return class16EventLogData;
    }

    // **************************************************************************************************
    // cached lazy initialized classes

    public Class32ModemAlarmCallConfiguration getClass32ModemAlarmCallConfiguration() throws IOException {
        if (class32ModemAlarmCallConfiguration == null) {
            class32ModemAlarmCallConfiguration = new Class32ModemAlarmCallConfiguration(this);
            class32ModemAlarmCallConfiguration.build();
        }
        return class32ModemAlarmCallConfiguration;
    }

    public Class31ModemBillingCallConfiguration getClass31ModemBillingCallConfiguration() throws IOException {
        if (class31ModemBillingCallConfiguration == null) {
            class31ModemBillingCallConfiguration = new Class31ModemBillingCallConfiguration(this);
            class31ModemBillingCallConfiguration.build();
        }
        return class31ModemBillingCallConfiguration;
    }

    public Class0ComputationalConfiguration getClass0ComputationalConfiguration() throws IOException {
        if (class0ComputationalConfiguration == null) {
            class0ComputationalConfiguration = new Class0ComputationalConfiguration(this);
            class0ComputationalConfiguration.build();
        }
        return class0ComputationalConfiguration;
    }

    public Class2IdentificationAndDemandData getClass2IdentificationAndDemandData() throws IOException {
        if (class2IdentificationAndDemandData == null) {
            class2IdentificationAndDemandData = new Class2IdentificationAndDemandData(this);
            class2IdentificationAndDemandData.build();
        }
        return class2IdentificationAndDemandData;
    }

    public Class6MeteringFunctionBlock getClass6MeteringFunctionBlock() throws IOException {
        if (class6MeteringFunctionBlock == null) {
            class6MeteringFunctionBlock = new Class6MeteringFunctionBlock(this);
            class6MeteringFunctionBlock.build();
        }
        return class6MeteringFunctionBlock;
    }

    public long getSerialNumber() throws IOException {
        class7MeteringFunctionBlock = new Class7MeteringFunctionBlock(this);
        class7MeteringFunctionBlock.discoverSerialNumber();
        class7MeteringFunctionBlock.build();
        return class7MeteringFunctionBlock.getXMTRSN();
    }

    public Class7MeteringFunctionBlock getClass7MeteringFunctionBlock() throws IOException {
        if (class7MeteringFunctionBlock == null) {
            class7MeteringFunctionBlock = new Class7MeteringFunctionBlock(this);
            class7MeteringFunctionBlock.build();
        }
        return class7MeteringFunctionBlock;
    }

    public Class8FirmwareConfiguration getClass8FirmwareConfiguration() throws IOException {
        if (class8FirmwareConfiguration == null) {
            class8FirmwareConfiguration = new Class8FirmwareConfiguration(this);
            class8FirmwareConfiguration.build();
        }
        return class8FirmwareConfiguration;
    }

    public Class33ModemConfigurationInfo getClass33ModemConfigurationInfo() throws IOException {
        if (class33ModemConfigurationInfo == null) {
            class33ModemConfigurationInfo = new Class33ModemConfigurationInfo(this);
            class33ModemConfigurationInfo.build();
        }
        return class33ModemConfigurationInfo;
    }
    public Class11BillingData getClass11BillingData() throws IOException {
        if (class11BillingData == null) {
            class11BillingData = new Class11BillingData(this);
            class11BillingData.build();
        }
        return class11BillingData;
    }
    public Class12PreviousMonthBillingData getClass12PreviousMonthBillingData() throws IOException {
        if (class12PreviousMonthBillingData == null) {
            class12PreviousMonthBillingData = new Class12PreviousMonthBillingData(this);
            class12PreviousMonthBillingData.build();
        }
        return class12PreviousMonthBillingData;
    }
    public Class13PreviousSeasonBillingData getClass13PreviousSeasonBillingData() throws IOException {
        if (class13PreviousSeasonBillingData == null) {
            class13PreviousSeasonBillingData = new Class13PreviousSeasonBillingData(this);
            class13PreviousSeasonBillingData.build();
        }
        return class13PreviousSeasonBillingData;
    }
    public Class14LoadProfileConfiguration getClass14LoadProfileConfiguration() throws IOException {
        if (class14LoadProfileConfiguration == null) {
            class14LoadProfileConfiguration = new Class14LoadProfileConfiguration(this);
            class14LoadProfileConfiguration.build();
        }
        return class14LoadProfileConfiguration;
    }
    public Class10Status2 getClass10Status2() throws IOException {
        if (class10Status2 == null) {
            class10Status2 = new Class10Status2(this);
            class10Status2.build();
        }
        return class10Status2;
    }

    public Class15EventLogConfiguration getClass15EventLogConfiguration() throws IOException {
        if (class15EventLogConfiguration == null) {
            class15EventLogConfiguration = new Class15EventLogConfiguration(this);
            class15EventLogConfiguration.build();
        }
        return class15EventLogConfiguration;
    }
}
