package com.elster.jupiter.util.geo;

import java.util.Objects;

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
        return Objects.hash(getLatitude(), getLongitude());
    }

    @Override
    public String toString() {
        return getLatitude().toString() + ", " +
                getLongitude().toString();
    }
}
