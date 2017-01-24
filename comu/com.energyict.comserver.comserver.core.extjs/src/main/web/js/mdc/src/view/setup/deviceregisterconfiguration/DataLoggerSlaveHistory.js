Ext.define('Mdc.view.setup.deviceregisterconfiguration.DataLoggerSlaveHistory', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.dataLogger-slaveRegisterHistory',
    itemId: 'mdc-dataLogger-slaveRegisterHistory',

    requires: [
        'Uni.util.FormEmptyMessage',
        'Mdc.store.DataLoggerSlaveRegisterHistory'
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
                    itemId: 'mdc-dataLoggerSlaveRegisterHistory-grid',
                    store: me.dataLoggerSlaveHistoryStore,
                    maxHeight: 450,
                    viewConfig: {
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
                            renderer: function(value, meta, record) {
                                if (Ext.isEmpty(value)) {
                                    return '-';
                                }
                                var url = '#/devices/' + encodeURIComponent(value) + '/registers/' + record.get('registerId');
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