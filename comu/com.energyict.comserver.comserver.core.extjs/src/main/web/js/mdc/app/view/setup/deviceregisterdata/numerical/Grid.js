Ext.define('Mdc.view.setup.deviceregisterdata.numerical.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-numerical',
    itemId: 'deviceregisterreportgrid',
    store: 'NumericalRegisterData',

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
                header: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                dataIndex: 'value',
                renderer: function (value, metaData, record) {
                    switch (record.get('validationResult')) {
                        case 'validationStatus.notValidated':
                            return '<span style="vertical-align: middle"><img style="height: 13px" src="../mdc/resources/images/Not-validated.png"/>&nbsp;&nbsp;&nbsp;'
                                + value + ' ' + record.get('unitOfMeasure') + '</span>';
                            break;
                        case 'validationStatus.ok':
                            return value + ' ' + record.get('unitOfMeasure');
                            break;
                        case 'validationStatus.suspect':
                            return '<span style="vertical-align: middle"><img style="height: 13px" src="../mdc/resources/images/Suspect.png"/>&nbsp;&nbsp;&nbsp;'
                                + value + ' ' + record.get('unitOfMeasure') + '</span>';
                            break;
                        default:
                            return value + ' ' + record.get('unitOfMeasure');
                            break;
                    }
                },
                flex: 3
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