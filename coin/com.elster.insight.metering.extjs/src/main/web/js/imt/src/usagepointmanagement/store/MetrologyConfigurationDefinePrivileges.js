/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.MetrologyConfigurationDefinePrivileges', {
    extend: 'Ext.data.Store',
    fields: [
        'name'
    ],

    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/privileges',

        reader: {
            type: 'json',
            root: 'privileges'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});