Ext.define('Mdc.view.setup.deviceregisterdata.numerical.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-numerical',
    itemId: 'deviceregisterreportgrid',
    store: 'NumericalRegisterData',
    requires: [
        'Uni.grid.column.ValidationFlag'
    ],

    columns: {
        items: [
            {
                header: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                dataIndex: 'timeStamp',
                xtype: 'datecolumn',
                format: 'M j, Y \\a\\t G:i',
                defaultRenderer: function (value) {
                    return Ext.util.Format.date(value, this.format);
                },
                width: 200
            },
            {
                dataIndex: 'value',
                xtype: 'validation-flag-column',
                align: 'right',
                header: '',
                width: 30
            },
            {
                dataIndex: 'value',
                header: 'Value',
                flex: 1,
                align: 'right',
                renderer: function (value, metaData, record) {
                        return value + ' ' + record.get('unitOfMeasure') + '</span>';
                }

            },
            {
                align: 'right',
                header: '',
                flex: 6
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