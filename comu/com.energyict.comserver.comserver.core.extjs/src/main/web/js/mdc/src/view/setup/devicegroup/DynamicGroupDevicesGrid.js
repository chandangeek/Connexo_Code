/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.DynamicGroupDevicesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dynamic-group-devices-grid',
    store: 'Mdc.store.DynamicGroupDevices',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.store.search.Results',
        'Uni.view.search.ColumnPicker',
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    plugins: [{
        ptype: 'showConditionalToolTip',
        pluginId: 'showConditionalToolTipId'
    }],

    forceFit: true,
    enableColumnMove: true,
    columns: [],
    config: {
        service: null
    },

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} devices'),
                displayMoreMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} devices'),
                emptyMsg: Uni.I18n.translate('devices.pagingtoolbartop.emptyMsg', 'MDC', 'There are no devices to display'),
                items: {
                    xtype: 'uni-search-column-picker',
                    itemId: 'dynamic-column-picker',
                    grid: me
                }
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('devices.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Devices per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];

        me.callParent(arguments);
    }
});