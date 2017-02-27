/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.model.DeviceLifeCycleState', {
    extend: 'Uni.model.ParentVersion',
    alias: 'deviceLifeCycleState',
    requires: [
        'Dlc.devicelifecyclestates.model.TransitionBusinessProcess'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'isCustom', type: 'boolean'},
        {name: 'isInitial', type: 'boolean'},
        {name: 'stage', type: 'auto'},
        {
            name: 'sorted_name',
            persist: false,
            mapping: function (data) {
                return data.name;
            }
        },
        {name: 'onEntry', type: 'auto', defaultValue: []},
        {name: 'onExit', type: 'auto', defaultValue: []}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dld/devicelifecycles/{id}/states/',
        reader: {
            type: 'json'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.deviceLifeCycleId);

        }
    }
});
