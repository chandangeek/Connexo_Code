/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.DataLoggerSlavesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dataLoggerSlavesGrid',
    itemId: 'mdc-dataloggerslaves-grid',
    store: 'Mdc.store.DataLoggerSlaves',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DataLoggerSlaves',
        'Mdc.view.setup.dataloggerslaves.DataLoggerSlavesActionMenu'
    ],
    purpose: undefined,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                renderer: function (value, meta, record) {
                    var href = me.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(record.get('name'))});
                    return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
                },
                getSortParam: function() { Ext.emptyFn }, // We don't want a sort icon in the header
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.serialNumber', 'MDC', 'Serial number'),
                dataIndex: 'serialNumber',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                dataIndex: 'deviceTypeName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.configuration', 'MDC', 'Configuration'),
                dataIndex: 'deviceConfigurationName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.linkedOn', 'MDC', 'Linked on'),
                dataIndex: 'linkingTimeStamp',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                }
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'dataloggerslaves-action-menu',
                    itemId: 'mdc-dataloggerslaves-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                deferLoading: true,
                dock: 'top',
                displayMsg: me.purpose.slavesGridDisplayMsg,
                displayMoreMsg: me.purpose.slavesGridDisplayMoreMsg,
                emptyMsg: me.purpose.slavesGridEmptyMsg,
                items: [
                    {
                        xtype: 'button',
                        text: me.purpose.displayValue,
                        itemId: 'mdc-dataloggerslavesgrid-link-slave-btn',
                        privileges: Mdc.privileges.Device.administrateDevice
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                deferLoading: true,
                store: me.store,
                itemsPerPageMsg: me.purpose.slavesGridItemsPerPageMsg,
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});