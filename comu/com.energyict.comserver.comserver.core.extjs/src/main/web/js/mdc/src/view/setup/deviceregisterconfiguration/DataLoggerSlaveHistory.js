Ext.define('Mdc.view.setup.deviceregisterconfiguration.DataLoggerSlaveHistory', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.dataLogger-slaveRegisterHistory',
    itemId: 'mdc-dataLogger-slaveRegisterHistory',

    requires: [
        'Mdc.store.DataLoggerSlaveRegisterHistory'
    ],

    fieldLabel: Uni.I18n.translate('dataLoggerSlaveHistory.title', 'MDC', 'Data logger slave history'),

    labelAlign: 'top',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'grid',
                itemId: 'mdc-dataLoggerSlaveRegisterHistory-grid',
                store: 'Mdc.store.DataLoggerSlaveRegisterHistory',
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
                        dataIndex: 'mrid',
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

        me.callParent(arguments);
    }
});