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
    },

    listeners: {
        beforeload: function(store, operation){
            var me = this,
                filter = [
                {
                    property: me.proxy.extraParams.property,
                    value: '*'+operation.params.value+'*'
                }
            ];

            me.proxy.extraParams.filter = JSON.stringify(filter);
        }
    }
});