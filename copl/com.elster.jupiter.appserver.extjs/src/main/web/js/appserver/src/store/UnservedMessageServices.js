/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.UnservedMessageServices', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.UnservedMessageService',
    autoLoad: false,

    sorters: {
        property: 'messageService',
        direction: 'ASC'
    },

    proxy: {
        type: 'rest',
        urlTpl: '/api/apr/appserver/{appServerName}/unserved',
        reader: {
            type: 'json',
            root: 'subscriberSpecs'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,

        setUrl: function (appServerName) {
            this.url = this.urlTpl.replace('{appServerName}', appServerName);
        }
    }
});
