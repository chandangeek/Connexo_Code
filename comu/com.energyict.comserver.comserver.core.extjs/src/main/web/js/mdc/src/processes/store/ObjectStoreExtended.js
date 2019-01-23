/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.store.ObjectStoreExtended', {
    extend: 'Ext.data.Store',
    model: 'Mdc.processes.model.ObjectModelExtended',
    pageSize: 50,
    autoLoad: false,
    requires: [
        'Ext.data.proxy.Rest'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/flowprocesses/{type}',
        reader: {
            type: 'json',
            root: 'data'
        },
        /* Objecttype can be for example "alarmobjects" or "deviceobjects" */
        setUrl: function (objecttype) {
             this.url = this.urlTpl.replace('{type}', objecttype);
        }
    }
});