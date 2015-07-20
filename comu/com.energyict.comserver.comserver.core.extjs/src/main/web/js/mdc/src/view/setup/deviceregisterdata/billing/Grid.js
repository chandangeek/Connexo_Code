Ext.define('Mdc.view.setup.deviceregisterdata.billing.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-billing',
    itemId: 'deviceregisterreportgrid',
    store: 'BillingRegisterData',
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
                header: Uni.I18n.translate('device.registerData.interval', 'MDC', 'Interval'),
                dataIndex: 'interval',
                renderer: function (value) {
                    if (!Ext.isEmpty(value)) {
                        var startDate = new Date(value.start),
                            endDate = new Date(value.end);
                        return Uni.DateTime.formatDateShort(startDate)
                            + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                            + Uni.DateTime.formatTimeShort(startDate)
                            + ' - '
                            + Uni.DateTime.formatDateShort(endDate)
                            + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                            + Uni.DateTime.formatTimeShort(endDate);
                    }
                },
                flex: 2
            },
            {
                xtype: 'validation-flag-column',
                dataIndex: 'value',
                align: 'right',
                minWidth: 150,
                flex: 1
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
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.Device.administrateDeviceData,
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions,
                menu: {
                    xtype: 'deviceregisterdataactionmenu'
                }
            }
        ];

        me.callParent(arguments);
    }
});