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
    title: Uni.I18n.translate('device.dataLoggerSlaves.title', 'MDC', 'Data logger slaves: most recently added'),

    setSlaveStore: function(slaveStore) {
        var me = this,
            slavesCount = slaveStore.getCount(),
            grid = {
                xtype: 'gridpanel',
                margin: '5 0 0 0',
                itemId: 'mdc-recent-slaves-grid',
                viewConfig: {
                    disableSelection: true,
                    enableTextSelection: true
                },
                store: slaveStore,
                columns: [
                    {
                        header: Uni.I18n.translate('deviceCommunicationTopology.mRID', 'MDC', 'MRID'),
                        dataIndex: 'mRID',
                        flex: 1,
                        renderer: function (value, meta, record) {
                            var href = me.router.getRoute('devices/device').buildUrl({mRID: record.get('mRID')});
                            return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
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
            showAllSlavesLink = {
                xtype: 'container',
                html: '<a href="' + me.router.getRoute('devices/device/dataloggerslaves').buildUrl({mRID: me.router.arguments.mRID}) + '">' + Uni.I18n.translate('general.showAllDataLoggerSlaves', 'MDC', 'Show all data logger slaves') + '</a>'
            };

        me.removeAll();
        if (slavesCount) {
            me.add(grid, showAllSlavesLink);
        } else {
            me.add({
                xtype: 'form',
                items: {
                    xtype: 'uni-form-empty-message',
                    text: Uni.I18n.translate('general.dataLogger.noSlaves', 'MDC', 'This data logger has no data logger slaves.')
                }
            });
        }
    }
});
