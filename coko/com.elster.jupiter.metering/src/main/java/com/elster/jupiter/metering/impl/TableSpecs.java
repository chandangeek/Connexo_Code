package com.elster.jupiter.metering.impl;

import com.elster.jupiter.orm.AssociationMapping;
import com.elster.jupiter.orm.Column;
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
			Column idColumn = table.column("ID").number().notNull().conversion(NUMBER2ENUMPLUSONE).map("kind").add();
			table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.addAuditColumns();			
			table.primaryKey("MTR_PK_SERVICECATEGORY").on(idColumn).add();
		}
	},
	MTR_SERVICELOCATION {
		void describeTable(Table table) {
			table.setJournalTableName("MTR_SERVICELOCATIONJRNL");
			Column idColumn = table.addAutoIdColumn();
			Column mRIDColumn = table.column("MRID").type("varchar2(80)").map("mRID").add();
			table.column("NAME").type("varchar2(80)").map("name").add();
			table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.column("DIRECTION").type("varchar2(80)").map("direction").add();
			table.column("EAEMAIL1").type("varchar2(80)").map("electronicAddress.email1").add();
			table.column("EAEMAIL2").type("varchar2(80)").map("electronicAddress.email2").add();
			table.column("EALAN").type("varchar2(80)").map("electronicAddress.lan").add();
			table.column("EAEMAC").type("varchar2(80)").map("electronicAddress.mac").add();
			table.column("EAPASSWORD").type("varchar2(80)").map("electronicAddress.password").add();
			table.column("EARADIO").type("varchar2(80)").map("electronicAddress.radio").add();
			table.column("EAUSERID").type("varchar2(80)").map("electronicAddress.userID").add();
			table.column("EAWEB").type("varchar2(80)").map("electronicAddress.web").add();
			table.column("GEOINFOREFERENCE").type("varchar2(80)").map("geoInfoReference").add();
			table.column("MASTATUSDATETIME").type("number").conversion(NUMBER2UTCINSTANT).map("mainAddress.status.dateTime").add();
			table.column("MASTATUSREASON").type("varchar2(80)").map("mainAddress.status.reason").add();
			table.column("MASTATUSREMARK").type("varchar2(80)").map("mainAddress.status.remark").add();
			table.column("MASTATUSVALUE").type("varchar2(80)").map("mainAddress.status.value").add();
			table.column("MASTREETADDRESSGENERAL").type("varchar2(80)").map("mainAddress.streetDetail.addressGeneral").add();
			table.column("MASTREETBUILDINGNAME").type("varchar2(80)").map("mainAddress.streetDetail.buildingName").add();
			table.column("MASTREETCODE").type("varchar2(80)").map("mainAddress.streetDetail.code").add();
			table.column("MASTREETNAME").type("varchar2(80)").map("mainAddress.streetDetail.name").add();
			table.column("MASTREETNUMBER").type("varchar2(80)").map("mainAddress.streetDetail.number").add();
			table.column("MASTREETPREFIX").type("varchar2(80)").map("mainAddress.streetDetail.prefix").add();
			table.column("MASTREETSUFFIX").type("varchar2(80)").map("mainAddress.streetDetail.suffix").add();
			table.column("MASTREETSUITENUMBER").type("varchar2(80)").map("mainAddress.streetDetail.suiteNumber").add();
			table.column("MASTREETTYPE").type("varchar2(80)").map("mainAddress.streetDetail.type").add();
			table.column("MASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("mainAddress.streetDetail.withinTownLimits").add();
			table.column("MATOWNCODE").type("varchar2(80)").map("mainAddress.townDetail.code").add();
			table.column("MATOWNCOUNTRY").type("varchar2(80)").map("mainAddress.townDetail.country").add();
			table.column("MATOWNNAME").type("varchar2(80)").map("mainAddress.townDetail.name").add();
			table.column("MATOWNSECTION").type("varchar2(80)").map("mainAddress.townDetail.section").add();
			table.column("MATOWNSTATE").type("varchar2(80)").map("mainAddress.townDetail.stateOrProvince").add();
			table.column("PHONE1AREA").type("varchar2(80)").map("phone1.areaCode").add();
			table.column("PHONE1CITY").type("varchar2(80)").map("phone1.cityCode").add();
			table.column("PHONE1COUNTRY").type("varchar2(80)").map("phone1.countryCode").add();
			table.column("PHONE1EXTENSION").type("varchar2(80)").map("phone1.extension").add();
			table.column("PHONE1LOCALNUMBER").type("varchar2(80)").map("phone1.localNumber").add();
			table.column("PHONE2AREA").type("varchar2(80)").map("phone2.areaCode").add();
			table.column("PHONE2CITY").type("varchar2(80)").map("phone2.cityCode").add();
			table.column("PHONE2COUNTRY").type("varchar2(80)").map("phone2.countryCode").add();
			table.column("PHONE2EXTENSION").type("varchar2(80)").map("phone2.extension").add();
			table.column("PHONE2LOCALNUMBER").type("varchar2(80)").map("phone2.localNumber").add();
			table.column("SASTATUSDATETIME").type("number").conversion(NUMBER2UTCINSTANT).map("secondaryAddress.status.dateTime").add();
			table.column("SASTATUSREASON").type("varchar2(80)").map("secondaryAddress.status.reason").add();
			table.column("SASTATUSREMARK").type("varchar2(80)").map("secondaryAddress.status.remark").add();
			table.column("SASTATUSVALUE").type("varchar2(80)").map("secondaryAddress.status.value").add();
			table.column("SASTREETADDRESSGENERAL").type("varchar2(80)").map("secondaryAddress.streetDetail.addressGeneral").add();
			table.column("SASTREETBUILDINGNAME").type("varchar2(80)").map("secondaryAddress.streetDetail.buildingName").add();
			table.column("SASTREETCODE").type("varchar2(80)").map("secondaryAddress.streetDetail.code").add();
			table.column("SASTREETNAME").type("varchar2(80)").map("secondaryAddress.streetDetail.name").add();
			table.column("SASTREETNUMBER").type("varchar2(80)").map("secondaryAddress.streetDetail.number").add();
			table.column("SASTREETPREFIX").type("varchar2(80)").map("secondaryAddress.streetDetail.prefix").add();
			table.column("SASTREETSUFFIX").type("varchar2(80)").map("secondaryAddress.streetDetail.suffix").add();
			table.column("SASTREETSUITENUMBER").type("varchar2(80)").map("secondaryAddress.streetDetail.suiteNumber").add();
			table.column("SASTREETTYPE").type("varchar2(80)").map("secondaryAddress.streetDetail.type").add();
			table.column("SASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("secondaryAddress.streetDetail.withinTownLimits").add();
			table.column("SATOWNCODE").type("varchar2(80)").map("secondaryAddress.townDetail.code").add();
			table.column("SATOWNCOUNTRY").type("varchar2(80)").map("secondaryAddress.townDetail.country").add();
			table.column("SATOWNNAME").type("varchar2(80)").map("secondaryAddress.townDetail.name").add();
			table.column("SATOWNSECTION").type("varchar2(80)").map("secondaryAddress.townDetail.section").add();
			table.column("SATOWNSTATE").type("varchar2(80)").map("secondaryAddress.townDetail.stateOrProvince").add();
			table.column("STATUSDATETIME").type("number").conversion(NUMBER2UTCINSTANT).map("status.dateTime").add();
			table.column("STATUSREASON").type("varchar2(80)").map("status.reason").add();
			table.column("STATUSREMARK").type("varchar2(80)").map("status.remark").add();
			table.column("STATUSVALUE").type("varchar2(80)").map("status.value").add();
			table.column("SERVICELOCATIONTYPE").type("varchar2(80)").map("type").add();
			table.column("ACCESSMETHOD").type("varchar2(80)").map("accessMethod").add();
			table.column("NEEDSINSPECTION").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("needsInspection").add();
			table.column("SITEACCESSPROBLEM").type("varchar2(80)").map("siteAccessProblem").add();
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_SERVICELOCATION", idColumn);
			table.addUniqueConstraint("MTR_U_SERVICELOCATION", mRIDColumn);
		}
	},
	MTR_AMRSYSTEM {
		void describeTable(Table table) {
			Column idColumn = table.column("ID").number().notNull().conversion(NUMBER2INT).map("id").add();
			Column nameColumn = table.column("NAME").type("varchar2(80)").notNull().map("name").add();
			table.addAuditColumns();
			table.primaryKey("MTR_PK_AMRSYSTEM").on(idColumn).add();
			table.unique("MTR_U_AMRSYSTEM").on(nameColumn).add();
		}
	},
	MTR_USAGEPOINT {
		void describeTable(Table table) {
			table.setJournalTableName("MTR_USAGEPOINTJRNL");
			Column idColumn = table.addAutoIdColumn();
			Column mRIDColumn = table.column("MRID").type("varchar2(80)").map("mRID").add();
			Column serviceKindColumn = table.column("SERVICEKIND").number().notNull().conversion(NUMBER2ENUMPLUSONE).map("serviceKind").add();
			Column serviceLocationIdColumn = table.column("SERVICELOCATIONID").number().conversion(NUMBER2LONGNULLZERO).map("serviceLocationId").add();
			table.column("NAME").type("varchar2(80)").map("name").add();
			table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.column("AMIBILLINGREADY").number().notNull().conversion(NUMBER2ENUM).map("amiBillingReady").add();
			table.column("CHECKBILLING").bool().map("checkBilling").add();
			table.column("CONNECTIONSTATE").number().notNull().conversion(NUMBER2ENUM).map("connectionState").add();
			table.addQuantityColumns("ESTIMATEDLOAD", false, "estimatedLoad");
			table.column("GROUNDED").bool().map("grounded").add();
			table.column("ISSDP").bool().map("isSdp").add();
			table.column("ISVIRTUAL").bool().map("isVirtual").add();
			table.column("MINIMALUSAGEEXPECTED").bool().map("minimalUsageExpected").add();
			table.addQuantityColumns("NOMINALVOLTAGE",false, "nominalServiceVoltage");
			table.column("OUTAGEREGION").type("varchar2(80)").map("outageRegion").add();
			table.column("PHASECODE").type("varchar2(7)").conversion(CHAR2ENUM).map("phaseCode").add();
			table.addQuantityColumns("RATEDCURRENT", false, "ratedCurrent");
			table.addQuantityColumns("RATEDPOWER", false , "ratedPower");
			table.column("READCYCLE").type("varchar2(80)").map("readCycle").add();
			table.column("READROUTE").type("varchar2(80)").map("readRoute").add();
			table.column("SERVICEDELIVERYREMARK").type("varchar2(80)").map("serviceDeliveryRemark").add();
			table.column("SERVICEPRIORITY").type("varchar2(80)").map("servicePriority").add();
			table.addAuditColumns();
			table.primaryKey("MTR_PK_USAGEPOINT").on(idColumn).add();
			table.unique("MTR_U_USAGEPOINT").on(mRIDColumn).add();
			table.foreignKey("MTR_FK_USAGEPOINTSERVICECAT").on(serviceKindColumn).references(MTR_SERVICECATEGORY.name()).onDelete(RESTRICT).map("serviceCategory").add();
			table.foreignKey("MTR_FK_USAGEPOINTSERVICELOC").on(serviceLocationIdColumn).references(MTR_SERVICELOCATION.name()).onDelete(RESTRICT).map("serviceLocation").reverseMap("usagePoints").add();
		}
	},
	MTR_READINGTYPE {
		void describeTable(Table table) {
			Column mRidColumn = table.column("MRID").type("varchar2(80)").notNull().map("mRID").add();
			table.column("ALIASNAME").type("varchar2(256)").map("aliasName").add();
            table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_READINGTYPE", mRidColumn);
		}
	},
	MTR_ENDDEVICE {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			table.addDiscriminatorColumn("ENDDEVICETYPE", "char(1)");
			Column mRIDColumn = table.column("MRID").type("varchar2(80)").map("mRID").add();
			Column amrSystemIdColumn = table.column("AMRSYSTEMID").type("number").notNull().conversion(NUMBER2INT).map("amrSystemId").add();
			Column amrIdColumn = table.column("AMRID").type("varchar2(256)").notNull().map("amrId").add();
			table.column("NAME").type("varchar2(80)").map("name").add();
			table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.column("SERIALNUMBER").type("varchar2(80)").map("serialNumber").add();
			table.column("UTCNUMBER").type("varchar2(80)").map("utcNumber").add();
			table.column("EAEMAIL1").type("varchar2(80)").map("electronicAddress.email1").add();
			table.column("EAEMAIL2").type("varchar2(80)").map("electronicAddress.email2").add();
			table.column("EALAN").type("varchar2(80)").map("electronicAddress.lan").add();
			table.column("EAEMAC").type("varchar2(80)").map("electronicAddress.mac").add();
			table.column("EAPASSWORD").type("varchar2(80)").map("electronicAddress.password").add();
			table.column("EARADIO").type("varchar2(80)").map("electronicAddress.radio").add();
			table.column("EAUSERID").type("varchar2(80)").map("electronicAddress.userID").add();
			table.column("EAWEB").type("varchar2(80)").map("electronicAddress.web").add();
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
			Column usagePointIdColumn = table.column("USAGEPOINTID").type("number").conversion(NUMBER2LONGNULLZERO).map("usagePointId").add();
			Column meterIdColumn = table.column("METERID").type("number").conversion(NUMBER2LONGNULLZERO).map("meterId").add();
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
			Column meterActivationIdColumn = table.column("METERACTIVATIONID").type("number").notNull().conversion(NUMBER2LONG).map("meterActivationId").add();
			Column timeSeriesIdColumn = table.column("TIMESERIESID").type("number").notNull().conversion(NUMBER2LONG).map("timeSeriesId").add();
			Column mainReadingTypeMRIDColumn = table.addColumn("MAINREADINGTYPEMRID","varchar2(80)",true,NOCONVERSION,"mainReadingTypeMRID");
			Column cumulativeReadingTypeMRIDColumn = table.addColumn("CUMULATIVEREADINGTYPEMRID","varchar2(80)",false,NOCONVERSION,"cumulativeReadingTypeMRID");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_CHANNEL", idColumn);
			table.addForeignKeyConstraint("MTR_FK_CHANNELACTIVATION", MTR_METERACTIVATION.name(), RESTRICT, new AssociationMapping("meterActivation" , "channels"), meterActivationIdColumn);
			table.addForeignKeyConstraint("MTR_FK_CHANNELMAINTYPE", MTR_READINGTYPE.name(), RESTRICT, new AssociationMapping("mainReadingType"), mainReadingTypeMRIDColumn);
			table.addForeignKeyConstraint("MTR_FK_CHANNELCUMULATIVETYPE", MTR_READINGTYPE.name(), RESTRICT, new AssociationMapping("cumulativeReadingType"), cumulativeReadingTypeMRIDColumn);
			table.foreignKey("MTR_FK_CHANNELTIMESERIES").on(timeSeriesIdColumn).references("IDS","IDS_TIMESERIES").onDelete(RESTRICT).map("timeSeries").add();
		}
	},		
	MTR_READINGTYPEINCHANNEL {
		void describeTable(Table table) {
			Column channelIdColumn = table.addColumn("CHANNNELID", "number", true, NUMBER2LONG," channelId");
			Column positionColumn = table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();
			Column readingTypeMRidColumn = table.column("READINGTYPEMRID").type("varchar2(80)").notNull().map("readingTypeMRID").add();
			table.addPrimaryKeyConstraint("MTR_PK_READINGTYPEINCHANNEL", channelIdColumn , positionColumn);
			table.addForeignKeyConstraint("MTR_FK_READINGTYPEINCHANNEL1", MTR_CHANNEL.name(),CASCADE,new AssociationMapping("channel"),channelIdColumn);
			table.addForeignKeyConstraint("MTR_FK_READINGTYPEINCHANNEL2", MTR_READINGTYPE.name(),RESTRICT,new AssociationMapping(null),readingTypeMRidColumn);
		}
	},
	MTR_UPACCOUNTABILITY {
		void describeTable(Table table) {
			table.setJournalTableName("MTR_UPACCOUNTABILITYJRNL");
			Column usagePointIdColumn = table.column("USAGEPOINTID").type("number").notNull().conversion(NUMBER2LONG).map("usagePointId").add();
			Column partyIdColumn = table.column("PARTYID").type("number").notNull().conversion(NUMBER2LONG).map("partyId").add();
			Column roleMRIDColumn = table.column("ROLEMRID").type("varchar2(80)").notNull().map("roleMRID").add();
			List<Column> intervalColumns = table.addIntervalColumns("interval");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MTR_PK_UPACCOUNTABILITY", usagePointIdColumn , partyIdColumn , roleMRIDColumn , intervalColumns.get(0));
			table.addForeignKeyConstraint("MTR_FK_UPACCOUNTUP", MTR_USAGEPOINT.name(),CASCADE,new AssociationMapping("usagePoint","accountabilities"),usagePointIdColumn);
			table.foreignKey("MTR_FK_UPACCOUNTPARTY").on(partyIdColumn).references("PRT", "PRT_PARTY").onDelete(RESTRICT).map("party").add();
			table.foreignKey("MTR_FK_UPACCOUNTPARTYROLE").on(roleMRIDColumn).references("PRT", "PRT_PARTYROLE").onDelete(RESTRICT).map("role").add();
 		}
	},
    MTR_UP_GROUP {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(80)").map("name").add();
            Column mRIDColumn = table.column("MRID").type("varchar2(80)").map("mRID").add();
            table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
            table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
            table.addDiscriminatorColumn("GROUPTYPE", "char(3)");
            table.addAuditColumns();
            table.addPrimaryKeyConstraint("MTR_PK_ENUM_UP_GROUP", idColumn);
            table.addUniqueConstraint("MTR_U_ENUM_UP_GROUP", mRIDColumn);
        }
    },
    MTR_ENUM_UP_IN_GROUP {
        @Override
        void describeTable(Table table) {
            Column groupColumn = table.column("GROUP_ID").type("number").notNull().conversion(NUMBER2LONG).map("groupId").add();
            Column usagePointColumn = table.column("USAGEPOINT_ID").type("number").notNull().conversion(NUMBER2LONG).map("usagePointId").add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addPrimaryKeyConstraint("MTR_PK_ENUM_UP_GROUP_ENTRY", groupColumn, usagePointColumn, intervalColumns.get(0));
            table.addForeignKeyConstraint("MTR_FK_UPGE_UPG", MTR_UP_GROUP.name(), DeleteRule.CASCADE, new AssociationMapping("usagePointGroup"), groupColumn);
            table.addForeignKeyConstraint("MTR_FK_UPGE_UP", MTR_USAGEPOINT.name(), DeleteRule.RESTRICT, new AssociationMapping("usagePoint"), usagePointColumn);
        }
    },
    MTR_QUERY_UP_GROUP_OP {
        @Override
        void describeTable(Table table) {
            Column groupColumn = table.column("GROUP_ID").type("number").notNull().conversion(NUMBER2LONG).map("groupId").add();
            Column positionColumn = table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();
            table.addDiscriminatorColumn("OPERATORTYPE", "char(3)");
            table.column("OPERATOR").type("VARCHAR2(80)").map("operator").add();
            table.column("FIELDNAME").type("VARCHAR2(80)").map("fieldName").add();
            table.column("BINDVALUES").type("VARCHAR2(256)").conversion(CHAR2JSON).map("values").add();

            table.addPrimaryKeyConstraint("MTR_PK_QUPGOP", groupColumn, positionColumn);
            table.addForeignKeyConstraint("MTR_FK_QUPG_QUPGOP", MTR_UP_GROUP.name(), DeleteRule.CASCADE, new AssociationMapping("usagePointGroup", "operations", "position"), groupColumn);

        }
    },
    MTR_READINGQUALITY {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            Column channelColumn = table.column("CHANNELID").type("number").notNull().conversion(NUMBER2LONG).map("channelId").add();
            Column timestampColumn = table.column("READINGTIMESTAMP").type("number").notNull().conversion(NUMBER2UTCINSTANT).map("readingTimestamp").add();
            Column typeColumn = table.column("TYPE").type("varchar(64)").notNull().map("typeCode").add();
            table.addAuditColumns();
            table.column("COMMENTS").type("varchar(4000)").map("comment").add();
            table.addPrimaryKeyConstraint("MTR_PK_READINGQUALITY", idColumn);
            table.addForeignKeyConstraint("MTR_FK_RQ_CHANNEL", MTR_CHANNEL.name(), DeleteRule.CASCADE, new AssociationMapping("channel"), channelColumn);
            table.addUniqueConstraint("MTR_U_READINGQUALITY", channelColumn, timestampColumn, typeColumn);
        }
    },
    MTR_ENDDEVICEEVENTTYPE {
        @Override
        void describeTable(Table table) {
            Column mRidColumn = table.column("MRID").type("varchar2(80)").notNull().map("mRID").add();
            table.column("ALIASNAME").type("varchar2(256)").map("aliasName").add();
            table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
            table.addAuditColumns();
            table.addPrimaryKeyConstraint("MTR_PK_ENDDEVICEEVENTTYPE", mRidColumn);
        }
    }, MTR_ENDDEVICEEVENTRECORD {
        @Override
        void describeTable(Table table) {
            table.map(EndDeviceEventRecordImpl.class);
            table.column("NAME").type("varchar2(80)").map("name").add();
            table.column("MRID").type("varchar2(80)").map("mRID").add();
            table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
            table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
            table.column("REASON").type("varchar2(256)").map("reason").add();
            table.column("SEVERITY").type("varchar2(80)").map("severity").add();
            Column eventTypeColumn = table.column("EVENTTYPE").type("varchar2(80)").notNull().map("eventTypeCode").add();
            table.column("ISSUERID").type("varchar2(80)").map("issuerID").add();
            table.column("ISSUERTRACKINGID").type("varchar2(80)").map("issuerTrackingID").add();
            table.column("STATUSDATETIME").type("number").conversion(NUMBER2UTCINSTANT).map("status.dateTime").add();
            table.column("STATUSREASON").type("varchar2(80)").map("status.reason").add();
            table.column("STATUSREMARK").type("varchar2(80)").map("status.remark").add();
            table.column("STATUSVALUE").type("varchar2(80)").map("status.value").add();
            table.column("PROCESSINGFLAGS").type("number").map("processingFlags").conversion(NUMBER2LONG).add();
            Column endDeviceColumn = table.column("ENDDEVICEID").type("number").notNull().map("endDeviceId").conversion(NUMBER2LONG).add();
            table.column("LOGBOOKID").type("number").map("logBookId").conversion(NUMBER2INT).add();
            table.column("LOGBOOKPOSITION").type("number").map("logBookPosition").conversion(NUMBER2INT).add();
            Column createdDateTimeColumn = table.column("CREATEDDATETIME").type("number").notNull().conversion(NUMBER2UTCINSTANT).map("createdDateTime").add();
            table.addAuditColumns();
            table.addPrimaryKeyConstraint("MTR_PK_ENDDEVICEEVENTRECORD", endDeviceColumn, eventTypeColumn, createdDateTimeColumn);
            table.addForeignKeyConstraint("MTR_FK_EVENT_ENDDEVICE", MTR_ENDDEVICE.name(), DeleteRule.CASCADE, new AssociationMapping("endDevice"), endDeviceColumn);
        }
    },
    MTR_ENDDEVICEEVENTDETAIL {
        @Override
        void describeTable(Table table) {
            table.map(EndDeviceEventRecordImpl.EndDeviceEventDetailRecord.class);
            Column eventTypeColumn = table.column("EVENTTYPE").type("varchar2(80)").notNull().map("eventTypeCode").add();
            Column endDeviceColumn = table.column("ENDDEVICEID").type("number").notNull().map("endDeviceId").conversion(NUMBER2LONG).add();
            Column createdDateTimeColumn = table.column("CREATEDDATETIME").type("number").notNull().conversion(NUMBER2UTCINSTANT).map("createdDateTime").add();
            Column keyColumn = table.column("KEY").type("varchar2(80)").notNull().map("key").add();
            table.column("VALUE").type("varchar2(256)").notNull().map("value").add();
            table.primaryKey("MTR_PK_ENDDEVICEEVENTDETAIL").on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn, keyColumn).add();
            table.foreignKey("MTR_FK_ENDDEVICEEVENT_DETAIL").on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn).references(MTR_ENDDEVICEEVENTRECORD.name())
                    .onDelete(DeleteRule.CASCADE).map("eventRecord").reverseMap("detailRecords").composition().add();
        }
    };
	
	public void addTo(DataModel component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}