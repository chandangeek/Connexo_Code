/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.AllReadingTypes', {
    extend: 'Ext.data.Store',

    fields: [
        'fullAliasName',
        'mRID'
    ],

    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        }
    }
});