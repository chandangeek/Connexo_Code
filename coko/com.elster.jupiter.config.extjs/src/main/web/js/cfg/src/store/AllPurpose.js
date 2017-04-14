/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.AllPurpose', {
    extend: 'Uni.data.store.Filterable',

    fields: [
        'id',
        'name'
    ],

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/metrologypurposes',
        reader: {
            type: 'json',
            root: 'metrologyPurposes'
        }
    }
});
