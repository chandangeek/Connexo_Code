/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.ReadingTypeGroups', {
    extend: 'Uni.data.store.Filterable',
    requires: ['Mtr.model.ReadingTypeGroup'],
    model: 'Mtr.model.ReadingTypeGroup',
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