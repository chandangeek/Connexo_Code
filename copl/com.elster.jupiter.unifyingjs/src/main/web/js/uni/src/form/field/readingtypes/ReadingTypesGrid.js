/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.field.readingtypes.ReadingTypesGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.grid.column.ReadingType'
    ],
    alias: 'widget.uni-reading-types-grid',

    selType: 'checkboxmodel',
    selModel: {
        mode: 'MULTI',
        checkOnly: true,
        showHeaderCheckbox: false,
        pruneRemoved: false,
        updateHeaderState: Ext.emptyFn
    },

    cls: 'uni-selection-grid',

    overflowY: 'auto',
    maxHeight: 450,

    columns: [
        {
            xtype: 'reading-type-column',
            valueIsRecordData: true,
            flex: 1
        }
    ]
});