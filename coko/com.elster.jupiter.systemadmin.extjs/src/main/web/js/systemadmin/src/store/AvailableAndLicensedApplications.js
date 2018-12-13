/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.store.AvailableAndLicensedApplications', {
    extend: 'Ext.data.Store',
    model: 'Sam.model.AvailableAndLicensedApplication',

    proxy: {
        type: 'rest',
        url: '/api/sys/fields/applications',
        reader: {
            type: 'json',
            root: 'applications'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});