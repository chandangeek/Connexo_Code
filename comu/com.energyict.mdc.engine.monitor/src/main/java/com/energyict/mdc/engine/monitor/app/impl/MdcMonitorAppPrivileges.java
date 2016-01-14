package com.energyict.mdc.engine.monitor.app.impl;


import java.util.*;

class MdcMonitorAppPrivileges {

    static List<String> getApplicationPrivileges() {
        return Collections.singletonList(Constants.MONITOR_COMMUNICATION_SERVER);
      }

    public interface Constants {
        String MONITOR_COMMUNICATION_SERVER = "privilege.monitor.communication.server";
    }
}