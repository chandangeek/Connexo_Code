package com.energyict.mdc.engine;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.engine.model.ComPortImpl;

public enum TableSpecs {

    CEC_COMPORTPOOL {
        @Override
        void describeTable(Table table) {
//            table.map(ComPortPoolImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(80)").map("name").add();
            table.column("ACTIVE").type("INTEGER(1)").notNull().map("active").add();
            table.column("DESCRIPTION").type("varchar2(80)").map("description").add();
            table.column("OBSOLETEFLAG").type("varchar2(1)").map("obsoleteFlag").conversion(ColumnConversion.CHAR2BOOLEAN).add();
            table.column("OBSOLETEDATE").type("DATE").map("obsoleteDate").add();
            table.column("COMPORTTYPE").type("number").notNull().conversion(ColumnConversion.NUMBER2ENUM).add();
            table.column("TASKEXECUTIONTIMEOUT.COUNT").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("TASKEXECUTIONTIMEOUT.TIMEUNITCODE").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("DISCOVERYPROTOCOLPLUGGABLECLASSID").type("number").conversion(ColumnConversion.NUMBER2INT).add();


            table.primaryKey("CEC_PK_COMPORTPOOL").on(idColumn).add();
            table.foreignKey("CEC_FK_COMPORTPOOL_INBOUNDCOMPORT");
//            public List<InboundComPortInfo> inboundComPorts;
//            public int discoveryProtocolPluggableClassId;
//            public List<OutboundComPortInfo> outboundComPorts;

        }
    },
    CEC_COMPORT {
        @Override
        void describeTable(Table table) {
            table.map(ComPortImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column comServerColumn = table.column("COMSERVERID").type("number").conversion(ColumnConversion.NUMBER2LONG).map("comServerId").add();
            table.column("NAME").type("varchar2(80)").map("name").add();
            table.addModTimeColumn("MOD_DATE", "modificationDate");
            table.column("ACTIVE").type("varchar2(1)").notNull().map("active").conversion(ColumnConversion.CHAR2BOOLEAN).add();
            table.column("BOUND").type("varchar2(1)").notNull().map("bound").conversion(ColumnConversion.CHAR2BOOLEAN).add();
            table.column("DESCRIPTION").type("varchar2(80)").map("description").add();
            table.column("OBSOLETEFLAG").type("varchar2(1)").map("obsoleteFlag").conversion(ColumnConversion.CHAR2BOOLEAN).add();
            table.column("OBSOLETEDATE").type("DATE").map("obsoleteDate").add();
            table.column("COMPORTTYPE").type("number").notNull().conversion(ColumnConversion.NUMBER2ENUM).map("comPortType").add();
            table.column("NUMBEROFSIMULTANEOUSCONNECTIONS").type("number").conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("RINGCOUNT").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("MAXIMUMNUMBEROFDIALERRORS").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("CONNECTTIMEOUT.COUNT").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("CONNECTTIMEOUT.TIMEUNITCODE").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("DELAYAFTERCONNECT.COUNT").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("DELAYAFTERCONNECT.TIMEUNITCODE").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("DELAYBEFORESEND.COUNT").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("DELAYBEFORESEND.TIMEUNITCODE").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("ATCOMMANDTIMEOUT.COUNT").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("ATCOMMANDTIMEOUT.TIMEUNITCODE").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("ATCOMMANDTRY").type("number").conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("ADDRESSSELECTOR").type("varchar2(255)").add();
            table.column("POSTDIALCOMMANDS").type("varchar2(255)").add();
            table.column("COMPORTNAME").type("varchar2(80)").add();
            table.column("BAUDRATE").type("number").conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("NROFDATABITS").type("number").conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("NROFSTOPBITS").type("number").conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("FLOWCONTROL").type("varchar2(255)").add();
            table.column("PARITY").type("varchar2(255)").add();
            table.column("PORTNUMBER").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("BUFFERSIZE").type("number").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("HTTPS").type("varchar2(1)").conversion(ColumnConversion.CHAR2BOOLEAN).add();
            table.column("KEYSTOREFILEPATH").type("varchar2(255)").add();
            table.column("TRUSTSTOREFILEPATH").type("varchar2(255)").add();
            table.column("KEYSTOREPASSWORD").type("varchar2(255)").add();
            table.column("TRUSTSTOREPASSWORD").type("varchar2(255)").add();
            table.column("CONTEXTPATH").type("varchar2(255)").add();
            table.column("MODEMINITSTRINGS").type("varchar2(255)").add();

            table.primaryKey("CEC_PK_COMPORT").on(idColumn).add();
            table.foreignKey("FK_COMPORT_COMSERVER").on(comServerColumn).references(CEC_COMSERVER.name()).add();
        }
    },
    CEC_COMSERVER {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.column("ID").type("LONG").map("id").add();
            table.column("NAME").type("varchar2(80)").map("name").add();
            table.primaryKey("CEC_PK_COMSERVER").on(idColumn).add();
        }
    };

    public void addTo(DataModel component) {
   		Table table = component.addTable(name(), null);
   		describeTable(table);
   	}

   	abstract void describeTable(Table table);

}
