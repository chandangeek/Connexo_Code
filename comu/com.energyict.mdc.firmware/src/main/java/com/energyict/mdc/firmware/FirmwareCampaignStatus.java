package com.energyict.mdc.firmware;

public enum FirmwareCampaignStatus {
    NOT_STARTED {
        @Override
        public boolean isValidStatusTransition(FirmwareCampaignStatus nextStatus) {
            return SCHEDULED.equals(nextStatus);
        }
    },
    SCHEDULED {
        @Override
        public boolean isValidStatusTransition(FirmwareCampaignStatus nextStatus) {
            return ONGOING.equals(nextStatus) || CANCELLED.equals(nextStatus);
        }
    },
    ONGOING {
        @Override
        public boolean isValidStatusTransition(FirmwareCampaignStatus nextStatus) {
            return COMPLETE.equals(nextStatus) || CANCELLED.equals(nextStatus) || FAILED.equals(nextStatus);
        }
    },
    COMPLETE {
        @Override
        public boolean isValidStatusTransition(FirmwareCampaignStatus nextStatus) {
            return false;
        }
    },
    CANCELLED {
        @Override
        public boolean isValidStatusTransition(FirmwareCampaignStatus nextStatus) {
            return false;
        }
    },
    FAILED {
        @Override
        public boolean isValidStatusTransition(FirmwareCampaignStatus nextStatus) {
            return false;
        }
    },
    ;

    public abstract boolean isValidStatusTransition(FirmwareCampaignStatus nextStatus);
}
