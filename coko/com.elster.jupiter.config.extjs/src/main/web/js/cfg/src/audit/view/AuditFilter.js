/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.audit.view.AuditFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.auditFilter',
    requires: [
        'Cfg.audit.store.Audit',
        'Cfg.audit.store.Categories',
        'Cfg.audit.store.Users'
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
                text: Uni.I18n.translate('audit.filter.changedBetween', 'CFG', 'Changed between')
            },
            {
                type: 'combobox',
                multiSelect: true,
                dataIndex: 'categories',
                emptyText: Uni.I18n.translate('audit.filter.category', 'CFG', 'Categories'),
                displayField: 'name',
                valueField: 'id',
                itemId: 'audit-filter-category-combo',
                store: 'Cfg.audit.store.Categories'
            },
            {
                type: 'combobox',
                multiSelect: true,
                dataIndex: 'users',
                emptyText: Uni.I18n.translate('audit.filter.changedBy', 'CFG', 'Changed by'),
                displayField: 'name',
                valueField: 'name',
                itemId: 'audit-filter-changedBy-combo',
                store: 'Cfg.audit.store.Users',
                queryMode: 'remote',
                remoteFilter: true,
                queryParam: 'like',
                queryCaching: false,
                minChars: 0,
                editable: true
            }
        ];

        me.callParent(arguments);
    }
});