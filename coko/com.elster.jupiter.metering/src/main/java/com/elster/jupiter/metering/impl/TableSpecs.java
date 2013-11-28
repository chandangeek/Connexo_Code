package com.elster.jupiter.metering.impl;

import com.elster.jupiter.orm.AssociationMapping;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
	MTR_SERVICECATEGORY {
		void describeTable(Table table) {
			table.setJournalTableName("MTR_SERVICECATEGORYJRNL");
			Column idColumn = table.addColumn("ID","number", true,NUMBER2ENUMPLUSONE,"kind");
			table.addColumn("ALIASNAME", "varchar2(80)", false, NOCONVERSION , "aliasName");
			table.addColumn("DESCRIPTION", "varchar2(256)", false, NOCONVERSION , "description");
			table.addAuditColumns();			
			table.addPrimaryKeyConstraint("MTR_PK_SERVICECATEGORY", idColumn);
		}
	},
	MTR_SERVICELOCATION {
		void describeTable(Table table) {
			table.setJournalTableName("MTR_SERVICELOCATIONJRNL");
			Column idColumn = table.addAutoIdColumn();
			Column mRIDColumn = table.addColumn("MRID", "varchar2(80)", false, NOCONVERSION , "mRID");
			table.addColumn("NAME", "varchar2(80)", false, NOCONVERSION , "name");
			table.addColumn("ALIASNAME", "varchar2(80)", false, NOCONVERSION , "aliasName");
			table.addColumn("DESCRIPTION", "varchar2(256)", false, NOCONVERSION , "description");
			table.addColumn("DIRECTION", "varchar2(80)", false, NOCONVERSION , "direction");
			table.addColumn("EAEMAIL1", "varchar2(80)", false, NOCONVERSION , "electronicAddress.email1");
			table.addColumn("EAEMAIL2", "varchar2(80)", false, NOCONVERSION , "electronicAddress.email2");
			table.addColumn("EALAN", "varchar2(80)", false, NOCONVERSION , "electronicAddress.lan");
			table.addColumn("EAEMAC", "varchar2(80)", false, NOCONVERSION , "electronicAddress.mac");
			table.addColumn("EAPASSWORD", "varchar2(80)", false, NOCONVERSION , "electronicAddress.password");
			table.addColumn("EARADIO", "varchar2(80)", false, NOCONVERSION , "electronicAddress.radio");
			table.addColumn("EAUSERID", "varchar2(80)", false, NOCONVERSION , "electronicAddress.userID");
			table.addColumn("EAWEB", "varchar2(80)", false, NOCONVERSION , "electronicAddress.web");
			table.addColumn("GEOINFOREFERENCE", "varchar2(80)", false, NOCONVERSION , "geoInfoReference");
			table.addColumn("MASTATUSDATETIME", "number", false, NUMBER2UTCINSTANT , "mainAddress.status.dateTime");
			table.addColumn("MASTATUSREASON", "varchar2(80)", false, NOCONVERSION , "mainAddress.status.reason");
			table.addColumn("MASTATUSREMARK", "varchar2(80)", false, NOCONVERSION , "mainAddress.status.remark");
			table.addColumn("MASTATUSVALUE", "varchar2(80)", false, NOCONVERSION , "mainAddress.status.value");
			table.addColumn("MASTREETADDRESSGENERAL", "varchar2(80)", false, NOCONVERSION , "mainAddress.streetDetail.addressGeneral");
			table.addColumn("MASTREETBUILDINGNAME", "varchar2(80)", false, NOCONVERSION , "mainAddress.streetDetail.buildingName");
			table.addColumn("MASTREETCODE", "varchar2(80)", false, NOCONVERSION , "mainAddress.streetDetail.code");
			table.addColumn("MASTREETNAME", "varchar2(80)", false, NOCONVERSION , "mainAddress.streetDetail.name");
			table.addColumn("MASTREETNUMBER", "varchar2(80)", false, NOCONVERSION , "mainAddress.streetDetail.number");
			table.addColumn("MASTREETPREFIX", "varchar2(80)", false, NOCONVERSION , "mainAddress.streetDetail.prefix");
			table.addColumn("MASTREETSUFFIX", "varchar2(80)", false, NOCONVERSION , "mainAddress.streetDetail.suffix");
			table.addColumn("MASTREETSUITENUMBER", "varchar2(80)", false, NOCONVERSION , "mainAddress.streetDetail.suiteNumber");
			table.addColumn("MASTREETTYPE", "varchar2(80)", false, NOCONVERSION , "mainAddress.streetDetail.type");
			table.addColumn("MASTREETWITHINTOWN", "char(1)", false, CHAR2BOOLEAN , "mainAddress.streetDetail.withinTownLimits");
			table.addColumn("MATOWNCODE", "varchar2(80)", false, NOCONVERSION , "mainAddress.townDetail.code");
			table.addColumn("MATOWNCOUNTRY", "varchar2(80)", false, NOCONVERSION , "mainAddress.townDetail.country");
			table.addColumn("MATOWNNAME", "varchar2(80)", false, NOCONVERSION , "mainAddress.townDetail.name");
			table.addColumn("MATOWNSECTION", "varchar2(80)", false, NOCONVERSION , "mainAddress.townDetail.section");
			table.addColumn("MATOWNSTATE", "varchar2(80)", false, NOCONVERSION , "mainAddress.townDetail.stateOrProvince");
			table.addColumn("PHONE1AREA", "varchar2(80)", false, NOCONVERSION , "phone1.areaCode");
			table.addColumn("PHONE1CITY", "varchar2(80)", false, NOCONVERSION , "phone1.cityCode");
			table.addColumn("PHONE1COUNTRY", "varchar2(80)", false, NOCONVERSION , "phone1.countryCode");
			table.addColumn("PHONE1EXTENSION", "varchar2(80)", false, NOCONVERSION , "phone1.extension");
			table.addColumn("PHONE1LOCALNUMBER", "varchar2(80)", false, NOCONVERSION , "phone1.localNumber");
			table.addColumn("PHONE2AREA", "varchar2(80)", false, NOCONVERSION , "phone2.areaCode");
			table.addColumn("PHONE2CITY", "varchar2(80)", false, NOCONVERSION , "phone2.cityCode");
			table.addColumn("PHONE2COUNTRY", "varchar2(80)", false, NOCONVERSION , "phone2.countryCode");
			table.addColumn("PHONE2EXTENSION", "varchar2(80)", false, NOCONVERSION , "phone2.extension");
			table.addColumn("PHONE2LOCALNUMBER", "varchar2(80)", false, NOCONVERSION , "phone2.localNumber");
			table.addColumn("SASTATUSDATETIME", "number", false, NUMBER2UTCINSTANT, "secondaryAddress.status.dateTime");
			table.addColumn("SASTATUSREASON", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.status.reason");
			table.addColumn("SASTATUSREMARK", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.status.remark");
			table.addColumn("SASTATUSVALUE", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.status.value");
			table.addColumn("SASTREETADDRESSGENERAL", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.streetDetail.addressGeneral");
			table.addColumn("SASTREETBUILDINGNAME", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.streetDetail.buildingName");
			table.addColumn("SASTREETCODE", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.streetDetail.code");
			table.addColumn("SASTREETNAME", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.streetDetail.name");
			table.addColumn("SASTREETNUMBER", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.streetDetail.number");
			table.addColumn("SASTREETPREFIX", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.streetDetail.prefix");
			table.addColumn("SASTREETSUFFIX", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.streetDetail.suffix");
			table.addColumn("SASTREETSUITENUMBER", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.streetDetail.suiteNumber");
			table.addColumn("SASTREETTYPE", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.streetDetail.type");
			table.addColumn("SASTREETWITHINTOWN", "char(1)", false, CHAR2BOOLEAN , "secondaryAddress.streetDetail.withinTownLimits");
			table.addColumn("SATOWNCODE", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.townDetail.code");
			table.addColumn("SATOWNCOUNTRY", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.townDetail.country");
			table.addColumn("SATOWNNAME", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.townDetail.name");
			table.addColumn("SATOWNSECTION", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.townDetail.section");
			table.addColumn("SATOWNSTATE", "varchar2(80)", false, NOCONVERSION , "secondaryAddress.townDetail.stateOrProvince");
			table.addColumn("STATUSDATETIME", "number", false, NUMBER2UTCINSTANT , "status.dateTime");
			table.addColumn("STATUSREASON", "varchar2(80)", false, NOCONVERSION , "status.reason");
			table.addColumn("STATUSREMARK", "varchar2(80)", false, NOCONVERSION , "status.remark");
			table.addColumn("STATUSVALUE", "varchar2(80)", false, NOCONVERSION , "status.value");
			table.addColumn("SERVICELOCATIONTYPE", "varchar2(80)", false, NOCONVERSION , "type");
			table.addColumn("ACCESSMETHOD", "varchar2(80)", false, NOCONVERSION , "accessMethod");
			table.addColumn("NEEDSINSPECTION", "char(1)", true, CHAR2BOOLEAN , "needsInspection");
			table.addColumn("SITEACCESSPROBLEM", "varchar2(80)", false, NOCONVERSION , "siteAccessProblem");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_SERVICELOCATION", idColumn);
			table.addUniqueConstraint("MTR_U_SERVICELOCATION", mRIDColumn);
		}
	},
	MTR_AMRSYSTEM {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column nameColumn = table.addColumn("NAME", "varchar2(80)", true, NOCONVERSION , "name");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_AMRSYSTEM", idColumn);
			table.addUniqueConstraint("MTR_U_AMRSYSTEM", nameColumn);
		}
	},
	MTR_USAGEPOINT {
		void describeTable(Table table) {
			table.setJournalTableName("MTR_USAGEPOINTJRNL");
			Column idColumn = table.addAutoIdColumn();
			Column mRIDColumn = table.addColumn("MRID", "varchar2(80)", false, NOCONVERSION , "mRID");
			Column serviceKindColumn = table.addColumn("SERVICEKIND", "number", true, NUMBER2ENUMPLUSONE, "serviceKind");
			Column serviceLocationIdColumn = table.addColumn("SERVICELOCATIONID", "number", false, NUMBER2LONGNULLZERO, "serviceLocationId");
			table.addColumn("NAME", "varchar2(80)", false, NOCONVERSION , "name");
			table.addColumn("ALIASNAME", "varchar2(80)", false, NOCONVERSION , "aliasName");
			table.addColumn("DESCRIPTION", "varchar2(256)", false, NOCONVERSION , "description");
			table.addColumn("AMIBILLINGREADY", "number", true , NUMBER2ENUM, "amiBillingReady");
			table.addColumn("CHECKBILLING", "char(1)", true, CHAR2BOOLEAN, "checkBilling");
			table.addColumn("CONNECTIONSTATE", "number", true , NUMBER2ENUM, "connectionState");
			table.addQuantityColumns("ESTIMATEDLOAD", false, "estimatedLoad");
			table.addColumn("GROUNDED", "char(1)", true, CHAR2BOOLEAN, "grounded");
			table.addColumn("ISSDP", "char(1)", true, CHAR2BOOLEAN, "isSdp");
			table.addColumn("ISVIRTUAL", "char(1)", true, CHAR2BOOLEAN, "isVirtual");
			table.addColumn("MINIMALUSAGEEXPECTED", "char(1)", true, CHAR2BOOLEAN, "minimalUsageExpected");
			table.addQuantityColumns("NOMINALVOLTAGE",false, "nominalServiceVoltage");
			table.addColumn("OUTAGEREGION", "varchar2(80)", false, NOCONVERSION , "outageRegion");
			table.addColumn("PHASECODE", "varchar2(7)", false,CHAR2ENUM , "phaseCode");
			table.addQuantityColumns("RATEDCURRENT", false, "ratedCurrent");
			table.addQuantityColumns("RATEDPOWER", false , "ratedPower");
			table.addColumn("READCYCLE", "varchar2(80)", false, NOCONVERSION , "readCycle");
			table.addColumn("READROUTE", "varchar2(80)", false, NOCONVERSION , "readRoute");
			table.addColumn("SERVICEDELIVERYREMARK", "varchar2(80)", false, NOCONVERSION , "serviceDeliveryRemark");
			table.addColumn("SERVICEPRIORITY", "varchar2(80)", false, NOCONVERSION , "servicePriority");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_USAGEPOINT", idColumn);
			table.addUniqueConstraint("MTR_U_USAGEPOINT", mRIDColumn);
			table.addForeignKeyConstraint("MTR_FK_USAGEPOINTSERVICECAT",MTR_SERVICECATEGORY.name(),RESTRICT, new AssociationMapping("serviceCategory"), serviceKindColumn);
			table.addForeignKeyConstraint("MTR_FK_USAGEPOINTSERVICELOC",MTR_SERVICELOCATION.name(),RESTRICT, new AssociationMapping("serviceLocation", "usagePoints") , serviceLocationIdColumn);
		}
	},
	MTR_READINGTYPE {
		void describeTable(Table table) {
			Column mRidColumn = table.addColumn("MRID","varchar2(80)",true,NOCONVERSION,"mRID");
			table.addColumn("ALIASNAME", "varchar2(256)", false, NOCONVERSION , "aliasName");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_READINGTYPE", mRidColumn);
		}
	},
	MTR_ENDDEVICE {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			table.addDiscriminatorColumn("ENDDEVICETYPE", "char(1)");
			Column mRIDColumn = table.addColumn("MRID", "varchar2(80)", false, NOCONVERSION , "mRID");
			Column amrSystemIdColumn = table.addColumn("AMRSYSTEMID", "number", true, NUMBER2INT, "amrSystemId");
			Column amrIdColumn = table.addColumn("AMRID", "varchar2(256)" , true , NOCONVERSION, "amrId");
			table.addColumn("NAME", "varchar2(80)", false, NOCONVERSION , "name");
			table.addColumn("ALIASNAME", "varchar2(80)", false, NOCONVERSION , "aliasName");
			table.addColumn("DESCRIPTION", "varchar2(256)", false, NOCONVERSION , "description");
			table.addColumn("SERIALNUMBER", "varchar2(80)", false, NOCONVERSION , "serialNumber");
			table.addColumn("UTCNUMBER", "varchar2(80)", false, NOCONVERSION , "utcNumber");
			table.addColumn("EAEMAIL1", "varchar2(80)", false, NOCONVERSION , "electronicAddress.email1");
			table.addColumn("EAEMAIL2", "varchar2(80)", false, NOCONVERSION , "electronicAddress.email2");
			table.addColumn("EALAN", "varchar2(80)", false, NOCONVERSION , "electronicAddress.lan");
			table.addColumn("EAEMAC", "varchar2(80)", false, NOCONVERSION , "electronicAddress.mac");
			table.addColumn("EAPASSWORD", "varchar2(80)", false, NOCONVERSION , "electronicAddress.password");
			table.addColumn("EARADIO", "varchar2(80)", false, NOCONVERSION , "electronicAddress.radio");
			table.addColumn("EAUSERID", "varchar2(80)", false, NOCONVERSION , "electronicAddress.userID");
			table.addColumn("EAWEB", "varchar2(80)", false, NOCONVERSION , "electronicAddress.web");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_METER", idColumn);
			table.addUniqueConstraint("MTR_U_METER", mRIDColumn);
			table.addUniqueConstraint("MTR_U_METERAMR",amrSystemIdColumn,amrIdColumn);
			table.addForeignKeyConstraint("MTR_FK_METERAMRSYSTEM",MTR_AMRSYSTEM.name(),RESTRICT,new AssociationMapping("amrSystem"), amrSystemIdColumn);
		}
	},	
	MTR_METERACTIVATION {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column usagePointIdColumn = table.addColumn("USAGEPOINTID", "number", false, NUMBER2LONGNULLZERO, "usagePointId");
			Column meterIdColumn = table.addColumn("METERID", "number", false, NUMBER2LONGNULLZERO, "meterId");
			table.addIntervalColumns("interval");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_METERACTIVATION", idColumn);
			table.addForeignKeyConstraint("MTR_FK_METERACTUSAGEPOINT",MTR_USAGEPOINT.name(),RESTRICT, new AssociationMapping("usagePoint", "meterActivations", "interval.start", "currentMeterActivation") , usagePointIdColumn);
			table.addForeignKeyConstraint("MTR_FK_METERACTMETER",MTR_ENDDEVICE.name(),RESTRICT, new AssociationMapping("meter" , "meterActivations", "interval.start", "currentMeterActivation") , meterIdColumn);
		}
	},
	MTR_CHANNEL {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column meterActivationIdColumn = table.addColumn("METERACTIVATIONID","number",true,NUMBER2LONG,"meterActivationId");
			Column timeSeriesIdColumn = table.addColumn("TIMESERIESID","number",true,NUMBER2LONG,"timeSeriesId");
			Column mainReadingTypeMRIDColumn = table.addColumn("MAINREADINGTYPEMRID","varchar2(80)",true,NOCONVERSION,"mainReadingTypeMRID");
			Column cumulativeReadingTypeMRIDColumn = table.addColumn("CUMULATIVEREADINGTYPEMRID","varchar2(80)",false,NOCONVERSION,"cumulativeReadingTypeMRID");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_CHANNEL", idColumn);
			table.addForeignKeyConstraint("MTR_FK_CHANNELACTIVATION", MTR_METERACTIVATION.name(), RESTRICT, new AssociationMapping("meterActivation" , "channels"), meterActivationIdColumn);
			table.addForeignKeyConstraint("MTR_FK_CHANNELMAINTYPE", MTR_READINGTYPE.name(), RESTRICT, new AssociationMapping("mainReadingType"), mainReadingTypeMRIDColumn);
			table.addForeignKeyConstraint("MTR_FK_CHANNELCUMULATIVETYPE", MTR_READINGTYPE.name(), RESTRICT, new AssociationMapping("cumulativeReadingType"), cumulativeReadingTypeMRIDColumn);
			// TODO: How to document dependency on id of IDS TIMESERIES table)
			table.addForeignKeyConstraint("MTR_FK_CHANNELTIMESERIES","IDS","IDS_TIMESERIES",DeleteRule.RESTRICT,"timeSeries",timeSeriesIdColumn);
		}
	},		
	MTR_READINGTYPEINCHANNEL {
		void describeTable(Table table) {
			Column channelIdColumn = table.addColumn("CHANNNELID","number",true,NUMBER2LONG,"channelId");
			Column positionColumn = table.addColumn("POSITION","number",true,NUMBER2INT,"position");
			Column readingTypeMRidColumn = table.addColumn("READINGTYPEMRID","varchar2(80)",true,NOCONVERSION,"readingTypeMRID");
			table.addPrimaryKeyConstraint("MTR_PK_READINGTYPEINCHANNEL", channelIdColumn , positionColumn);
			table.addForeignKeyConstraint("MTR_FK_READINGTYPEINCHANNEL1", MTR_CHANNEL.name(),CASCADE,new AssociationMapping("channel"),channelIdColumn);
			table.addForeignKeyConstraint("MTR_FK_READINGTYPEINCHANNEL2", MTR_READINGTYPE.name(),RESTRICT,new AssociationMapping(null),readingTypeMRidColumn);
		}
	},
	MTR_UPACCOUNTABILITY {
		void describeTable(Table table) {
			table.setJournalTableName("MTR_UPACCOUNTABILITYJRNL");
			Column usagePointIdColumn = table.addColumn("USAGEPOINTID", "number", true , NUMBER2LONG , "usagePointId");
			Column partyIdColumn = table.addColumn("PARTYID", "number", true , NUMBER2LONG, "partyId");			
			Column roleMRIDColumn = table.addColumn("ROLEMRID", "varchar2(80)",  true,  NOCONVERSION, "roleMRID");
			List<Column> intervalColumns = table.addIntervalColumns("interval");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_UPACCOUNTABILITY", usagePointIdColumn , partyIdColumn , roleMRIDColumn , intervalColumns.get(0));
			table.addForeignKeyConstraint("MTR_FK_UPACCOUNTUP", MTR_USAGEPOINT.name(),CASCADE,new AssociationMapping("usagePoint","accountabilities"),usagePointIdColumn);
			table.addForeignKeyConstraint("MTR_FK_UPACCOUNTPARTY", "PRT" , "PRT_PARTY", RESTRICT , "party" , partyIdColumn);
			table.addForeignKeyConstraint("MTR_FK_UPACCOUNTPARTYROLE", "PRT" , "PRT_PARTYROLE", RESTRICT , "role" , roleMRIDColumn);
 		}
	},
    MTR_UP_GROUP {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            table.addColumn("NAME", "varchar2(80)", false, NOCONVERSION , "name");
            Column mRIDColumn = table.addColumn("MRID", "varchar2(80)", false, NOCONVERSION, "mRID");
            table.addColumn("DESCRIPTION", "varchar2(256)", false, NOCONVERSION , "description");
            table.addColumn("ALIASNAME", "varchar2(80)", false, NOCONVERSION , "aliasName");
            table.addDiscriminatorColumn("GROUPTYPE", "char(3)");
            table.addAuditColumns();
            table.addPrimaryKeyConstraint("MTR_PK_ENUM_UP_GROUP", idColumn);
            table.addUniqueConstraint("MTR_U_ENUM_UP_GROUP", mRIDColumn);
        }
    },
    MTR_ENUM_UP_IN_GROUP {
        @Override
        void describeTable(Table table) {
            Column groupColumn = table.addColumn("GROUP_ID", "number", true, NUMBER2LONG, "groupId");
            Column usagePointColumn = table.addColumn("USAGEPOINT_ID", "number", true, NUMBER2LONG, "usagePointId");
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addPrimaryKeyConstraint("MTR_PK_ENUM_UP_GROUP_ENTRY", groupColumn, usagePointColumn, intervalColumns.get(0));
            table.addForeignKeyConstraint("MTR_FK_UPGE_UPG", MTR_UP_GROUP.name(), DeleteRule.CASCADE, new AssociationMapping("usagePointGroup"), groupColumn);
            table.addForeignKeyConstraint("MTR_FK_UPGE_UP", MTR_USAGEPOINT.name(), DeleteRule.RESTRICT, new AssociationMapping("usagePoint"), usagePointColumn);
        }
    },
    MTR_QUERY_UP_GROUP_OP {
        @Override
        void describeTable(Table table) {
            Column groupColumn = table.addColumn("GROUP_ID", "number", true, NUMBER2LONG, "groupId");
            Column positionColumn = table.addColumn("POSITION", "number" , true , NUMBER2INT , "position");
            table.addDiscriminatorColumn("OPERATORTYPE", "char(3)");
            table.addColumn("OPERATOR", "VARCHAR2(80)", false, NOCONVERSION, "operator");
            table.addColumn("FIELDNAME", "VARCHAR2(80)", false, NOCONVERSION, "fieldName");
            table.addColumn("BINDVALUES", "VARCHAR2(256)", false, CHAR2JSON, "values");

            table.addPrimaryKeyConstraint("MTR_PK_QUPGOP", groupColumn, positionColumn);
            table.addForeignKeyConstraint("MTR_FK_QUPG_QUPGOP", MTR_UP_GROUP.name(), DeleteRule.CASCADE, new AssociationMapping("usagePointGroup", "operations", "position"), groupColumn);

        }
    },
    MTR_READINGQUALITY {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            Column channelColumn = table.addColumn("CHANNELID", "number", true, NUMBER2LONG, "channelId");
            Column timestampColumn = table.addColumn("READINGTIMESTAMP", "number", true, NUMBER2UTCINSTANT, "readingTimestamp");
            Column typeColumn = table.addColumn("TYPE", "varchar(64)", true, NOCONVERSION, "typeCode");
            table.addAuditColumns();
            table.addColumn("COMMENTS", "varchar(4000)", false, ColumnConversion.NOCONVERSION, "comment");
            table.addPrimaryKeyConstraint("MTR_PK_READINGQUALITY", idColumn);
            table.addForeignKeyConstraint("MTR_FK_RQ_CHANNEL", MTR_CHANNEL.name(), DeleteRule.CASCADE, new AssociationMapping("channel"), channelColumn);
            table.addUniqueConstraint("MTR_U_READINGQUALITY", channelColumn, timestampColumn, typeColumn);
        }
    };
	
	public void addTo(DataModel component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}