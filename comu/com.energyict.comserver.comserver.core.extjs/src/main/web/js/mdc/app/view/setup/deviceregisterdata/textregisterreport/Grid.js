Ext.define('Mdc.view.setup.deviceregisterdata.textregisterreport.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.devicetextregisterreportgrid',
    itemId: 'text-deviceRegisterReportGrid',
    store: 'TextRegisterData',

    columns: {
        items: [
            {
                header: Uni.I18n.translate('device.registerData.readingTime', 'MDC', 'Reading time'),
                dataIndex: 'timeStamp',
                xtype: 'datecolumn',
                format: 'M j, Y \\a\\t G:i',
                defaultRenderer: function (value) {
                    return Ext.util.Format.date(value, this.format);
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                dataIndex: 'value',
                flex: 1
            },
            {
                header: Uni.I18n.translate('device.registerData.validationStatus', 'MDC', 'Validation status'),
                dataIndex: 'validationStatus',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn'
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});