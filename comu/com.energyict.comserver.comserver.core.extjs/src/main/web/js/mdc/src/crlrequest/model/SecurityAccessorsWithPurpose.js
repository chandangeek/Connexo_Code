/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.model.SecurityAccessorsWithPurpose', {
    extend: 'Uni.model.Version',

    fields: [
        {name: 'securityAccessorName', type: 'auto', defaultValue: null, persist:false},
        {name: 'securityAccessor', type: 'auto', defaultValue: null},
        {name: 'caName', type: 'string', defaultValue: null},
        {name: 'timeDurationInfo', type: 'auto', defaultValue: null},
        {name: 'nextRun', type: 'auto', defaultValue: new Date().getTime()}

    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/crlprops'
    }
});