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
                flex: 1
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'modificationState',
                width: 30
            },
            {
                dataIndex: 'deltaValue',
                align: 'right',
                minWidth: 150,
                hidden: true,
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceData'),
                menu: {
                    xtype: 'deviceregisterdataactionmenu'
                }
            }
        ];

        me.callParent(arguments);
    }
});