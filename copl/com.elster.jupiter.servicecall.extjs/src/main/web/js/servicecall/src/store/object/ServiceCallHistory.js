/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.store.object.ServiceCallHistory', {
    extend: 'Ext.data.Store',
    model: 'Scs.model.ServiceCall',
    autoLoad: false,
    proxy: {
        type: 'rest',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'serviceCalls'
        },

        setUrl: function (url) {
            this.url = url;
        }
    }
});
