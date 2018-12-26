/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.ZonesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-zones-grid',
    overflowY: 'auto',
    store: 'Mdc.store.DeviceZones',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceZones',
        'Uni.grid.column.Default',
        'Mdc.view.setup.devicezones.ZonesActionsMenu'
    ],
    device: null,


    initComponent: function () {
        var me = this;
        me.deviceId = me.device.get('name');
        me.columns = [
            {
                header: Uni.I18n.translate('deviceZones.zoneType', 'MDC', 'Zone Type'),
                dataIndex: 'zoneTypeName',
                flex: 2,
                valueField: 'zoneTypeId',
                displayField: 'zoneTypeName',
            },
            {
                header: Uni.I18n.translate('deviceZones.zone', 'MDC', 'Zone'),
                dataIndex: 'zoneName',
                flex: 1,
                valueField: 'zoneId',
                displayField: 'zoneName',
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                menu: {
                    xtype: 'device-zones-action-menu',
                    itemId: 'device-zones-action-menu'
                }
            }

        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceZones.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} zones'),
                displayMoreMsg: Uni.I18n.translate('deviceZones.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} ones'),
                emptyMsg: Uni.I18n.translate('deviceZones.pagingtoolbartop.emptyMsg', 'MDC', 'There are no zones to display'),
                items: [
                    {
                        xtype: 'button',
                        //privileges: Mdc.privileges.DeviceCommands.executeCommands,
                        text: Uni.I18n.translate('deviceZones.addCommand','MDC','Add zone'),
                        itemId: 'deviceAddZoneButton',
                        deviceId: me.deviceId,
                        //dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.allDeviceCommandPrivileges
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('deviceZones.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Zones per page')
            }
        ];

        me.callParent();
    }
});



