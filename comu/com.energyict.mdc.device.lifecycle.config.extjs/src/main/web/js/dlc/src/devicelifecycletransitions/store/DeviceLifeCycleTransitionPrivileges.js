/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionPrivileges', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransitionPrivilege',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles/privileges',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'privileges'
        }
    }
});
