/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.model.EditProcess', {
    extend: 'Ext.data.Model',
    requires: [
        'Dbp.processes.model.DeviceState',
        'Dbp.processes.model.Privilege'
    ],

    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'version',
            type: 'string'
        },
        {
            name: 'associatedTo',
            type: 'string'
        },
        {
            name: 'active',
            type: 'string'
        },
        {
            name: 'deviceStates',
            persist: false
        },
        {
            name: 'privileges',
            persist: false
        }
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Dbp.processes.model.DeviceState',
            associationKey: 'deviceStates',
            name: 'deviceStates'
        },
        {
            type: 'hasMany',
            model: 'Dbp.processes.model.Privilege',
            associationKey: 'privileges',
            name: 'privileges'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/process',
        reader: {
            type: 'json'
        }
    }
});