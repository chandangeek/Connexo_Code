Ext.define('Mdc.view.setup.deviceregisterdata.numerical.Grid', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainGrid',
    alias: 'widget.deviceregisterreportgrid-numerical',
    itemId: 'deviceregisterreportgrid',
    store: 'NumericalRegisterData',
    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Edited',
        'Uni.grid.column.ValidationFlag'
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
                header: Uni.I18n.translate('general.measurementPeriod', 'MDC', 'Measurement period'),
                dataIndex: 'interval',
                renderer: function (value) {
                    if(!Ext.isEmpty(value)) {
                        var endDate = new Date(value.end);
                        if (!!value.start && !!value.end) {
                            var startDate = new Date(value.start);
                            return Uni.DateTime.formatDateTimeShort(startDate) + ' - ' + Uni.DateTime.formatDateTimeShort(endDate);
                        } else {
                            return Uni.DateTime.formatDateTimeShort(endDate);
                        }
                    }
                    return '-';
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('device.registerData.eventTime', 'MDC', 'Event time'),
                dataIndex: 'eventDate',
                itemId: 'eventTime',
                renderer: me.renderMeasurementTime,
                flex: 1
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
                    return '-';
                }
            },
            {
                header: Uni.I18n.translate('device.registerData.reportedTime', 'MDC', 'Last updated'),
                dataIndex: 'reportedDateTime',
                flex: 1,
                renderer: function(value){
                    var date = new Date(value);
                    return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(date), Uni.DateTime.formatTimeShort(date)]);
                }
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