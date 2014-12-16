Ext.define('Mdc.view.setup.deviceregisterdata.flags.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-flags',
    itemId: 'deviceregisterreportgrid',
    store: 'FlagsRegisterData',

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
                header: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                dataIndex: 'value',
                flex: 3
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'modificationState',
                width: 30
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