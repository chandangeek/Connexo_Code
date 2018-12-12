/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.ServedWebserviceEndpoints', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.WebserviceEndpoint',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/apr/appserver/{appServerName}',
        reader: {
            type: 'json',
            root: 'endPointConfigurations'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,

        setUrl: function (appServerName) {
            this.url = this.urlTpl.replace('{appServerName}', appServerName);
        }
    }
});
