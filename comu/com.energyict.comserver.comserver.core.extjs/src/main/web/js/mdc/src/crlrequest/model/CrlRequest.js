/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.model.CrlRequest', {
    extend: 'Uni.model.Version',

    fields: [
        {name: 'securityAccessor', type: 'auto', defaultValue: null},
        {name: 'caName', type: 'string', defaultValue: null},
        {name: 'timeDurationInfo', type: 'auto', defaultValue: null},
        {name: 'nextRun', type: 'auto', defaultValue: new Date().getTime()}
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/crlprops',
        actionMethods: {
            create: 'PUT',
            read: 'GET',
            update: 'PUT',
            destroy: 'DELETE'
        }
    }
});