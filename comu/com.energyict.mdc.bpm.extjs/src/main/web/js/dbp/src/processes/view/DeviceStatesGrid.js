Ext.define('Dbp.processes.view.DeviceStatesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-states-grid',
    selModel: {
        mode: 'SINGLE'
   },
    width: '100%',
    maxHeight: 300,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('editProcess.deviceLifeCycle', 'DBP', 'Device life cycle'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('editProcess.deviceState', 'DBP', 'Device state'),
                dataIndex: 'deviceState',
                flex: 1
            },
            {
                xtype: 'actioncolumn',
                header: Uni.I18n.translate('editProcess.actions', 'DBP', 'Actions'),
                align: 'right',
                privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                items: [
                    {
                        iconCls: 'uni-icon-delete',
                        itemId: 'btn-remove-device-state',
                        tooltip: Uni.I18n.translate('editProcess.remove', 'DBP', 'Remove'),
                        handler: function (grid, rowIndex, colIndex, column, event, record) {
                            me.fireEvent('msgRemoveDeviceState', record);
                        }
                    }
                ]
            }
        ];
        me.callParent();
    }
});
