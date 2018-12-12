/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.view.relativeperiod.CategoryFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'usage-category-filter',

    store: 'Tme.store.RelativePeriodUsage',

    requires: [
        'Tme.store.RelativePeriodUsageCategories'
    ],

    filters: [
        {
            type: 'combobox',
            itemId: 'tme-categories-combo-box',
            minWidth: 200,
            dataIndex: 'category',
            emptyText:  Uni.I18n.translate('relativeperiod.category', 'TME', 'Category'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'id',
            store: 'Tme.store.RelativePeriodUsageCategories'
        }
    ]
});