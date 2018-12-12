/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.ReadingTypesBulk', {
    extend: 'Uni.data.store.Filterable',
    requires: ['Mtr.model.ReadingType'],
    model: 'Mtr.model.ReadingType',
    storeId: 'ReadingTypesBulk',
    autoLoad: false,
    buffered: true,
    pageSize: 200,
    remoteFilter: true,
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