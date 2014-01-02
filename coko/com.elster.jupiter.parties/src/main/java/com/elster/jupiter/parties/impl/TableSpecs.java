package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;

public enum TableSpecs {
	PRT_PARTY (Party.class) {
		void describeTable(Table table) {
			table.map(PartyImpl.IMPLEMENTERS);
			table.setJournalTableName("PRT_PARTYJRNL");
			Column idColumn = table.addAutoIdColumn();
			table.addDiscriminatorColumn("PARTYTYPE", "char(1)");
			Column mRIDColumn = table.column("MRID").type("varchar2(80)").map("mRID").add();
			table.column("NAME").type("varchar2(80)").map("name").add();
			table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.column("EAEMAIL1").type("varchar2(80)").map("electronicAddress.email1").add();
			table.column("EAEMAIL2").type("varchar2(80)").map("electronicAddress.email2").add();
			table.column("EALAN").type("varchar2(80)").map("electronicAddress.lan").add();
			table.column("EAEMAC").type("varchar2(80)").map("electronicAddress.mac").add();
			table.column("EAPASSWORD").type("varchar2(80)").map("electronicAddress.password").add();
			table.column("EARADIO").type("varchar2(80)").map("electronicAddress.radio").add();
			table.column("EAUSERID").type("varchar2(80)").map("electronicAddress.userID").add();
			table.column("EAWEB").type("varchar2(80)").map("electronicAddress.web").add();
			table.column("PAPOBOX").type("varchar2(80)").map("postalAddress.poBox").add();
			table.column("PAPOSTALCODE").type("varchar2(80)").map("postalAddress.postalCode").add();			
			table.column("PASTREETADDRESSGENERAL").type("varchar2(80)").map("postalAddress.streetDetail.addressGeneral").add();
			table.column("PASTREETBUILDINGNAME").type("varchar2(80)").map("postalAddress.streetDetail.buildingName").add();
			table.column("PASTREETCODE").type("varchar2(80)").map("postalAddress.streetDetail.code").add();
			table.column("PASTREETNAME").type("varchar2(80)").map("postalAddress.streetDetail.name").add();
			table.column("PASTREETNUMBER").type("varchar2(80)").map("postalAddress.streetDetail.number").add();
			table.column("PASTREETPREFIX").type("varchar2(80)").map("postalAddress.streetDetail.prefix").add();
			table.column("PASTREETSUFFIX").type("varchar2(80)").map("postalAddress.streetDetail.suffix").add();
			table.column("PASTREETSUITENUMBER").type("varchar2(80)").map("postalAddress.streetDetail.suiteNumber").add();
			table.column("PASTREETTYPE").type("varchar2(80)").map("postalAddress.streetDetail.type").add();
			table.column("PASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("postalAddress.streetDetail.withinTownLimits").add();
			table.column("PATOWNCODE").type("varchar2(80)").map("postalAddress.townDetail.code").add();
			table.column("PATOWNCOUNTRY").type("varchar2(80)").map("postalAddress.townDetail.country").add();
			table.column("PATOWNNAME").type("varchar2(80)").map("postalAddress.townDetail.name").add();
			table.column("PATOWNSECTION").type("varchar2(80)").map("postalAddress.townDetail.section").add();
			table.column("PATOWNSTATE").type("varchar2(80)").map("postalAddress.townDetail.stateOrProvince").add();			
			table.column("SASTATUSDATETIME").type("number").conversion(NUMBER2UTCINSTANT).map("streetAddress.status.dateTime").add();
			table.column("SASTATUSREASON").type("varchar2(80)").map("streetAddress.status.reason").add();
			table.column("SASTATUSREMARK").type("varchar2(80)").map("streetAddress.status.remark").add();
			table.column("SASTATUSVALUE").type("varchar2(80)").map("streetAddress.status.value").add();	
			table.column("SASTREETADDRESSGENERAL").type("varchar2(80)").map("streetAddress.streetDetail.addressGeneral").add();
			table.column("SASTREETBUILDINGNAME").type("varchar2(80)").map("streetAddress.streetDetail.buildingName").add();
			table.column("SASTREETCODE").type("varchar2(80)").map("streetAddress.streetDetail.code").add();
			table.column("SASTREETNAME").type("varchar2(80)").map("streetAddress.streetDetail.name").add();
			table.column("SASTREETNUMBER").type("varchar2(80)").map("streetAddress.streetDetail.number").add();
			table.column("SASTREETPREFIX").type("varchar2(80)").map("streetAddress.streetDetail.prefix").add();
			table.column("SASTREETSUFFIX").type("varchar2(80)").map("streetAddress.streetDetail.suffix").add();
			table.column("SASTREETSUITENUMBER").type("varchar2(80)").map("streetAddress.streetDetail.suiteNumber").add();
			table.column("SASTREETTYPE").type("varchar2(80)").map("streetAddress.streetDetail.type").add();
			table.column("SASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("streetAddress.streetDetail.withinTownLimits").add();
			table.column("SATOWNCODE").type("varchar2(80)").map("streetAddress.townDetail.code").add();
			table.column("SATOWNCOUNTRY").type("varchar2(80)").map("streetAddress.townDetail.country").add();
			table.column("SATOWNNAME").type("varchar2(80)").map("streetAddress.townDetail.name").add();
			table.column("SATOWNSECTION").type("varchar2(80)").map("streetAddress.townDetail.section").add();
			table.column("SATOWNSTATE").type("varchar2(80)").map("streetAddress.townDetail.stateOrProvince").add();			
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
			table.column("FIRSTNAME").type("varchar2(80)").map("firstName").add();
			table.column("LASTNAME").type("varchar2(80)").map("lastName").add();
			table.column("MIDDLENAME").type("varchar2(80)").map("middleName").add();
			table.column("PREFIX").type("varchar2(80)").map("prefix").add();
			table.column("SUFFIX").type("varchar2(80)").map("suffix").add();
			table.column("SPECIALNEED").type("varchar2(80)").map("specialNeed").add();
			table.addAuditColumns();
			table.primaryKey("PTR_PK_PARTY").on(idColumn).add();
			table.unique("PTR_U_PARTY").on(mRIDColumn).add();
		}
	},
	PRT_PARTYREP (PartyRepresentation.class) {
		void describeTable(Table table) {
			table.map(PartyRepresentationImpl.class);
			Column delegateColumn = table.column("DELEGATE").type("varchar2(256)").notNull().map("delegate").add();
			Column partyIdColumn = table.column("PARTYID").number().notNull().conversion(NUMBER2LONG).add();
			List<Column> intervalColumns = table.addIntervalColumns("interval");			
			table.addAuditColumns();
			table.primaryKey("PRT_PK_PARTYREP").on(delegateColumn , partyIdColumn , intervalColumns.get(0)).add();
			table.foreignKey("PRT_FKPARTYREP").on(partyIdColumn).references(PRT_PARTY.name()).onDelete(DeleteRule.CASCADE).
				map("party").reverseMap("representations").reverseMapOrder("delegate").composition().add();
		}		
	},
	PRT_PARTYROLE (PartyRole.class) {
		void describeTable(Table table) {
			table.map(PartyRoleImpl.class);
			table.cache();
			Column mRIDColumn = table.column("MRID").type("varchar2(80)").notNull().map("mRID").add();
			table.column("COMPONENT").type("varchar2(3)").notNull().map("componentName").add();
			table.column("NAME").type("varchar2(80)").notNull().map("name").add();
			table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.addAuditColumns();
			table.primaryKey("PTR_PK_PARTYROLE").on(mRIDColumn).add();			
		}
	},
	PRT_PARTYINROLE(PartyInRole.class) {
		void describeTable(Table table) {
			table.map(PartyInRoleImpl.class);
			Column idColumn = table.addAutoIdColumn();
			Column partyIdColumn = table.column("PARTYID").number().notNull().conversion(NUMBER2LONG).add();
			Column roleMRIDColumn = table.column("PARTYROLEMRID").type("varchar2(80)").notNull().add();
			List<Column> intervalColumns = table.addIntervalColumns("interval");
			table.addAuditColumns();
			table.primaryKey("PTR_PK_PARTYINROLE").on(idColumn).add();
			table.unique("PTR_U_PARTYINROLE").on(partyIdColumn , roleMRIDColumn , intervalColumns.get(0)).add();
			table.foreignKey("PRT_FKPARTYINROLEPARTY").on(partyIdColumn).references(PRT_PARTY.name()).onDelete(DeleteRule.CASCADE).
				map("party").reverseMap("partyInRoles").composition().add();
			table.foreignKey("PRT_FKPARTYINROLEROLE").on(roleMRIDColumn).references(PRT_PARTYROLE.name()).onDelete(DeleteRule.RESTRICT).map("role").add();
		}
	};
		
	private Class<?> api;
	
	TableSpecs(Class<?> api) {
		this.api = api;
	}
	
	public void addTo(DataModel component) {
		Table table = component.addTable(name(),api);
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}