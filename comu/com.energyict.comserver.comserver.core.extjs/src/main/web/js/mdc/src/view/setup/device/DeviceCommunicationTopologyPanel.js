/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceCommunicationTopologyPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationTopologyPanel',
    requires: [
        'Mdc.store.MasterDeviceCandidates'
    ],
    overflowY: 'auto',
    itemId: 'devicecommicationtopologypanel',
    deviceId: null,
    device: null,
    ui: 'tile',

    setRecord: function (device) {
        var me = this,
            slavesStore = device.slaveDevices(),
            slavesCount = slavesStore.getCount(),
            isGateway = device.get('isGateway'),
            isDirectlyAddressable = device.get('isDirectlyAddressed'),
            manageTopologyLink  = {
                xtype: 'container',
                margin: '0 0 4 7',
                html: Ext.String.format('<a href="{0}">{1}</a>',
                    me.router.getRoute('devices/device/topology').buildUrl(),
                    Uni.I18n.translate('deviceCommunicationTopology.manageLinkText', 'MDC', 'Manage communication topology')
                )
            },
            form = {
                xtype: 'form',
                itemId: 'deviceCommunicationTopologyForm',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    labelWidth: isGateway && device.get('gatewayType') === 'LAN' ? 200 : 100,
                    labelAlign: 'left'
                },
                items: []
            },
            grid = undefined;

        me.device = device;

        Ext.suspendLayouts();
        me.removeAll(true);
        if (!isDirectlyAddressable) {
            form.items.push({
                xtype: 'displayfield',
                labelAlign: 'left',
                fieldLabel: Uni.I18n.translate('deviceCommunicationTopology.master', 'MDC', 'Master'),
                margin: '0 0 10 7',
                renderer: function() {
                    var masterId = device.get('masterDeviceName');
                    if (masterId) {
                        return Ext.String.format(
                            '<a href="{0}">{1}</a>',
                            me.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(masterId)}),
                            Ext.String.htmlEncode(masterId)
                        );
                    } else {
                        return '-';
                    }
                }
            });
        }
        if (isGateway) {
            form.items.push({
                xtype: 'displayfield',
                labelAlign: 'left',
                fieldLabel: device.get('gatewayType') === 'LAN'
                    ? Uni.I18n.translate('deviceCommunicationTopology.MostRecentlyAddedSlaves', 'MDC', 'Most recently added slaves')
                    : Uni.I18n.translate('deviceCommunicationTopology.slaves', 'MDC', 'Slaves'),
                margin: slavesCount>0 ? '0 0 0 7' : '0 0 10 7',
                renderer: function() {
                    return slavesCount>0 ? '' : '-';
                }
            });

            if (slavesCount>0) {
                grid = {
                    xtype: 'gridpanel',
                    margin: '5 6 0 6',
                    itemId: 'communication-topology-grid',
                    viewConfig: {
                        disableSelection: true,
                        enableTextSelection: true
                    },
                    columns: [
                        {
                            header: Uni.I18n.translate('deviceCommunicationTopology.name', 'MDC', 'Name'),
                            dataIndex: 'name',
                            flex: 1,
                            renderer: function (value, meta, record) {
                                var href = me.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(record.get('name'))});
                                return Ext.String.format('<a href="{0}">{1}</a>', href, Ext.String.htmlEncode(value));
                            }
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
                                return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                            }
                        }
                    ]
                };
            }
        }
        me.add(form);
        if (!Ext.isEmpty(grid)) {
            me.add(grid);
            me.down('#communication-topology-grid').reconfigure(slavesStore);
        }
        me.add(manageTopologyLink);
        Ext.resumeLayouts();
    }
});