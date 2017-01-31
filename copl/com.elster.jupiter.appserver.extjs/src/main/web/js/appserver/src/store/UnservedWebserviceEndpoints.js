/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.UnservedWebserviceEndpoints', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.WebserviceEndpoint',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/apr/appserver/{appServerName}/unusedendpoints',
        reader: {
            type: 'json'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,

        setUrl: function (appServerName) {
            this.url = this.urlTpl.replace('{appServerName}', appServerName);
        }
    }
});
