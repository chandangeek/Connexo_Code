/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.numerical.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-registers-history-numerical',
    itemId: 'deviceregisterreportgrid',
    store: 'Mdc.store.NumericalRegisterHistoryData',
    requires: [
        'Uni.grid.column.Edited',
        'Uni.grid.column.ValidationFlag'
    ],
    maxHeight: 450,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                dataIndex: 'timeStamp',
                renderer: me.renderMeasurementTime,
                flex: 1,
                renderer1: function (value, metaData, record, rowIndex, colIndex, store) {
                    if (rowIndex >= 1 && store.getAt(rowIndex - 1).get('timeStamp') == store.getAt(rowIndex).get('timeStamp')) {
                        return;
                    }
                }
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
                xtype: 'validation-flag-column',
                dataIndex: 'value',
                align: 'right',
                minWidth: 150,
                flex: 1,
                renderer: function (data, metaData, record) {
                    if (!Ext.isEmpty(data)) {
                        var status = record.data.validationResult ? record.data.validationResult.split('.')[1] : 'unknown',
                            icon = '';
                        if (record.get('isConfirmed')) {
                            icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute;" data-qtip="'
                                + Uni.I18n.translate('reading.validationResult.confirmed', 'MDC', 'Confirmed') + '"></span>'
                        } else if (status === 'suspect') {
                            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:red;" data-qtip="'
                                + Uni.I18n.translate('general.suspect', 'MDC', 'Suspect') + '"></span>';
                        } else if (status === 'notValidated') {
                            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;" data-qtip="'
                                + Uni.I18n.translate('general.notValidated', 'MDC', 'Not validated') + '"></span>';
                        }
                        return Uni.Number.formatNumber(data, -1) + icon;
                    }
                }
            },
            {
                xtype: 'edited-column',
                dataIndex: 'modificationState',
                header: '',
                width: 30,
                emptyText: ' '
            },
            {
                dataIndex: 'calculatedValue',
                align: 'right',
                minWidth: 150,
                hidden: true,
                flex: 1,
                renderer: function (data, metaData, record) {
                    if (!Ext.isEmpty(data)) {
                        var status = record.data.validationResult ? record.data.validationResult.split('.')[1] : 'unknown',
                            icon = '';
                        if (record.get('isConfirmed')) {
                            icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute" data-qtip="'
                                + Uni.I18n.translate('reading.validationResult.confirmed', 'MDC', 'Confirmed') + '"></span>'
                        } else if (status === 'suspect') {
                            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:red;"></span>';
                        } else if (status === 'notValidated') {
                            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;"></span>';
                        }
                        return Uni.Number.formatNumber(data, -1) + icon;
                    }
                }
            },
            {
                xtype: 'edited-column',
                dataIndex: 'calculatedModificationState',
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
                flex: 1,
                renderer: function (data) {
                    if (!Ext.isEmpty(data)) {
                        return Uni.Number.formatNumber(data, -1);
                    }
                }
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