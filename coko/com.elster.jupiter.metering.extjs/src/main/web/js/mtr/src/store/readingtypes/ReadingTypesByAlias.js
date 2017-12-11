/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypes.ReadingTypesByAlias', {
    extend: 'Uni.data.store.Filterable',
    requires: ['Mtr.model.readingtypes.ReadingType'],
    model: 'Mtr.model.readingtypes.ReadingType',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '../../api/mtr/readingtypes/groups/{aliasName}/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        },

        setUrl: function (aliasName) {
            this.url = this.urlTpl.replace('{aliasName}', aliasName);
        }
    }
});