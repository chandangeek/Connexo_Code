package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

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
            table.column("COMPORTTYPE").number().notNull().conversion(ColumnConversion.NUMBER2ENUM).add();
            table.column("TASKEXECUTIONTIMEOUT.COUNT").number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column("TASKEXECUTIONTIMEOUT.TIMEUNITCODE").number().conversion(ColumnConversion.NUMBER2INT).add();
            table.column("DISCOVERYPROTOCOLPLUGGABLECLASSID").number().conversion(ColumnConversion.NUMBER2INT).add();

            // LIST of comportpoolmembers!!!!
            table.primaryKey("CEM_PK_COMPORTPOOL").on(idColumn).add();
            table.foreignKey("CEM_FK_COMPORTPOOL_INBOUNDCOMPORT");
        }
    },
    MDCCOMPORT {
        @Override
        void describeTable(Table table) {
            table.map(ComPortImpl.IMPLEMENTERS);
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
            table.column("COMPORTPOOLID").number().conversion(NUMBER2LONG).map("comPortPool");
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
        }
    },
    MDCCOMPORTINPOOL {
   		void describeTable(Table table) {
   			table.map(ComPortPoolMemberImpl.class);
   			Column comPortPoolIdColumn = table.column("COMPORTPOOLID").number().notNull().conversion(NUMBER2LONG).map("comPortPool").add();
   			Column comPortIdColumn = table.column("COMPORTID").number().notNull().conversion(NUMBER2LONG).map("comPort").add();
   			table.primaryKey("CEM_PK_COMPORTINPOOL").on(comPortPoolIdColumn, comPortIdColumn).add();
   			table.unique("CEM_U_COMPORTINPOOL").on(comPortPoolIdColumn , comPortIdColumn).add();
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
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(80)").notNull().map("name").add();
            table.primaryKey("CEM_PK_COMSERVER").on(idColumn).add();
        }
    };

    public void addTo(DataModel component) {
   		Table table = component.addTable(name());
   		describeTable(table);
   	}

   	abstract void describeTable(Table table);

}
