/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.DataLoggerSlaveHistory', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.dataLogger-slaveChannelHistory',
    itemId: 'mdc-dataLogger-slaveChannelHistory',

    requires: [
        'Uni.util.FormEmptyMessage',
        'Mdc.store.DataLoggerSlaveChannelHistory'
    ],

    fieldLabel: Uni.I18n.translate('dataLoggerSlaveHistory.title', 'MDC', 'Data logger slave history'),

    labelAlign: 'top',

    dataLoggerSlaveHistoryStore: null,

    initComponent: function () {
        var me = this;

        if (me.dataLoggerSlaveHistoryStore.getTotalCount() === 0) {
            me.items = [
                {
                    xtype: 'form',
                    items: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('dataLoggerSlaveHistory.empty', 'MDC', 'No data logger slave history available.')
                    }
                }
            ];
        } else {
            me.items = [
                {
                    xtype: 'grid',
                    itemId: 'mdc-dataLoggerSlaveChannelHistory-grid',
                    store: 'Mdc.store.DataLoggerSlaveChannelHistory',
                    maxHeight: 450,
                    viewConfig: {
                        loadMask: false,
                        disableSelection: true,
                        enableTextSelection: true
                    },
                    columns: [
                        {
                            header: Uni.I18n.translate('general.period', 'MDC', 'Period'),
                            dataIndex: 'periodName',
                            flex: 1
                        },
                        {
                            header: Uni.I18n.translate('general.dataLoggerSlave', 'MDC', 'Data logger slave'),
                            dataIndex: 'deviceName',
                            flex: 1,
                            renderer: function (value, meta, record) {
                                if (Ext.isEmpty(value)) {
                                    return '-';
                                }
                                var url = '#/devices/' + encodeURIComponent(value) + '/channels/' + record.get('channelId');
                                return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                            }
                        }
                    ]
                }
            ];
        }

        me.callParent(arguments);
    }
});
