/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.ReadingTypesToAddForRule', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.ReadingType',
    storeId: 'ReadingTypesToAddForRule',
    requires: [
        'Cfg.model.ReadingType'
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