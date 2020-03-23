/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.model.CrlRequest', {
    extend: 'Uni.model.Version',

    fields: [
        {name: 'securityAccessor', type: 'auto', defaultValue: null},
        {name: 'caName', type: 'string', defaultValue: null},
        {name: 'logLevel', type: 'auto', defaultValue: null},
        {name: 'periodicalExpressionInfo', type: 'auto', defaultValue: null},
        {name: 'nextRun', type: 'auto', defaultValue: new Date().getTime()},
        {name: 'task', type: 'auto', defaultValue: null}
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/crlprops',
        actionMethods: {
            create: 'POST',
            read: 'GET',
            update: 'PUT',
            destroy: 'DELETE'
        }
    }
});