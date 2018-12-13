/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.ReadingTypesForTask', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Dxp.model.ReadingTypeForAddTaskGrid',
    storeId: 'ReadingTypesForTask',

    requires: [
        'Dxp.model.ReadingTypeForAddTaskGrid'
    ]
});