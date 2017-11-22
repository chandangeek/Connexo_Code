/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypesgroup.ReadingTypeGroups', {
    extend: 'Uni.data.store.Filterable',
    requires: ['Mtr.model.readingtypesgroup.ReadingTypeGroup'],
    model: 'Mtr.model.readingtypesgroup.ReadingTypeGroup',
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