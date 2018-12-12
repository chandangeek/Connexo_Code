/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransition', {
    extend: 'Uni.model.ParentVersion',
    fields: [
        'id',
        'name',
        'fromState',
        'toState',
        'privileges',
        'triggeredBy',
        {name: 'microActions', defaultValue: null},
        {name: 'microChecks', defaultValue: null},
        {
            name: 'fromState_name',
            persist: false,
            mapping: function (data) {
                return data.fromState.name;
            }
        },
        {
            name: 'toState_name',
            persist: false,
            mapping: function (data) {
                return data.toState.name;
            }
        },
        {
            name: 'triggeredBy_name',
            persist: false,
            mapping: function (data) {
                return data.triggeredBy.name;
            }
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dld/devicelifecycles/{id}/actions',
        reader: {
            type: 'json'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.deviceLifeCycleId);
        }
    }
});
