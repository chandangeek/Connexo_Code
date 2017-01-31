/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.store.SystemComponents', {
    extend: 'Ext.data.Store',
    model: 'Sam.model.SystemComponent',

    proxy: {
        type: 'rest',
        url: '/api/sys/components',
        reader: {
            type: 'json',
            root: 'components'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});