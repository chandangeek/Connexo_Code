/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.LoadProfilesOfDeviceDataFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        {name: 'intervalStart', type: 'date', dateFormat: 'Y-m-dTH:i:s'},
        {name: 'duration'},
        {name: 'onlySuspect'},
        {name: 'onlyNonSuspect'}
    ]
});