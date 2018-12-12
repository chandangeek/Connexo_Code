/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.ReadingTypesByAlias', {
    extend: 'Uni.data.store.Filterable',
    requires: ['Mtr.model.ReadingType'],
    model: 'Mtr.model.ReadingType',
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