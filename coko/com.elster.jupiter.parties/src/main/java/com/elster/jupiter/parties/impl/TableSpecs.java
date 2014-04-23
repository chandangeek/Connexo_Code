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
	PRT_PARTY () {
		void addTo(DataModel dataModel) {
			Table<Party> table = dataModel.addTable(name(), Party.class);
			table.map(PartyImpl.IMPLEMENTERS);
			table.setJournalTableName("PRT_PARTYJRNL");
			Column idColumn = table.addAutoIdColumn();
			table.addDiscriminatorColumn("PARTYTYPE", "char(1)");
			Column mRIDColumn = table.column("MRID").varChar(80).map("mRID").add();
			Column nameColumn = table.column("NAME").varChar().map("name").add();
			table.column("ALIASNAME").varChar().map("aliasName").add();
			table.column("DESCRIPTION").varChar(256).map("description").add();
			table.column("EAEMAIL1").varChar(80).map("electronicAddress.email1").add();
			table.column("EAEMAIL2").varChar(80).map("electronicAddress.email2").add();
			table.column("EALAN").varChar(80).map("electronicAddress.lan").add();
			table.column("EAEMAC").varChar(80).map("electronicAddress.mac").add();
			table.column("EAPASSWORD").varChar(80).map("electronicAddress.password").add();
			table.column("EARADIO").varChar(80).map("electronicAddress.radio").add();
			table.column("EAUSERID").varChar(80).map("electronicAddress.userID").add();
			table.column("EAWEB").varChar(80).map("electronicAddress.web").add();
			table.column("PAPOBOX").varChar(80).map("postalAddress.poBox").add();
			table.column("PAPOSTALCODE").varChar(80).map("postalAddress.postalCode").add();			
			table.column("PASTREETADDRESSGENERAL").varChar(80).map("postalAddress.streetDetail.addressGeneral").add();
			table.column("PASTREETBUILDINGNAME").varChar(80).map("postalAddress.streetDetail.buildingName").add();
			table.column("PASTREETCODE").varChar(80).map("postalAddress.streetDetail.code").add();
			table.column("PASTREETNAME").varChar(80).map("postalAddress.streetDetail.name").add();
			table.column("PASTREETNUMBER").varChar(80).map("postalAddress.streetDetail.number").add();
			table.column("PASTREETPREFIX").varChar(80).map("postalAddress.streetDetail.prefix").add();
			table.column("PASTREETSUFFIX").varChar(80).map("postalAddress.streetDetail.suffix").add();
			table.column("PASTREETSUITENUMBER").varChar(80).map("postalAddress.streetDetail.suiteNumber").add();
			table.column("PASTREETTYPE").varChar(80).map("postalAddress.streetDetail.type").add();
			table.column("PASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("postalAddress.streetDetail.withinTownLimits").add();
			table.column("PATOWNCODE").varChar(80).map("postalAddress.townDetail.code").add();
			table.column("PATOWNCOUNTRY").varChar(80).map("postalAddress.townDetail.country").add();
			table.column("PATOWNNAME").varChar(80).map("postalAddress.townDetail.name").add();
			table.column("PATOWNSECTION").varChar(80).map("postalAddress.townDetail.section").add();
			table.column("PATOWNSTATE").varChar(80).map("postalAddress.townDetail.stateOrProvince").add();			
			table.column("SASTATUSDATETIME").number().conversion(NUMBER2UTCINSTANT).map("streetAddress.status.dateTime").add();
			table.column("SASTATUSREASON").varChar(80).map("streetAddress.status.reason").add();
			table.column("SASTATUSREMARK").varChar(80).map("streetAddress.status.remark").add();
			table.column("SASTATUSVALUE").varChar(80).map("streetAddress.status.value").add();	
			table.column("SASTREETADDRESSGENERAL").varChar(80).map("streetAddress.streetDetail.addressGeneral").add();
			table.column("SASTREETBUILDINGNAME").varChar(80).map("streetAddress.streetDetail.buildingName").add();
			table.column("SASTREETCODE").varChar(80).map("streetAddress.streetDetail.code").add();
			table.column("SASTREETNAME").varChar(80).map("streetAddress.streetDetail.name").add();
			table.column("SASTREETNUMBER").varChar(80).map("streetAddress.streetDetail.number").add();
			table.column("SASTREETPREFIX").varChar(80).map("streetAddress.streetDetail.prefix").add();
			table.column("SASTREETSUFFIX").varChar(80).map("streetAddress.streetDetail.suffix").add();
			table.column("SASTREETSUITENUMBER").varChar(80).map("streetAddress.streetDetail.suiteNumber").add();
			table.column("SASTREETTYPE").varChar(80).map("streetAddress.streetDetail.type").add();
			table.column("SASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("streetAddress.streetDetail.withinTownLimits").add();
			table.column("SATOWNCODE").varChar(80).map("streetAddress.townDetail.code").add();
			table.column("SATOWNCOUNTRY").varChar(80).map("streetAddress.townDetail.country").add();
			table.column("SATOWNNAME").varChar(80).map("streetAddress.townDetail.name").add();
			table.column("SATOWNSECTION").varChar(80).map("streetAddress.townDetail.section").add();
			table.column("SATOWNSTATE").varChar(80).map("streetAddress.townDetail.stateOrProvince").add();			
			table.column("PHONE1AREA").varChar(80).map("phone1.areaCode").add();
			table.column("PHONE1CITY").varChar(80).map("phone1.cityCode").add();
			table.column("PHONE1COUNTRY").varChar(80).map("phone1.countryCode").add();
			table.column("PHONE1EXTENSION").varChar(80).map("phone1.extension").add();
			table.column("PHONE1LOCALNUMBER").varChar(80).map("phone1.localNumber").add();
			table.column("PHONE2AREA").varChar(80).map("phone2.areaCode").add();
			table.column("PHONE2CITY").varChar(80).map("phone2.cityCode").add();
			table.column("PHONE2COUNTRY").varChar(80).map("phone2.countryCode").add();
			table.column("PHONE2EXTENSION").varChar(80).map("phone2.extension").add();
			table.column("PHONE2LOCALNUMBER").varChar(80).map("phone2.localNumber").add();
			table.column("FIRSTNAME").varChar(80).map("firstName").add();
			table.column("LASTNAME").varChar(80).map("lastName").add();
			table.column("MIDDLENAME").varChar(80).map("middleName").add();
			table.column("PREFIX").varChar(80).map("prefix").add();
			table.column("SUFFIX").varChar(80).map("suffix").add();
			table.column("SPECIALNEED").varChar(80).map("specialNeed").add();
			table.column("UPPERNAME").varChar(80).as("UPPER(NAME)").alias("upperName").add();
			table.addAuditColumns();
			table.primaryKey("PTR_PK_PARTY").on(idColumn).add();
			table.unique("PTR_U_PARTY").on(mRIDColumn).add();
			table.index("PTR_IX_PARTY_NAME").on(nameColumn).add();
		}
	},
	PRT_PARTYREP {
		void addTo(DataModel dataModel) {
			Table<PartyRepresentation> table = dataModel.addTable(name(), PartyRepresentation.class);
			table.map(PartyRepresentationImpl.class);
			Column delegateColumn = table.column("DELEGATE").varChar(256).notNull().map("delegate").add();
			Column partyIdColumn = table.column("PARTYID").number().notNull().conversion(NUMBER2LONG).add();
			List<Column> intervalColumns = table.addIntervalColumns("interval");			
			table.addAuditColumns();
			table.primaryKey("PRT_PK_PARTYREP").on(delegateColumn , partyIdColumn , intervalColumns.get(0)).add();
			table.foreignKey("PRT_FKPARTYREP").on(partyIdColumn).references(PRT_PARTY.name()).onDelete(DeleteRule.CASCADE).
				map("party").reverseMap("representations").reverseMapOrder("delegate").composition().add();
		}		
	},
	PRT_PARTYROLE {
		void addTo(DataModel dataModel) {
			Table<PartyRole> table = dataModel.addTable(name(), PartyRole.class);
			table.map(PartyRoleImpl.class);
			table.cache();
			Column mRIDColumn = table.column("MRID").varChar(80).notNull().map("mRID").add();
			table.column("COMPONENT").varChar(3).notNull().map("componentName").add();
			table.column("NAME").varChar(80).notNull().map("name").add();
			table.column("ALIASNAME").varChar(80).map("aliasName").add();
			table.column("DESCRIPTION").varChar(256).map("description").add();
			table.addAuditColumns();
			table.primaryKey("PTR_PK_PARTYROLE").on(mRIDColumn).add();			
		}
	},
	PRT_PARTYINROLE {
		void addTo(DataModel dataModel) {
			Table<PartyInRole> table = dataModel.addTable(name(), PartyInRole.class);
			table.map(PartyInRoleImpl.class);
			Column idColumn = table.addAutoIdColumn();
			Column partyIdColumn = table.column("PARTYID").number().notNull().conversion(NUMBER2LONG).add();
			Column roleMRIDColumn = table.column("PARTYROLEMRID").varChar(80).notNull().add();
			List<Column> intervalColumns = table.addIntervalColumns("interval");
			table.addAuditColumns();
			table.column("UPPERROLEMRID").varChar(80).as("upper(PARTYROLEMRID)").alias("upperPartyRoleMRID").add();
			table.primaryKey("PTR_PK_PARTYINROLE").on(idColumn).add();
			table.unique("PTR_U_PARTYINROLE").on(partyIdColumn , roleMRIDColumn , intervalColumns.get(0)).add();
			table.foreignKey("PRT_FKPARTYINROLEPARTY").on(partyIdColumn).references(PRT_PARTY.name()).onDelete(DeleteRule.CASCADE).
				map("party").reverseMap("partyInRoles").composition().add();
			table.foreignKey("PRT_FKPARTYINROLEROLE").on(roleMRIDColumn).references(PRT_PARTYROLE.name()).onDelete(DeleteRule.RESTRICT).map("role").add();
		}
	};
	
	abstract void addTo(DataModel component);
		
	
}