/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.Subdomains',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Subdomain'
    ],
    model: 'Mdc.model.Subdomain',
    storeId: 'Subdomains',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/field/enddevicesubdomains',
        reader: {
            type: 'json',
            root: 'subDomains'
        }
    }
});