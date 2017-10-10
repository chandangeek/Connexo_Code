/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.readingtypes.store.ReadingTypeGroups', {
    extend: 'Uni.data.store.Filterable',
    requires: ['Mtr.readingtypes.model.ReadingTypeGroup'],
    model: 'Mtr.readingtypes.model.ReadingTypeGroup',
    storeId: 'ReadingTypeGroups',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '../../api/mtr/readingtypes/groups',
        reader: {
            type: 'json',
            root: 'groups'
        }
    }
});