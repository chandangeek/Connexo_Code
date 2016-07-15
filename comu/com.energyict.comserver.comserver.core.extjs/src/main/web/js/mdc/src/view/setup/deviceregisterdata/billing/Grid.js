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
                        ? Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [ Uni.DateTime.formatDateLong(new Date(value)),Uni.DateTime.formatTimeLong(new Date(value))])
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
                        return  Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(startDate),Uni.DateTime.formatTimeShort(startDate)])
                            + ' - '
                            + Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(endDate),Uni.DateTime.formatTimeShort(endDate)])
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
                width: 30,
                emptyText: ' '
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
                },
                renderer: function(value, metaData, record) {
                    this.disabled = !Ext.isEmpty(record.get('slaveRegister'))
                }
            }
        ];

        me.callParent(arguments);
    }
});