/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.billing.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-registers-history-billing',
    store: 'Mdc.store.BillingRegisterHistoryData',
    requires: [
        'Uni.grid.column.ValidationFlag'
    ],
    maxHeight: 450,
    useMultiplier: false,

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
                header: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                dataIndex: 'interval',
                renderer: function (value) {
                    if (!Ext.isEmpty(value)) {
                        var startDate = new Date(value.start),
                            endDate = new Date(value.end);
                        return Uni.DateTime.formatDateTimeShort(startDate) + ' - ' + Uni.DateTime.formatDateTimeShort(endDate);
                    }
                    return '-';
                },
                flex: 2
            },
            {
                xtype: 'validation-flag-column',
                dataIndex: me.useMultiplier ? 'calculatedValue' : 'value',
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
                header: Uni.I18n.translate('historyGrid.changedBy', 'MDC', 'Changed by'),
                dataIndex: 'userName',
                flex: 1
            }
        ];

        me.callParent(arguments);
    }
});