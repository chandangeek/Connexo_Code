Ext.define('Mdc.view.setup.deviceregisterdata.numerical.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-numerical',
    itemId: 'deviceregisterreportgrid',
    store: 'NumericalRegisterData',
    requires: [
        'Uni.grid.column.ValidationFlag'
    ],
    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                dataIndex: 'timeStamp',
                renderer: function (value) {
                    return value
                        ? Uni.DateTime.formatDateShort(new Date(value))
                        + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                        + Uni.DateTime.formatTimeShort(new Date(value))
                        : '';
                },
                flex: 1
            },
            {
                xtype: 'validation-flag-column',
                dataIndex: 'value',
                align: 'right',
                minWidth: 150,
                flex: 1,
                renderer: function (data, metaData, record) {
                    if (record.data.validationStatus) {
                        var result = record.data.validationResult,
                            status = result.split('.')[1],
                            cls = 'icon-validation-cell';

                        if (status === 'suspect') {
                            cls +=  ' icon-validation-red'
                        }
                        if (status === 'notValidated') {
                            cls +=  ' icon-validation-black'
                        }
                        metaData.tdCls = cls;
                    }
                    if (!Ext.isEmpty(data)) {
                        return record.get('isConfirmed') ? Uni.Number.formatNumber(data, -1) + '<span style="margin: 0 0 0 10px; position: absolute" class="icon-checkmark3"</span>' : Uni.Number.formatNumber(data, -1);
                    }
                }
            },
            {
                xtype: 'edited-column',
                dataIndex: 'modificationState',
                header: '',
                width: 30
            },
            {
                xtype: 'validation-flag-column',
                dataIndex: 'deltaValue',
                align: 'right',
                minWidth: 150,
                hidden: true,
                flex: 1,
                renderer: function (data, metaData, record) {
                    if (record.data.validationStatus) {
                        var result = record.data.validationResult,
                            status = result.split('.')[1],
                            cls = 'icon-validation-cell';
                        if (status === 'suspect') {
                            cls +=  ' icon-validation-red'
                        }
                        if (status === 'notValidated') {
                            cls +=  ' icon-validation-black'
                        }
                        metaData.tdCls = cls;
                    }
                    if (!Ext.isEmpty(data)) {
                        return record.get('isConfirmed') ? Uni.Number.formatNumber(data, -1) + '<span style="margin: 0 0 0 10px; position: absolute" class="icon-checkmark3"</span>' : Uni.Number.formatNumber(data, -1);
                    }
                }
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.Device.administrateDeviceData,
                menu: {
                    xtype: 'deviceregisterdataactionmenu'
                }
            }
        ];

        me.callParent(arguments);
    }
});