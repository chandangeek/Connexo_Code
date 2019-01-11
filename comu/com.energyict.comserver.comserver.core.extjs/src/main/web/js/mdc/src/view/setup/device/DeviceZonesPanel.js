/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceZonesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-zones-panel',
    requires: [
        'Mdc.store.DeviceZones'
    ],

    overflowY: 'auto',
    itemId: 'deviceZonesPanel',
    deviceId: null,
    device: null,
    ui: 'tile',

    setRecord: function (device) {
        var me = this,
            zonesStore = device.zones(),
            zonesCount = zonesStore.getCount(),

            manageZonesLink = {
                xtype: 'container',
                margin: '0 0 4 7',
                html: Ext.String.format('<a href="{0}">{1}</a>',
                    me.router.getRoute('devices/device/zones').buildUrl(),
                    Uni.I18n.translate('deviceZones.manageLinkText', 'MDC', 'Manage zones')
                )
            },
            form = {
                xtype: 'form',
                itemId: 'deviceZonesForm',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    labelWidth: 100,
                    labelAlign: 'left'
                },
                items: []
            },
            grid = undefined;

        me.device = device;

        Ext.suspendLayouts();
        me.removeAll(true);


        if (zonesCount > 0) {
            grid = {
                xtype: 'gridpanel',
                margin: '5 6 0 6',
                itemId: 'zones-grid',
                viewConfig: {
                    disableSelection: true,
                    enableTextSelection: true
                },
                columns: [
                    {
                        itemId: 'deviceZoneTypeName',
                        header: Uni.I18n.translate('deviceZones.zoneType', 'MDC', 'Zone type'),
                        dataIndex: 'zoneTypeName',
                        flex: 2,
                        valueField: 'zoneTypeId',
                        displayField: 'zoneTypeName'

                    },
                    {
                        itemId: 'deviceZoneName',
                        header: Uni.I18n.translate('deviceZones.zone', 'MDC', 'Zone'),
                        dataIndex: 'zoneName',
                        flex: 1,
                        valueField: 'zoneId',
                        displayField: 'zoneName'
                    }
                ]
            };
        }

        me.add(form);
        if (!Ext.isEmpty(grid)) {
            me.add(grid);
            me.down('#zones-grid').reconfigure(zonesStore);
        }
        me.add(manageZonesLink);
        Ext.resumeLayouts();
    }
});
