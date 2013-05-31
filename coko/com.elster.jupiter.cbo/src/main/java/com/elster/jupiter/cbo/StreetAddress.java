package com.elster.jupiter.cbo;

public final class StreetAddress implements Cloneable {
	private Status status;
	private StreetDetail streetDetail;
	private TownDetail townDetail;
	
	public StreetAddress(StreetDetail streetDetail , TownDetail townDetail , Status status) {
		this.streetDetail = streetDetail;
		this.townDetail = townDetail;
		this.status = status;
	}
	
	public StreetAddress(StreetDetail streetDetail , TownDetail townDetail ) {
		this(streetDetail,townDetail,new Status());
	}
	
	public StreetAddress() {
		this(new StreetDetail(),new TownDetail());
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
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
	
	public StreetAddress copy() {
		try {
			StreetAddress result = (StreetAddress) this.clone();
			result.streetDetail = this.streetDetail.copy();
			result.townDetail = this.townDetail.copy();
			result.status = this.status.copy();
			return result;
		} catch (CloneNotSupportedException ex) {
			throw new UnsupportedOperationException(ex);
		}
	}
	
	public boolean isEmpty() {
		return status.isEmpty() && townDetail.isEmpty() && streetDetail.isEmpty();
	}
	
	@Override
	public String toString() {
		return "" + getStreetDetail() + " " + getTownDetail();
	}
}
