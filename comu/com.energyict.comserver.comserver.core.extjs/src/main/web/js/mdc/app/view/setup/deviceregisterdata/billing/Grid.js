Ext.define('Mdc.view.setup.deviceregisterdata.billing.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-billing',
    itemId: 'deviceregisterreportgrid',
    store: 'BillingRegisterData',
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
                flex: 1
            },
            {
                header: Uni.I18n.translate('device.registerData.interval', 'MDC', 'Interval'),
                dataIndex: 'interval',
                renderer: function (value) {
                    if (value) {
                        var startDate = new Date(value.start),
                            endDate = new Date(value.end),
                            format = 'M j, Y \\a\\t G:i';
                        return Ext.util.Format.date(startDate, format) + ' - ' + Ext.util.Format.date(endDate, format);
                    }
                },
                flex: 2
            },
            {
                dataIndex: 'value',
                xtype: 'validation-flag-column',
                flex: 2
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