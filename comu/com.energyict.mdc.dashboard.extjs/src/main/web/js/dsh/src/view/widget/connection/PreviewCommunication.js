Ext.define('Dsh.view.widget.connection.PreviewCommunication', {
    extend: 'Ext.form.Panel',
    alias: 'widget.preview-connection-communication',
    title: '',
    frame: true,
    layout: {
        type: 'column'
    },
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'DSH', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            itemId: 'communicationPreviewActionMenu',
            menu: {
//                xtype: 'communications-action-menu'
            }
        }
    ],
    items: [
        {
            columnWidth: 0.5,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.name', 'DSH', 'Name'),
                    name: 'comTask',
                    renderer: function (val) {
                        return Ext.String.htmlEncode(val.name);
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.device', 'DSH', 'Device'),
                    name: 'device',
                    renderer: function (val) {
                        var res = '';
                        if (val) {
                            Mdc.privileges.Device.canViewOrAdministrateDeviceData()
                                ? res = '<a href="#/devices/' + val.id + '">' + Ext.String.htmlEncode(val.name) + '</a>' : res = Ext.String.htmlEncode(val.name);
                        }
                        return res;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.deviceType', 'DSH', 'Device type'),
                    name: 'deviceType',
                    renderer: function (val) {
                        var res = '';
                        if (val) {
                            Mdc.privileges.DeviceType.canView()
                                ? res = '<a href="#/administration/devicetypes/' + val.id + '">' + Ext.String.htmlEncode(val.name) + '</a>' : res = Ext.String.htmlEncode(val.name);
                        }
                        return res;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.deviceConfig', 'DSH', 'Device configuration'),
                    name: 'devConfig',
                    renderer: function (val) {
                        var res = '';
                        val && (res = '<a href="#/administration/devicetypes/' +
                        val.devType.id + '/deviceconfigurations/' +
                        val.config.id +
                        '">' +
                        Ext.String.htmlEncode(val.config.name) +
                        '</a>');
                        if (res !== '' && !Mdc.privileges.DeviceType.canView()) {
                            res = Ext.String.htmlEncode(val.config.name);
                        }
                        return res
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.frequency', 'DSH', 'Frequency'),
                    name: 'comScheduleFrequency',
                    renderer: function (val) {
                        var res = '';
                        if (val) {
                            res = Uni.I18n.translate('general.every', 'DSH', 'Every')
                            + ' '
                            + val.every.count
                            + ' '
                            + val.every.timeUnit;
                        }
                        return res
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.urgency', 'DSH', 'Urgency'),
                    name: 'urgency'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.alwaysExecuteOnInbound', 'DSH', 'Always execute on inbound'),
                    name: 'alwaysExecuteOnInbound',
                    renderer: function (val) {
                        if (!_.isUndefined(val)) {
                            return val ? 'Yes' : 'No'
                        } else {
                            return ''
                        }
                    }
                }
            ]
        },
        {
            columnWidth: 0.5,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.lastResult', 'DSH', 'Last result'),
                    name: 'result',
                    renderer: function (val) {
                        return val.displayValue ? Ext.String.htmlEncode(val.displayValue) : '-';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'DSH', 'Status'),
                    name: 'currentState',
                    renderer: function (val) {
                        return val.displayValue ? Ext.String.htmlEncode(val.displayValue) : '-';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.startedOn', 'DSH', 'Started on'),
                    name: 'startTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.finishedOn', 'DSH', 'Finished on'),
                    name: 'stopTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.nextCommunication', 'DSH', 'Next communication'),
                    name: 'nextCommunication',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    }
});