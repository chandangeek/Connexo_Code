/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.store.DeviceLifeCycleStates', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecyclestates.model.DeviceLifeCycleState',
    autoLoad: false,
    remoteSort: true,
    sorters: [
        {
            property: 'name',
            direction: 'ASC'
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dld/devicelifecycles/{id}/states',
        reader: {
            type: 'json',
            root: 'deviceLifeCycleStates'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.deviceLifeCycleId);
        }
    }
});