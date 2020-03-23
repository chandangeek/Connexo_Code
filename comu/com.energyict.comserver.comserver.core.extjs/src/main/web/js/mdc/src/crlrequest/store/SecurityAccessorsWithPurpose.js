/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.store.SecurityAccessorsWithPurpose', {
    extend: 'Ext.data.Store',
    require: ['Mdc.crlrequest.model.SecurityAccessorsWithPurpose'],
    model: 'Mdc.crlrequest.model.SecurityAccessorsWithPurpose',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/ddr/crlprops/securityaccessors',
        reader: {
            type: 'json',
            root: 'securityAccessors'
        }
    }
});
