/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.DevicesOfZoneGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.zone-details-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DevicesOfZone'
    ],
    store: 'Mdc.store.DevicesOfZone',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.device', 'MDC', 'Device'),
                dataIndex: 'name',
                sortable: true,
                hideable: false,
                renderer: function (value, b, record) {
                    return '<a href="#/devices/' + encodeURIComponent(record.get('name')) + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                dataIndex: 'deviceTypeName',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                dataIndex: 'deviceConfigurationName',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn-remove',
                itemId: 'device-zone-remove-button',
                privileges: Cfg.privileges.Validation.adminZones,
                handler: function (grid, rowIndex, colIndex, column, event, deviceZoneRecord) {
                    me.fireEvent('zoneDeviceRemoveEvent', deviceZoneRecord);
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} devices'),
                displayMoreMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} devices'),
                emptyMsg: Uni.I18n.translate('devices.pagingtoolbartop.emptyMsg', 'MDC', 'There are no devices to display'),
                items: [
                    {
                        xtype: 'button',
                        ui: 'link',
                        itemId: 'view-all-devices-in-search',
                        text: Uni.I18n.translate('deviceZones.Search', 'MDC', 'View all devices in search'),
                        action: 'viewDevicesInSearch'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('devices.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Devices per page'),
                dock: 'bottom',
            }
        ];

        me.callParent(arguments);
    }
});



