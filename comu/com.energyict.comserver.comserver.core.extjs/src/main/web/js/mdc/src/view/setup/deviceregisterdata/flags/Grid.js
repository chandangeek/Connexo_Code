/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.flags.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-flags',
    itemId: 'deviceregisterreportgrid',
    store: 'FlagsRegisterData',
    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Edited'
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
                header: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                dataIndex: 'value',
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
                },
                isDisabled: function(grid, rowIndex, colIndex, clickedItem, record) {
                    return !Ext.isEmpty(record.get('slaveRegister'));
                }
            }
        ];

        me.callParent(arguments);
    }
});