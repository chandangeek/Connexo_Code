/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Mdc.privileges.Monitor
 *
 * Class that defines privileges for the communication server monitor
 */
Ext.define('Mdc.privileges.Monitor', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    monitor: ['privilege.monitor.communication.server'],
    all: function() {
        return this.canMonitor();
    },
    canMonitor: function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.Monitor.monitor);
    }
});
