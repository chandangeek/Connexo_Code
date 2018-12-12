/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.AdaptedReadingsForBulk', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.ReadingTypeForAddTaskGrid',
    storeId: 'AdaptedReadingsForBulk',
    requires: [
        'Dxp.model.ReadingTypeForAddTaskGrid'
    ],
    autoLoad: false
});