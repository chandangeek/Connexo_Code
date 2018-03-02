/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.store.CrlRequests', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.crlrequest.model.CrlRequest'
    ],
    model: 'Mdc.crlrequest.model.CrlRequest',
    storeId: 'CrlRequests',
    proxy: {
        type: 'rest',
        url: '/api/ddr/crls',
        reader: {
            type: 'json',
            root: 'crls'
        }
    }
});
