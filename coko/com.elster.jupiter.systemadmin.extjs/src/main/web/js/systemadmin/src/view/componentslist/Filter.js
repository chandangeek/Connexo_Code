/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.componentslist.Filter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.components-filter',
    store: 'Sam.store.SystemComponents',

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'application',
                emptyText: Uni.I18n.translate('general.application', 'SAM', 'Application'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Sam.store.AvailableAndLicensedApplications'
            },
            {
                type: 'combobox',
                dataIndex: 'bundleType',
                emptyText: Uni.I18n.translate('general.bundleType', 'SAM', 'Bundle type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Sam.store.BundleTypes'
            },
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'SAM', 'Status'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Sam.store.ComponentStatuses'
            }
        ];

        me.callParent(arguments);
    }
});