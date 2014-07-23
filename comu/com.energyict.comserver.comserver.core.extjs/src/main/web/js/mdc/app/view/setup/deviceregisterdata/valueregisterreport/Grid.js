Ext.define('Mdc.view.setup.deviceregisterdata.valueregisterreport.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.devicevalueregisterreportgrid',
    itemId: 'numerical-deviceRegisterReportGrid',
    store: 'NumericalRegisterData',

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
                header: Uni.I18n.translate('device.registerData.amount', 'MDC', 'Amount'),
                dataIndex: 'value',
                renderer: function (value, metaData, record) {
                    return value + ' ' + record.get('unitOfMeasure')
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('device.registerData.rawValue', 'MDC', 'Raw value'),
                dataIndex: 'rawValue',
                renderer: function (value, metaData, record) {
                    return value + ' ' + record.get('unitOfMeasure')
                },
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