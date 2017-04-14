/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.AllReadingTypes', {
    extend: 'Ext.data.Store',

    fields: [
        'aliasName',
        'mRID'
    ],

    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        }
    },

    listeners: {
        beforeload: function(){
            console.log('ssddds');
        }
    }
});