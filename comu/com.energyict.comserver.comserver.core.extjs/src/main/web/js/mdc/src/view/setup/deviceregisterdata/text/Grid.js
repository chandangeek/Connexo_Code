Ext.define('Mdc.view.setup.deviceregisterdata.text.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-text',
    itemId: 'deviceregisterreportgrid',
    store: 'TextRegisterData',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                dataIndex: 'timeStamp',
                xtype: 'datecolumn',
                format: 'M j, Y \\a\\t G:i',
                defaultRenderer: function (value) {
                    if (!Ext.isEmpty(value)) {
                        return Ext.util.Format.date(new Date(value), this.format);
                    }
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                dataIndex: 'value',
                renderer: function (value, metaData, record) {
                    if (!Ext.isEmpty(value) && Ext.isString(value)) {
                        var val = new String(value);
                        return val.substr(0, 300);
                    }
                },
                flex: 3
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'editedDateTime'
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'deviceregisterdataactionmenu'
                }
            }
        ];

        me.callParent(arguments);
    }
});