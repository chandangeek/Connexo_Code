package com.energyict.protocolimplv2.eict.eiweb;

public class EIWebBulkMessageSeed {

    public static String MAX_CONFIGURED_WAIT_TIME_X_EXCEEDS_THE_MAXIMUM_ALLOWED_Y =
                            "The configured max wait time {0} ms exceeds the maximum allowed value of {1} ms, " +
                            "Rejecting Message";
    public static String DATA_IN_FUTURE_DIFFERENCE_X_NO_WAIT_TIME_CONFIGURED_REJECTING_MESSAGE =
                            "Received load profile data in the future, difference {0} ms no wait time configured on device, " +
                            "Rejecting Message";
    public static String DATA_IN_FUTURE_WHICH_DIFFERENCE_X_EXCEED_MAXIMUM_CONFIGURED_Y =
                            "Received load profile data in the future, difference {0} ms which exceeds the maximum " +
                            "configured wait time of {1} ms ";
    public static String DATA_IN_FUTURE_CONFIGURED_DIFFERENCE_X_MAX_Y_START_WAITING =
                            "Received load profile data in the future, difference {0} ms which is within the configured " +
                            "wait time of {1} ms, Start waiting";

    public static String DATA_IN_FUTURE_CONFIGURED_DIFFERENCE_X_MAX_Y_FINISHED_WAITING =
                            "Received load profile data in the future, difference {0} ms which is within the configured " +
                            "wait time of {1} ms, Finished waiting";

}
