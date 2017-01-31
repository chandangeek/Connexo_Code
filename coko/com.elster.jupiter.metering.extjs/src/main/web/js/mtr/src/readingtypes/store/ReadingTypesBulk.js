/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.store.ReadingTypesBulk', {
    extend: 'Uni.data.store.Filterable',
    requires: ['Mtr.readingtypes.model.ReadingType'],
    model: 'Mtr.readingtypes.model.ReadingType',
    storeId: 'ReadingTypesBulk',
    autoLoad: false,
    buffered: true,
    pageSize: 200,
    remoteFilter: true,
    proxy: {
                   type: 'rest',
                   url: '../../api/mtr/readingtypes',
                   reader: {
                       type: 'json',
                       root: 'readingTypes'
                   }
               }
});