/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.store.UsagePoint', {
    extend: 'Ext.data.Store',

    fields: [
        'id',
        'name'
    ],

    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints',
        reader: {
            type: 'json',
            root: 'usagePoints'
        }
    }

});
