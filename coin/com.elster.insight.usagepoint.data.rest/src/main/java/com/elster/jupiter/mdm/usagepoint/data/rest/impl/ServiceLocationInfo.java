/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.metering.ServiceLocation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceLocationInfo {
	public long id;
	public String aliasName;
	public String description;
	String mRID;
	public String name;
	public String direction;
	public ElectronicAddress electronicAddress;
	public String geoInfoReference;
	public StreetAddress mainAddress;
	public TelephoneNumber phone1;
	public TelephoneNumber phone2;
	public StreetAddress secondaryAddress;
	public Status status;
	public String type;
	public String accessMethod;
	public boolean needsInspection;
	public String siteAccessProblem;
	public long createTime;
	public long modTime;
	public long version;
	
	public ServiceLocationInfo() {		
	}
	
	public ServiceLocationInfo(ServiceLocation serviceLocation) {
		id = serviceLocation.getId();
		aliasName = serviceLocation.getAliasName();
		description = serviceLocation.getDescription();
		mRID = serviceLocation.getMRID();
		name = serviceLocation.getName();
		direction = serviceLocation.getDirection();
		electronicAddress = serviceLocation.getElectronicAddress();
		geoInfoReference = serviceLocation.getGeoInfoReference();
		mainAddress = serviceLocation.getMainAddress();
		phone1 = serviceLocation.getPhone1();
		phone2 = serviceLocation.getPhone2();
		secondaryAddress = serviceLocation.getSecondaryAddress();
		status = serviceLocation.getStatus();
		type = serviceLocation.getType();
		accessMethod = serviceLocation.getAccessMethod();
		needsInspection = serviceLocation.isNeedsInspection();
		siteAccessProblem = serviceLocation.getSiteAccessProblem();
		createTime = serviceLocation.getCreateDate().toEpochMilli();
		modTime = serviceLocation.getModificationDate().toEpochMilli();
		version = serviceLocation.getVersion();
	}

}
