/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import javax.xml.bind.annotation.XmlTransient;

public final class PostalAddress implements Cloneable {
	private String postalCode;
	private String poBox;	
	private StreetDetail streetDetail;
	private TownDetail townDetail;
	
	public PostalAddress(String postalCode, String poBox , StreetDetail streetDetail , TownDetail townDetail) {
		this.postalCode = postalCode;
		this.poBox = poBox;
		this.streetDetail = streetDetail;
		this.townDetail = townDetail;		
	}
	
	public PostalAddress(StreetDetail streetDetail , TownDetail townDetail ) {
		this(null,null,streetDetail,townDetail);
	}
	
	public PostalAddress() {
		this(new StreetDetail(),new TownDetail());
	}

	public StreetDetail getStreetDetail() {
		return streetDetail;
	}

	public void setStreetDetail(StreetDetail streetDetail) {
		this.streetDetail = streetDetail;
	}

	public TownDetail getTownDetail() {
		return townDetail;
	}

	public void setTownDetail(TownDetail townDetail) {
		this.townDetail = townDetail;
	}
	
	public PostalAddress copy() {
		try {
			PostalAddress result = (PostalAddress) this.clone();
			result.streetDetail = this.streetDetail.copy();
			result.townDetail = this.townDetail.copy();			
			return result;
		} catch (CloneNotSupportedException ex) {
			throw new UnsupportedOperationException(ex);
		}
	}
	
	@XmlTransient
	public boolean isEmpty() {
		return postalCode == null && poBox == null && townDetail.isEmpty() && streetDetail.isEmpty();
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getPoBox() {
		return poBox;
	}

	public void setPoBox(String poBox) {
		this.poBox = poBox;
	}
	
	@Override
	public String toString() {
		return "" + postalCode + " " + poBox + " " + getStreetDetail() + " " + getTownDetail();
	}
}
