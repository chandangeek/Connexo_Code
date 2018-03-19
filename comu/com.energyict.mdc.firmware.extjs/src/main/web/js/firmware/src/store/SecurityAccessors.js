/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.store.SecurityAccessors', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.model.SecurityAccessor'
    ],
    model: 'Fwc.model.SecurityAccessor',
    storeId: 'SecurityAccessor',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/dtc/securityaccessors',
        reader: {
            type: 'json',
            root: 'securityaccessors'
        }
    }
});
