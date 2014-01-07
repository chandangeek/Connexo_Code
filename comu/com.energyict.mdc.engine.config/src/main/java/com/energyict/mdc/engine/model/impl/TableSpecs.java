package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.engine.model.ComPortPoolMember;

import static com.elster.jupiter.orm.ColumnConversion.DATE2DATE;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;

public enum TableSpecs {

    MDCCOMPORTPOOL {
        @Override
        void describeTable(Table table) {
            table.map(ComPortPoolImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(80)").map("name").add();
            table.column("ACTIVE").type("INTEGER(1)").notNull().map("active").add();
            table.column("DESCRIPTION").type("varchar2(80)").map("description").add();
            table.column("OBSOLETEFLAG").type("varchar2(1)").map("obsoleteFlag").conversion(ColumnConversion.CHAR2BOOLEAN).add();
            table.column("OBSOLETEDATE").type("DATE").map("obsoleteDate").add();
            table.column("COMPORTTYPE").number().notNull().map("comPortType").conversion(ColumnConversion.NUMBER2ENUM).add();
            table.column("TASKEXECUTIONTIMEOUTVALUE").number().conversion(ColumnConversion.NUMBER2INT).map("taskExecutionTimeout.count").add();
            table.column("TASKEXECUTIONTIMEOUTUNIT").number().conversion(ColumnConversion.NUMBER2INT).map("taskExecutionTimeout.timeCodeUnit").add();
            table.column("DISCOVERYPROTOCOL").number().conversion(ColumnConversion.NUMBER2INT).map("discoveryProtocolPluggableClassId").add();

            table.primaryKey("CEM_PK_COMPORTPOOL").on(idColumn).add();
        }
    },
    MDCCOMPORT {
        @Override
        void describeTable(Table table) {
            table.map(ComPortImpl.IMPLEMENTERS);
            this.apiClass = ServerComPort.class;
            // ComPortImpl
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(80)").map("name").add();
            table.addModTimeColumn("MOD_DATE", "modificationDate");
            Column comServerColumn = table.column("COMSERVERID").number().conversion(ColumnConversion.NUMBER2LONG).map("comServer").add();
            table.column("ACTIVE").type("varchar2(1)").notNull().map("active").conversion(ColumnConversion.NUMBER2BOOLEAN).add();
            table.column("DESCRIPTION").type("varchar2(80)").map("description").add();
            table.column("OBSOLETEFLAG").type("varchar2(1)").map("obsoleteFlag").conversion(ColumnConversion.NUMBER2BOOLEAN).add();
            table.column("OBSOLETE_DATE").type("DATE").map("obsoleteDate").add();
            table.column("COMPORTTYPE").number().notNull().conversion(ColumnConversion.NUMBER2ENUM).map("type").add();
            // no mapping required for comPortPoolMembers
            // IPBasedInboundComPortImpl & OutboundComPortImpl
            table.column("PORTNUMBER").number().conversion(ColumnConversion.NUMBER2INT).map("portNumber").add();
            table.column("NUMBEROFSIMULTANEOUSCONNECTIONS").number().conversion(ColumnConversion.NUMBER2LONG).map("numberOfSimultaneousConnections").add();
            // InboundComPortImpl
            Column comPortPoolId = table.column("COMPORTPOOLID").number().conversion(NUMBER2LONG).map("comPortPool").add();
            // ModemBasedInboundComPortImpl
            table.column("RINGCOUNT").number().conversion(ColumnConversion.NUMBER2INT).map("ringCount").add();
            table.column("MAXIMUMNUMBEROFDIALERRORS").number().conversion(ColumnConversion.NUMBER2INT).map("maximumDialErrors").add();
            table.column("CONNECTTIMEOUT").number().conversion(ColumnConversion.NUMBER2INT).map("connectionTimeout.count").add();
            table.column("CONNECTTIMEOUTCODE").number().conversion(ColumnConversion.NUMBER2INT).map("connectionTimeout.timeUnitCode").add();
            table.column("DELAYAFTERCONNECT").number().conversion(ColumnConversion.NUMBER2INT).map("delayAfterConnect.count").add();
            table.column("DELAYAFTERCONNECTCODE").number().conversion(ColumnConversion.NUMBER2INT).map("delayAfterConnect.timeUnitCode").add();
            table.column("DELAYBEFORESEND").number().conversion(ColumnConversion.NUMBER2INT).map("delayBeforeSend.count").add();
            table.column("DELAYBEFORESENDCODE").number().conversion(ColumnConversion.NUMBER2INT).map("delayBeforeSend.timeUnitCode").add();
            table.column("ATCOMMANDTIMEOU").number().conversion(ColumnConversion.NUMBER2INT).map("atCommandTimeout.count").add();
            table.column("ATCOMMANDTIMEOUTCODE").number().conversion(ColumnConversion.NUMBER2INT).map("atCommandTimeout.timeUnitCode").add();
            table.column("ATCOMMANDTRY").number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("ADDRESSSELECTOR").type("varchar2(255)").map("addressSelector").add();
            table.column("POSTDIALCOMMANDS").type("varchar2(255)").map("postDialCommands").add();
            table.column("BAUDRATE").number().conversion(ColumnConversion.NUMBER2ENUM).map("serialPortConfiguration.baudrate").add();
            table.column("NROFDATABITS").number().conversion(ColumnConversion.NUMBER2ENUM).map("serialPortConfiguration.nrOfDataBits").add();
            table.column("NROFSTOPBITS").number().conversion(ColumnConversion.NUMBER2ENUM).map("serialPortConfiguration.nrOfStopBits").add();
            table.column("PARITY").number().conversion(NUMBER2ENUM).map("serialPortConfiguration.parity").add();
            table.column("FLOWCONTROL").number().conversion(NUMBER2ENUM).map("serialPortConfiguration.flowControl").add();
            // ServletBasedInboundComPortImpl
            table.column("HTTPS").type("varchar2(1)").conversion(ColumnConversion.NUMBER2BOOLEAN).map("https").add();
            table.column("KEYSTOREFILEPATH").type("varchar2(255)").map("keyStoreSpecsFilePath").add();
            table.column("KEYSTOREPASSWORD").type("varchar2(255)").map("keyStoreSpecsPassword").add();
            table.column("TRUSTSTOREFILEPATH").type("varchar2(255)").map("trustStoreSpecsFilePath").add();
            table.column("TRUSTSTOREPASSWORD").type("varchar2(255)").map("trustStoreSpecsPassword").add();
            table.column("CONTEXTPATH").type("varchar2(255)").map("contextPath").add();

            table.column("BUFFERSIZE").number().conversion(ColumnConversion.NUMBER2INT).map("bufferSize").add();
            table.column("MODEMINITSTRINGS").type("varchar2(255)").map("modemInitStrings").add();

            table.primaryKey("CEM_PK_COMPORT").on(idColumn).add();
            table.foreignKey("FK_COMPORT_COMSERVER").on(comServerColumn).references(MDCCOMSERVER.name()).
                    map("comServer").reverseMap("comPorts").composition().add();
            table.foreignKey("FK_COMPORT_COMPORTPOOL").on(comPortPoolId).references(MDCCOMPORTPOOL.name()).map("comPortPool").reverseMap("comPorts").composition().add();
        }
    },
    MDCCOMPORTINPOOL {
   		void describeTable(Table table) {
   			table.map(ComPortPoolMemberImpl.class);
            this.apiClass=ComPortPoolMember.class;
   			Column comPortPoolIdColumn = table.column("COMPORTPOOLID").number().notNull().conversion(NUMBER2LONG).map("comPortPool").add();
   			Column comPortIdColumn = table.column("COMPORTID").number().notNull().conversion(NUMBER2LONG).map("comPort").add();
   			table.primaryKey("CEM_PK_COMPORTINPOOL").on(comPortPoolIdColumn, comPortIdColumn).add();
   			table.unique("CEM_U_COMPORTINPOOL").on(comPortPoolIdColumn , comPortIdColumn).add();
   			table.foreignKey("CEM_FKCOMPORTINPOOLCOMPORT").on(comPortIdColumn).references(MDCCOMPORT.name()).onDelete(DeleteRule.CASCADE).
   				map("comPort").reverseMap("comPortPoolMembers").composition().add();
            table.foreignKey("CEM_FKCOMPORTINPOOLCOMPORT").on(comPortIdColumn).references(MDCCOMPORT.name()).onDelete(DeleteRule.CASCADE).
                    map("comPort").reverseMap("comPortPoolMembers").composition().add();
            table.foreignKey("CEM_FKCOMPORTINPOOLCOMPORTPOOL").on(comPortPoolIdColumn).references(MDCCOMPORTPOOL.name()).onDelete(DeleteRule.CASCADE).
                    map("comPortPool").reverseMap("comPortPoolMembers").composition().add();
   		}
   	},
    MDCCOMSERVER {
        @Override
        void describeTable(Table table) {
            table.map(ComServerImpl.IMPLEMENTERS);
            this.apiClass = ServerComServer.class;
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(80)").notNull().map("name").add();
            table.primaryKey("CEM_PK_COMSERVER").on(idColumn).add();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            table.column("ACTIVE").number().conversion(ColumnConversion.NUMBER2INT).map("active").add();
            table.column("SERVERLOGLEVEL").number().conversion(ColumnConversion.NUMBER2INT).map("serverLogLevel").add();
            table.column("COMLOGLEVEL").number().conversion(ColumnConversion.NUMBER2INT).map("communicationLogLevel").add();

            table.column("CHANGESDELAYVALUE").number().conversion(ColumnConversion.NUMBER2INT).map("changesInterPollDelay.count").add();
            table.column("CHANGESDELAYUNIT").number().conversion(ColumnConversion.NUMBER2INT).map("changesInterPollDelay.timeUnitCode").add();

            table.column("SCHEDULINGDELAYVALUE").number().conversion(ColumnConversion.NUMBER2INT).map("schedulingInterPollDelay.count").add();
            table.column("SCHEDULINGDELAYUNIT").number().conversion(ColumnConversion.NUMBER2INT).map("schedulingInterPollDelay.timeUnitCode").add();

            table.column("QUERYAPIPOSTURI").type("varchar2(512)").notNull().map("queryAPIPostUri").add();
            table.column("QUERYAPIUSERNAME").type("varchar2(255)").notNull().map("queryAPIUsername").add();
            table.column("QUERYAPIPASSWORD").type("varchar2(255)").notNull().map("queryAPIPassword").add();
            table.addModTimeColumn("MOD_DATE", "modificationDate");
            table.column("OBSOLETE_DATE").type("DATE").conversion(DATE2DATE).map("obsoleteDate").add();
            Column onlineComServerId = table.column("ONLINESERVERID").number().conversion(ColumnConversion.NUMBER2INT).map("onlineComServer").add();
            table.column("QUEUESIZE").number().conversion(ColumnConversion.NUMBER2INT).map("storeTaskQueueSize").add();
            table.column("THREADPRIORITY").number().conversion(ColumnConversion.NUMBER2INT).map("storeTaskThreadPriority").add();
            table.column("NROFTHREADS").number().conversion(ColumnConversion.NUMBER2INT).map("numberOfStoreTaskThreads").add();
            table.column("EVENTREGISTRATIONURI").type("varchar2(512)").notNull().map("eventRegistrationUri").add();
            table.column("DEFAULTQUERYAPIPOSTURI").number().conversion(ColumnConversion.NUMBER2INT).map("usesDefaultQueryAPIPostUri").add();
            table.column("DEFAULTEVENTREGISTRATIONURI").number().conversion(ColumnConversion.NUMBER2INT).map("usesDefaultEventRegistrationUri").add();
            table.foreignKey("FK_REMOTE_ONLINE").on(onlineComServerId).references(MDCCOMSERVER.name()).map("onlineComserver").add();
        }
    };

    private Class apiClass;

    public void addTo(DataModel component) {
   		Table table = component.addTable(name(), apiClass); // TODO fix me
   		describeTable(table);
   	}

   	abstract void describeTable(Table table);

}
