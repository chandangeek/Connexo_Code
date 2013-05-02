package com.elster.jupiter.metering.plumbing;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.*;

import com.elster.jupiter.orm.*;

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
			table.addColumn("AMIBILLINGREADY", "number", false, NUMBER2ENUMPLUSONE, "amiBillingReady");
			table.addColumn("CHECKBILLING", "char(1)", true, CHAR2BOOLEAN, "checkBilling");
			table.addColumn("CONNECTIONSTATE", "number", false, NUMBER2ENUMPLUSONE, "connectionState");
			table.addQuantityColumns("ESTIMATEDLOAD", false, "estimatedLoad");
			table.addColumn("GROUNDED", "char(1)", true, CHAR2BOOLEAN, "grounded");
			table.addColumn("ISSDP", "char(1)", true, CHAR2BOOLEAN, "isSdp");
			table.addColumn("ISVIRTUAL", "char(1)", true, CHAR2BOOLEAN, "isVirtual");
			table.addColumn("MINIMALUSAGEEXPECTED", "char(1)", true, CHAR2BOOLEAN, "minimalUsageExpected");
			table.addQuantityColumns("NOMINALVOLTAGE",false, "nominalServiceVoltage");
			table.addColumn("OUTAGEREGION", "varchar2(80)", false, NOCONVERSION , "outageRegion");
			table.addColumn("PHASECODE", "varchar2(4)", false,CHAR2ENUM , "phaseCode");
			table.addQuantityColumns("RATEDCURRENT", false, "ratedCurrent");
			table.addQuantityColumns("RATEDPOWER", false , "ratedPower");
			table.addColumn("READCYCLE", "varchar2(80)", false, NOCONVERSION , "readCycle");
			table.addColumn("READROUTE", "varchar2(80)", false, NOCONVERSION , "readRoute");
			table.addColumn("SERVICEDELIVERYREMARK", "varchar2(80)", false, NOCONVERSION , "serviceDeliveryRemark");
			table.addColumn("SERVICEPRIORITY", "varchar2(80)", false, NOCONVERSION , "servicePriority");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_USAGEPOINT", idColumn);
			table.addUniqueConstraint("MTR_U_USAGEPOINT", mRIDColumn);
			table.addForeignKeyConstraint("MTR_FK_USAGEPOINTSERVICECAT",MTR_SERVICECATEGORY.name(),RESTRICT,"serviceCategory", null , serviceKindColumn);
			table.addForeignKeyConstraint("MTR_FK_USAGEPOINTSERVICELOC",MTR_SERVICELOCATION.name(),RESTRICT,"serviceLocation", "usagePoints" , serviceLocationIdColumn);
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
	MTR_METER {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column mRIDColumn = table.addColumn("MRID", "varchar2(80)", false, NOCONVERSION , "mRID");
			Column amrSystemIdColumn = table.addColumn("AMRSYSTEMID", "number", true, NUMBER2INT, "amrSystemId");
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
			table.addForeignKeyConstraint("MTR_FK_METERAMRSYSTEM",MTR_AMRSYSTEM.name(),RESTRICT,"amrSystem", null, amrSystemIdColumn);
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
			table.addForeignKeyConstraint("MTR_FK_METERACTUSAGEPOINT",MTR_USAGEPOINT.name(),RESTRICT,"usagePoint","meterActivations" , "currentMeterActivation" , usagePointIdColumn);
			table.addForeignKeyConstraint("MTR_FK_METERACTMETER",MTR_METER.name(),RESTRICT, "meter" , "meterActivations" , meterIdColumn);
		}
	},
	MTR_CHANNEL {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column meterActivationIdColumn = table.addColumn("METERACTIVATIONID","number",true,NUMBER2LONG,"meterActivationId");
			Column timeSeriesIdColumn = table.addColumn("TIMESERIESID","number",true,NUMBER2LONG,"timeSeriesId");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_CHANNEL", idColumn);
			table.addForeignKeyConstraint("MTR_FK_CHANNELACTIVATION", MTR_METERACTIVATION.name(), RESTRICT, "meterActivation" , "channels" , meterActivationIdColumn);
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
			table.addForeignKeyConstraint("MTR_FK_READINGTYPEINCHANNEL1", MTR_CHANNEL.name(),CASCADE,null,null,channelIdColumn);
			table.addForeignKeyConstraint("MTR_FK_READINGTYPEINCHANNEL2", MTR_READINGTYPE.name(),RESTRICT,null, null,readingTypeMRidColumn);
		}
	};
		
	public void addTo(DataModel component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}