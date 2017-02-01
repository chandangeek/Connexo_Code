/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DataLoggerSlavesPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataLoggerSlavesPanel',
    requires: [
        'Mdc.store.MasterDeviceCandidates',
        'Uni.util.FormEmptyMessage'
    ],
    overflowY: 'auto',
    itemId: 'mdc-dataLoggerSlavesPanel',
    device: null,
    ui: 'tile',
    title: Uni.I18n.translate('device.dataLoggerSlaves.title', 'MDC', 'Data logger slaves'),

    setSlaveStore: function(slaveStore) {
        var me = this,
            slavesCount = slaveStore.getCount(),
            label = {
                xtype: 'displayfield',
                labelAlign: 'left',
                labelWidth: 200,
                fieldLabel: Uni.I18n.translate('deviceCommunicationTopology.MostRecentlyAddedSlaves', 'MDC', 'Most recently added slaves'),
                margin: slavesCount>0 ? '0 0 0 7' : '0 0 10 7',
                renderer: function() {
                    return slavesCount>0 ? '' : '-';
                }
            },
            grid = {
                xtype: 'gridpanel',
                margin: '5 6 0 6',
                itemId: 'mdc-recent-slaves-grid',
                viewConfig: {
                    disableSelection: true,
                    enableTextSelection: true
                },
                store: slaveStore,
                columns: [
                    {
                        header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        dataIndex: 'name',
                        flex: 1,
                        renderer: function (value, meta, record) {
                            var href = me.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(record.get('name'))});
                            return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>';
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
                            return Ext.isEmpty(value) || value===0 ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                        }
                    }
                ]
            },
            manageSlavesLink = {
                xtype: 'container',
                margin: '0 0 4 7',
                html: '<a href="' + me.router.getRoute('devices/device/dataloggerslaves').buildUrl() + '">' + Uni.I18n.translate('general.manageDataLoggerSlaves', 'MDC', 'Manage data logger slaves') + '</a>'
            };

        me.removeAll();
        if (slavesCount) {
            me.add(label, grid, manageSlavesLink);
        } else {
            me.add(label, manageSlavesLink);
        }
    }
});
