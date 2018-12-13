/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycles.model.DeviceLifeCycle', {
    extend: 'Uni.model.Version',
    fields: [
        'id',
        'name',
        {
            name: 'sorted_name',
            persist: false,
            mapping: function (data) {
                return data.name;
            }
        },
        {
            name: 'statesCount',
            persist: false
        },
        {
            name: 'actionsCount',
            persist: false
        },
        {
            name: 'deviceTypes',
            persist: false
        },
        {
            name: 'obsolete',
            persist: false
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles',
        reader: {
            type: 'json'            
        }
    }
});
