/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitions', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransition',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dld/devicelifecycles/{id}/actions',
        reader: {
            type: 'json',
            root: 'deviceLifeCycleActions'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.deviceLifeCycleId);
        }
    }
});