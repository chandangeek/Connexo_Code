/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.main.store.ReadingTypes', {
    extend: 'Ext.data.Store',
    model: 'Est.main.model.ReadingType',
    storeId: 'ReadingTypesToAddToEstimationRule',
    requires: [
        'Est.main.model.ReadingType'
    ],

    buffered: true,
    pageSize: 200,

    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        }
    },

    getCount: function () {
        if (this.lastRequestEnd < 0) {
            return 0
        } else {
            return this.data.getCount();

        }
    }
});