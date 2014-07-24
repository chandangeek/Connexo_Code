Ext.define('Mdc.view.setup.deviceregisterdata.eventregisterreport.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceeventregisterreportgrid',
    itemId: 'event-deviceRegisterReportGrid',
    store: 'EventRegisterData',

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