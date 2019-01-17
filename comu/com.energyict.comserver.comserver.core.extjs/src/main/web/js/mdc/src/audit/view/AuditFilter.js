/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.view.AuditFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.auditFilter',

    store: 'Mdc.audit.store.Audit',

    requires: [
        'Mdc.audit.store.Audit',
        'Mdc.audit.store.Categories',
        'Mdc.audit.store.Users'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'interval',
                dataIndex: 'changed',
                dataIndexFrom: 'changedOnFrom',
                dataIndexTo: 'changedOnTo',
                itemId: 'audit-filter-changed-interval',
                text: Uni.I18n.translate('audit.filter.changedBetween', 'MDC', 'Changed between')
            },
            {
                type: 'combobox',
                multiSelect: true,
                dataIndex: 'categories',
                emptyText: Uni.I18n.translate('audit.filter.category', 'MDC', 'Categories'),
                displayField: 'name',
                valueField: 'id',
                itemId: 'audit-filter-category-combo',
                store: 'Mdc.audit.store.Categories'
            },
            {
                type: 'combobox',
                multiSelect: true,
                dataIndex: 'users',
                emptyText: Uni.I18n.translate('audit.filter.changedBy', 'MDC', 'Changed by'),
                displayField: 'name',
                valueField: 'name',
                itemId: 'audit-filter-changedBy-combo',
                store: 'Mdc.audit.store.Users',
                queryMode: 'remote',
                remoteFilter: true,
                queryParam: 'like',
                queryCaching: false,
                minChars: 1,
                editable: true
            }
        ];

        me.callParent(arguments);
    }
});