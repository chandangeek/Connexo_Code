/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.WebServiceUpdatedEndPoint', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.WebServiceEndPointModel',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/export/selectors/{name}/endpoints?operation=CHANGE',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,

        reader: {
            type: 'json',
            root: 'endpoints'
        },
        setUrl: function (name) {
            this.url = this.urlTpl.replace('{name}', name);
        }
    }
});
