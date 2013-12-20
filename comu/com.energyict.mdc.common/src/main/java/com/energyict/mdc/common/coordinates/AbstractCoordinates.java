package com.energyict.mdc.common.coordinates;

public abstract class AbstractCoordinates implements Coordinates {


    public AbstractCoordinates() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractCoordinates)) {
            return false;
        }

        AbstractCoordinates that = (AbstractCoordinates) o;

        if (getLatitude() != null ? !getLatitude().equals(that.getLatitude()) : that.getLatitude() != null) {
            return false;
        }
        if (getLongitude() != null ? !getLongitude().equals(that.getLongitude()) : that.getLongitude() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getLatitude() != null ? getLatitude().hashCode() : 0;
        result = 31 * result + (getLongitude() != null ? getLongitude().hashCode() : 0);
        return result;
    }

    public String toString() {
        return "(" + getLatitude().toString() + ", " +
                getLongitude().toString() + ")";
    }
}
