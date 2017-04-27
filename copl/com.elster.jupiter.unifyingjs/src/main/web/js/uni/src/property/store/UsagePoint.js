/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.AllUsagePoint', {
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
