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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StreetAddress that = (StreetAddress) o;

        if (streetDetail != null ? !streetDetail.equals(that.streetDetail) : that.streetDetail != null) {
            return false;
        }
        return !(townDetail != null ? !townDetail.equals(that.townDetail) : that.townDetail != null);

    }

    @Override
    public int hashCode() {
        int result = streetDetail != null ? streetDetail.hashCode() : 0;
        result = 31 * result + (townDetail != null ? townDetail.hashCode() : 0);
        return result;
    }

    @Override
	public String toString() {
		return "" + getStreetDetail() + " " + getTownDetail();
	}
}
