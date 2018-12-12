/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.DeviceStatesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-states-grid',
    selModel: {
        mode: 'SINGLE'
    },
    width: '100%',
    maxHeight: 300,
    requires: [
        'Uni.grid.column.RemoveAction'
    ],

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
                xtype: 'uni-actioncolumn-remove',
                privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                handler: function (grid, rowIndex, colIndex, column, event, record) {
                    me.fireEvent('msgRemoveDeviceState', record);
                }
            }
        ];
        me.callParent();
    }
});
