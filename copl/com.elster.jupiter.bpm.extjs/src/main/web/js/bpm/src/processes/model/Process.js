/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.model.Process', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'processId',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'deploymentId',
            type: 'string'
        },
        {
            name: 'version',
            type: 'string'
        },
        {
            name: 'type',
            type: 'string'
        },
        {
            name: 'displayType',
            type: 'string'
        },
        {
            name: 'active',
            type: 'string'
        },
        {
            name: 'appKey',
            type: 'string'
        },
        {
            name: 'versionDB',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/process',
        reader: {
            type: 'json'
        },
        setUrl: function(url){
            this.url = url;
        }

    }
});