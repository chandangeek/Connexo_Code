Ext.define('Mdc.view.setup.deviceregisterdata.text.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-text',
    itemId: 'deviceregisterreportgrid',
    store: 'TextRegisterData',

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
                header: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                dataIndex: 'value',
                renderer: function (value, metaData, record) {
                    if (!Ext.isEmpty(value) && Ext.isString(value)) {
                        var val = Ext.String.htmlEncode(value);
                        return val.substr(0, 300);
                    }
                },
                flex: 3
            },
            {
                xtype: 'edited-column',
                dataIndex: 'modificationState',
                header: '',
                width: 30,
                emptyText: ' '
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