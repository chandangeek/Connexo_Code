package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.ComServer;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;

public enum TableSpecs {

    MDCCOMPORTPOOL {
        @Override
        void addTo(DataModel dataModel) {
            Table<ComPortPool> table = dataModel.addTable(name(), ComPortPool.class);
            table.map(ComPortPoolImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            table.column("NAME").type("varchar2(80)").map(ComPortPoolImpl.Fields.NAME.fieldName()).add();
            table.column("ACTIVE").type("varchar2(1)").notNull().map(ComPortPoolImpl.Fields.ACTIVE.fieldName()).conversion(ColumnConversion.NUMBER2BOOLEAN).add();
            table.column("DESCRIPTION").type("varchar2(80)").map(ComPortPoolImpl.Fields.DESCRIPTION.fieldName()).add();
            table.column("OBSOLETE_DATE").type("DATE").map(ComPortPoolImpl.Fields.OBSOLETEDATE.fieldName()).add();
            table.column("COMPORTTYPE").number().notNull().map(ComPortPoolImpl.Fields.COMPORTTYPE.fieldName()).conversion(ColumnConversion.NUMBER2ENUM).add();
            table.column("TASKEXECUTIONTIMEOUTVALUE").number().conversion(ColumnConversion.NUMBER2INT).map(OutboundComPortPoolImpl.FIELD_TASKEXECUTIONTOMEOUT + ".count").add();
            table.column("TASKEXECUTIONTIMEOUTUNIT").number().conversion(ColumnConversion.NUMBER2INT).map(OutboundComPortPoolImpl.FIELD_TASKEXECUTIONTOMEOUT + ".timeUnitCode").add();
            table.column("DISCOVERYPROTOCOL").number().conversion(ColumnConversion.NUMBER2INT).map(InboundComPortPoolImpl.FIELD_DISCOVEYPROTOCOL).add();
            table.primaryKey("CEM_PK_COMPORTPOOL").on(idColumn).add();
        }
    },
    MDCCOMSERVER {
        @Override
        void addTo(DataModel dataModel) {
            Table<ComServer> table = dataModel.addTable(name(), ComServer.class);
            table.map(ComServerImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(80)").notNull().map(ComServerImpl.FieldNames.NAME.getName()).add();
            table.primaryKey("CEM_PK_COMSERVER").on(idColumn).add();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            table.column("ACTIVE").number().conversion(ColumnConversion.NUMBER2BOOLEAN).map("active").add();
            table.column("SERVERLOGLEVEL").number().conversion(ColumnConversion.NUMBER2ENUM).map("serverLogLevel").add();
            table.column("COMLOGLEVEL").number().conversion(ColumnConversion.NUMBER2ENUM).map("communicationLogLevel").add();

            table.column("CHANGESDELAYVALUE").number().conversion(ColumnConversion.NUMBER2INT).map("changesInterPollDelay.count").add();
            table.column("CHANGESDELAYUNIT").number().conversion(ColumnConversion.NUMBER2INT).map("changesInterPollDelay.timeUnitCode").add();

            table.column("SCHEDULINGDELAYVALUE").number().conversion(ColumnConversion.NUMBER2INT).map("schedulingInterPollDelay.count").add();
            table.column("SCHEDULINGDELAYUNIT").number().conversion(ColumnConversion.NUMBER2INT).map("schedulingInterPollDelay.timeUnitCode").add();

            table.column("QUERYAPIPOSTURI").type("varchar2(512)").map("queryAPIPostUri").add();
            table.column("QUERYAPIUSERNAME").type("varchar2(255)").map("queryAPIUsername").add();
            table.column("QUERYAPIPASSWORD").type("varchar2(255)").map("queryAPIPassword").add();
            table.column("MOD_DATE").type("DATE").conversion(ColumnConversion.DATE2DATE).map("modificationDate").insert("sysdate").update("sysdate").add();
            table.column("OBSOLETE_DATE").type("DATE").conversion(ColumnConversion.DATE2DATE).map("obsoleteDate").add();
            Column onlineComServerId = table.column("ONLINESERVERID").number().conversion(ColumnConversion.NUMBER2INT).add(); // DO NOT MAP
            table.column("QUEUESIZE").number().conversion(ColumnConversion.NUMBER2INT).map("storeTaskQueueSize").add();
            table.column("THREADPRIORITY").number().conversion(ColumnConversion.NUMBER2INT).map("storeTaskThreadPriority").add();
            table.column("NROFTHREADS").number().conversion(ColumnConversion.NUMBER2INT).map("numberOfStoreTaskThreads").add();
            table.column("EVENTREGISTRATIONURI").type("varchar2(512)").map("eventRegistrationUri").add();
            table.column("DEFAULTQUERYAPIPOSTURI").number().conversion(ColumnConversion.NUMBER2BOOLEAN).map("usesDefaultQueryAPIPostUri").add();
            table.column("DEFAULTEVENTREGISTRATIONURI").number().conversion(ColumnConversion.NUMBER2BOOLEAN).map("usesDefaultEventRegistrationUri").add();
            table.foreignKey("FK_REMOTE_ONLINE").on(onlineComServerId).references(MDCCOMSERVER.name()).map("onlineComServer").add();
        }
    },
    MDCCOMPORT {
        @Override
        void addTo(DataModel dataModel) {
            Table<ComPort> table = dataModel.addTable(name(), ComPort.class);
            table.map(ComPortImpl.IMPLEMENTERS);
            // ComPortImpl
            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            table.column("NAME").type("varchar2(80)").map(ComPortImpl.FieldNames.NAME.getName()).add();
            table.column("MOD_DATE").type("DATE").conversion(ColumnConversion.DATE2DATE).map("modificationDate").insert("sysdate").update("sysdate").add();
            Column comServerColumn = table.column("COMSERVERID").number().conversion(ColumnConversion.NUMBER2LONG).add(); // DO NOT MAP
            table.column("ACTIVE").type("varchar2(1)").notNull().map("active").conversion(ColumnConversion.NUMBER2BOOLEAN).add();
            table.column("DESCRIPTION").type("varchar2(80)").map("description").add();
            table.column("OBSOLETE_DATE").type("DATE").conversion(ColumnConversion.DATE2DATE).map("obsoleteDate").add();
            table.column("COMPORTTYPE").number().notNull().conversion(ColumnConversion.NUMBER2ENUM).map("type").add();
            // no mapping required for comPortPoolMembers
            // IPBasedInboundComPortImpl & OutboundComPortImpl
            table.column("PORTNUMBER").number().conversion(ColumnConversion.NUMBER2INT).map("portNumber").add();
            table.column("nrofsimultaneousconns").number().conversion(ColumnConversion.NUMBER2INT).map("numberOfSimultaneousConnections").add();
            // InboundComPortImpl
            // ModemBasedInboundComPortImpl
            table.column("RINGCOUNT").number().conversion(ColumnConversion.NUMBER2INT).map("ringCount").add();
            table.column("maximumDialErrors").number().conversion(ColumnConversion.NUMBER2INT).map("maximumDialErrors").add();
            table.column("CONNECTTIMEOUT").number().conversion(ColumnConversion.NUMBER2INT).map("connectTimeout.count").add();
            table.column("CONNECTTIMEOUTCODE").number().conversion(ColumnConversion.NUMBER2INT).map("connectTimeout.timeUnitCode").add();
            table.column("DELAYAFTERCONNECT").number().conversion(ColumnConversion.NUMBER2INT).map("delayAfterConnect.count").add();
            table.column("DELAYAFTERCONNECTCODE").number().conversion(ColumnConversion.NUMBER2INT).map("delayAfterConnect.timeUnitCode").add();
            table.column("DELAYBEFORESEND").number().conversion(ColumnConversion.NUMBER2INT).map("delayBeforeSend.count").add();
            table.column("DELAYBEFORESENDCODE").number().conversion(ColumnConversion.NUMBER2INT).map("delayBeforeSend.timeUnitCode").add();
            table.column("COMMANDTIMEOUT").number().conversion(ColumnConversion.NUMBER2INT).map("atCommandTimeout.count").add();
            table.column("COMMANDTIMEOUTCODE").number().conversion(ColumnConversion.NUMBER2INT).map("atCommandTimeout.timeUnitCode").add();
            table.column("COMMANDTRY").number().conversion(ColumnConversion.NOCONVERSION).map("atCommandTry").add();
            table.column("ADDRESSSELECTOR").type("varchar2(255)").map("addressSelector").map("addressSelector").add();
            table.column("POSTDIALCOMMANDS").type("varchar2(255)").map("postDialCommands").map("postDialCommands").add();
            table.column("BAUDRATE").number().map("serialPortConfiguration.baudrate").add();
            table.column("NROFDATABITS").number().map("serialPortConfiguration.nrOfDataBits").add();
            table.column("NROFSTOPBITS").number().map("serialPortConfiguration.nrOfStopBits").add();
            table.column("PARITY").type("varchar2(255)").map("serialPortConfiguration.parity").add();
            table.column("FLOWCONTROL").type("varchar2(255)").map("serialPortConfiguration.flowControl").add();
            // ServletBasedInboundComPortImpl
            table.column("HTTPS").type("varchar2(1)").conversion(ColumnConversion.NUMBER2BOOLEAN).map("https").add();
            table.column("KEYSTOREPATH").type("varchar2(255)").map("keyStoreSpecsFilePath").add();
            table.column("KEYSTOREPASSWORD").type("varchar2(255)").map("keyStoreSpecsPassword").add();
            table.column("TRUSTSTOREPATH").type("varchar2(255)").map("trustStoreSpecsFilePath").add();
            table.column("TRUSTSTOREPASSWORD").type("varchar2(255)").map("trustStoreSpecsPassword").add();
            table.column("CONTEXTPATH").type("varchar2(255)").map("contextPath").add();

            table.column("BUFFERSIZE").number().conversion(ColumnConversion.NUMBER2INT).map("bufferSize").add();
            table.column("MODEMINITS").type("varchar2(255)").map("modemInitStrings").add();
            Column inboundComPortPoolId = table.column("COMPORTPOOL").number().conversion(ColumnConversion.NUMBER2LONG).add(); // DO NOT MAP

            table.primaryKey("CEM_PK_COMPORT").on(idColumn).add();
            table.foreignKey("FK_INBOUNDCOMPORTPOOL").on(inboundComPortPoolId).references(MDCCOMPORTPOOL.name()).map("comPortPool").add();
            table.foreignKey("FK_COMPORT_COMSERVER").on(comServerColumn).references(MDCCOMSERVER.name()).
                    map("comServer").reverseMap("comPorts").composition().add();
        }
    },
    MDCCOMPORTPOOLMEMBER {
        @Override
        void addTo(DataModel dataModel) {
            Table<ComPortPoolMember> table = dataModel.addTable(name(), ComPortPoolMember.class);
            table.map(ComPortPoolMemberImpl.class);

            Column comPortPoolIdColumn = table.column("POOL").number().notNull().conversion(NUMBER2LONG).add(); // DO NOT MAP
            Column comPortIdColumn = table.column("COMPORT").number().notNull().conversion(NUMBER2LONG).add(); // DO NOT MAP
            table.primaryKey("CEM_PK_COMPORTINPOOL").on(comPortPoolIdColumn, comPortIdColumn).add();

            table.foreignKey("CEM_FKCOMPORTINPOOLCOMPORT").on(comPortIdColumn).references(MDCCOMPORT.name()).map("comPort").add();
            table.foreignKey("CEM_FKCOMPORTINPOOLCOMPORTPOOL").on(comPortPoolIdColumn).references(MDCCOMPORTPOOL.name()).
                    map("comPortPool").reverseMap("comPortPoolMembers").composition().add();
        }
    };

    abstract void addTo(DataModel component);

}
