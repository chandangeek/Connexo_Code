package com.energyict.mdc.engine.monitor.app.impl;


import java.util.*;

class MdcMonitorAppPrivileges {

    public final static String MONITOR_COMMUNICATION_SERVER = "privilege.monitor.communication.server";

    static List<String> getApplicationPrivileges() {
        return Collections.singletonList(MONITOR_COMMUNICATION_SERVER);
      }

}