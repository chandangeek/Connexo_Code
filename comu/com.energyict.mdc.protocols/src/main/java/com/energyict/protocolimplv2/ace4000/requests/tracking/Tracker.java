package com.energyict.protocolimplv2.ace4000.requests.tracking;

/**
 * Copyrights EnergyICT
 * Date: 22/01/13
 * Time: 9:20
 * Author: khe
 */
public class Tracker {

    private RequestType type;
    private int trackingId;

    public Tracker(RequestType type, int trackingId) {
        this.type = type;
        this.trackingId = trackingId;
    }

    public Tracker(RequestType type) {
        this.type = type;
        this.trackingId = -1;   //No tracking number is used
    }

    public Tracker(int trackingId) {
        this.type = null;
        this.trackingId = trackingId;
    }

    public RequestType getType() {
        return type;
    }

    public int getTrackingId() {
        return trackingId;
    }

    /**
     * Two trackers are equal if they have the same tracking number.
     * If one of them doesn't have a trackingId, they are identical if they have the same RequestType.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tracker) {
            Tracker otherTracker = (Tracker) obj;
            if (trackingId != -1 && otherTracker.getTrackingId() != -1) {
                return trackingId == otherTracker.getTrackingId();
            }
            return type == otherTracker.getType();
        }
        return false;
    }
}