/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.ServedMessageServices', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.ServedMessageService',
    autoLoad: false,

    sorters: {
        property: 'messageService',
        direction: 'ASC'
    },

    proxy: {
        type: 'rest',
        urlTpl: '/api/apr/appserver/{appServerName}',
        reader: {
            type: 'json',
            root: 'executionSpecs'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,

        setUrl: function (appServerName) {
            this.url = this.urlTpl.replace('{appServerName}', appServerName);
        }
    }
});
