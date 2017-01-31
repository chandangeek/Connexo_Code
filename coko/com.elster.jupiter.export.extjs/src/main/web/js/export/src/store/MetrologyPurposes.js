/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.MetrologyPurposes', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.IdWithName',
    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/metrologypurposes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'metrologyPurposes'
        }
    }
});