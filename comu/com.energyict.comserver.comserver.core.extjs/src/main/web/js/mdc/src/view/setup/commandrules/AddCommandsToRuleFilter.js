/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.commandrules.AddCommandsToRuleFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.AddCommandsToRuleFilter',

    requires: [
        'Mdc.store.Commands',
        'Mdc.store.CommandCategories'
    ],

    store: 'Mdc.store.Commands',

    requires: [
        'Mdc.store.Commands',
        'Mdc.store.CommandCategories'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                multiSelect: true,
                dataIndex: 'categories',
                emptyText: Uni.I18n.translate('general.category', 'MDC', 'Category'),
                displayField: 'name',
                valueField: 'id',
                itemId: 'mdc-commands-filter-category-combo',
                store: 'Mdc.store.CommandCategories'
            }
        ];

        me.callParent(arguments);
    }
});