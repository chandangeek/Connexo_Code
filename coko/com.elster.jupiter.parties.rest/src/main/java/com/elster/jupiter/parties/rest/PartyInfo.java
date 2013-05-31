package com.elster.jupiter.parties.rest;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.parties.Party;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PartyInfo {
    // party
    public long id;
    public String mRID;
    public String name;
    public String aliasName;
    public String description;
    public ElectronicAddress electronicAddress;
    public long version;
    
    public PartyInfo(Party party) {
        id = party.getId();
        mRID = party.getMRID();
        name = party.getName();
        aliasName = party.getAliasName();
        description = party.getDescription();
        electronicAddress = party.getElectronicAddress();
        version = party.getVersion();
    }

    public PartyInfo() {
    }

    void updateParty(Party party) {
        party.setMRID(mRID);
        party.setName(name);
        party.setAliasName(aliasName);
        party.setDescription(description);
        party.setElectronicAddress(electronicAddress);
    }
}
