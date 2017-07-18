/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.store.DynamicPrivileges', {
    extend: 'Ext.data.Store',
    singleton: true,

    fields: [
        'name'
    ],

    autoLoad: false
});