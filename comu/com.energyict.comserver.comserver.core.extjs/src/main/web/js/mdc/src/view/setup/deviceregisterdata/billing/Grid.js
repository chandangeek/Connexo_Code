Ext.define('Mdc.view.setup.deviceregisterdata.billing.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-billing',
    itemId: 'deviceregisterreportgrid',
    store: 'BillingRegisterData',

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
                header: Uni.I18n.translate('device.registerData.interval', 'MDC', 'Interval'),
                dataIndex: 'interval',
                renderer: function (value) {
                    if (!Ext.isEmpty(value)) {
                        var startDate = new Date(value.start),
                            endDate = new Date(value.end),
                            format = 'M j, Y \\a\\t G:i';
                        return Ext.util.Format.date(startDate, format) + ' - ' + Ext.util.Format.date(endDate, format);
                    }
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                dataIndex: 'value',
                align: 'right',
                minWidth: 150,
                flex: 1,
                renderer: function (data, metaData, record) {
                    var validationFlag = '';
                    switch (record.get('validationResult')) {
                        case 'validationStatus.notValidated':
                            validationFlag = '<span class="icon-validation icon-validation-black"></span>';
                            break;
                        case 'validationStatus.ok':
                            validationFlag = '&nbsp;&nbsp;&nbsp;&nbsp;';
                            break;
                        case 'validationStatus.suspect':
                            validationFlag = '<span class="icon-validation icon-validation-red"></span>';
                            break;
                        default:
                            validationFlag = '&nbsp;&nbsp;&nbsp;&nbsp;';
                            break;
                    }
                    return !Ext.isEmpty(data)
                        ? '<span class="validation-column-align">' + data + ' ' + record.get('unitOfMeasure') + ' ' + validationFlag + '</span>'
                        : '<span class="icon-validation icon-validation-black"></span>';
                }
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