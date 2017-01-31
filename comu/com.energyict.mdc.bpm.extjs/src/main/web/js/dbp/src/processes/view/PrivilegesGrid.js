/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.PrivilegesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.privileges-grid',
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
                header: Uni.I18n.translate('editProcess.privilege', 'DBP', 'Privilege'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('editProcess.userRoles', 'DBP', 'User roles'),
                dataIndex: 'userRoles',
                renderer: function (value) {
                    var resultArray = [];
                    Ext.Array.each(value, function (userRole) {
                        resultArray.push(Ext.String.htmlEncode(userRole.name));
                    });
                    return resultArray.join('<br>');
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn-remove',
                privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                handler: function (grid, rowIndex, colIndex, column, event, record) {
                    me.fireEvent('msgRemovePrivilege', record);
                }
            }
        ];
        me.callParent();
    }
});
