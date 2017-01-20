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
                            icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute;"></span>'
                        } else if (status === 'suspect') {
                            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:red;" data-qtip="'
                                + Uni.I18n.translate('general.suspect', 'MDC', 'Suspect') + '"></span>';
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
                            icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute"></span>'
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