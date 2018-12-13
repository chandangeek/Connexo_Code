/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.privileges.RegisteredDevicesKpi', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['registereddeviceskpi.privileges.administrate', 'registereddeviceskpi.privileges.view'],
    admin: ['registereddeviceskpi.privileges.administrate'],
    all: function () {
        return Ext.Array.merge(Mdc.privileges.RegisteredDevicesKpi.view, Mdc.privileges.RegisteredDevicesKpi.admin);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.RegisteredDevicesKpi.view);
    },
    canAdmin: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.RegisteredDevicesKpi.admin);
    }
});
