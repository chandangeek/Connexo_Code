/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.UnservedImportServices', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.ImportService',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/apr/appserver/{appServerName}/unservedimport',
        reader: {
            type: 'json',
            root: 'importServices'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,

        setUrl: function (appServerName) {
            this.url = this.urlTpl.replace('{appServerName}', appServerName);
        }
    }
});
