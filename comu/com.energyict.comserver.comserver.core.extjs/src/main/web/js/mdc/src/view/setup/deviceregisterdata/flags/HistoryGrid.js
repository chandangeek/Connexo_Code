/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.flags.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-registers-history--flags',
    store: 'Mdc.store.FlagsRegisterHistoryData',
    requires: [
        'Uni.grid.column.Edited'
    ],
    maxHeight: 450,
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
                header: Uni.I18n.translate('device.registerData.changedOn', 'MDC', 'Changed on'),
                dataIndex: 'reportedDateTime',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
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
                header: Uni.I18n.translate('historyGrid.changedBy', 'MDC', 'Changed by'),
                dataIndex: 'userName',
                flex: 1
            }
        ];

        me.callParent(arguments);
    }
});