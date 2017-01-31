/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.store.Applications', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.Application',
    pageSize: 1000,
    sorters: {
        property: 'sortingfield',
        direction: 'ASC'
    }
});