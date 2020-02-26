/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.store.CRLSigners', {
    extend: 'Ext.data.Store',
    require: ['Mdc.crlrequest.model.CRLSigners'],
    model: 'Mdc.crlrequest.model.CRLSigners',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/ddr/crlprops/crlsigners',
        reader: {
            type: 'json',
            root: 'crlsigners'
        }
    }
});
