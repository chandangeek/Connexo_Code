Ext.define('Mdc.view.setup.deviceregisterdata.billing.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-billing',
    itemId: 'deviceregisterreportgrid',
    store: 'BillingRegisterData',
    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Edited',
        'Uni.grid.column.ValidationFlag'
    ],
    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                dataIndex: 'timeStamp',
                renderer: me.renderMeasurementTime,
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
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
                isDisabled: function(grid, rowIndex, colIndex, clickedItem, record) {
                    return !Ext.isEmpty(record.get('slaveRegister'));
                }
            }
        ];

        me.callParent(arguments);
    }
});