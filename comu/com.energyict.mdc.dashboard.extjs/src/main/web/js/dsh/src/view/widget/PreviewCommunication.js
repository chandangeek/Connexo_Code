Ext.define('Dsh.view.widget.PreviewCommunication', {
    extend: 'Ext.form.Panel',
    alias: 'widget.preview_communication',
    title: '',
    frame: true,
    layout: {
        type: 'column'
    },
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'ISE', 'Actions'),
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
                    fieldLabel: Uni.I18n.translate('communication.widget.details.commTaskName', 'DSH', 'Name'),
                    name: 'name'
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.commTasks', 'DSH', 'Communication task(s)'),
                    name: 'comTasks',
                        renderer: function(value){
                            if(value!==''){
                                var result = '';
                                Ext.each(value, function(item){
                                    result = result + '<li>'+ Ext.String.htmlEncode(item.name)+'</li>'
                                });
                                return result;
                            } else {
                                return '';
                            }
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.device', 'DSH', 'Device'),
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
                    fieldLabel: Uni.I18n.translate('communication.widget.details.deviceType', 'DSH', 'Device type'),
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
                    fieldLabel: Uni.I18n.translate('communication.widget.details.deviceConfig', 'DSH', 'Device configuration'),
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
                    fieldLabel: Uni.I18n.translate('communication.widget.details.frequency', 'DSH', 'Frequency'),
                    name: 'comScheduleFrequency',
                    renderer: function (val) {
                        var res = '';
                        if (val) {
                            res = Uni.I18n.translate('communication.widget.details.every', 'DSH', 'Every')
                                + ' '
                                + val.every.count
                                + ' '
                                + val.every.timeUnit;
                        }
                        return res
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.urgency', 'DSH', 'Urgency'),
                    name: 'urgency'
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.executeOnInbound', 'DSH', 'Always execute on inbound'),
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
                    fieldLabel: Uni.I18n.translate('communication.widget.details.currentState', 'DSH', 'Current state'),
                    name: 'currentState',
                    renderer: function (val) {
                        return val.displayValue ? Ext.String.htmlEncode(val.displayValue) : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.latestResult', 'DSH', 'Latest result'),
                    name: 'latestResult',
                    renderer: function (val) {
                        return val.displayValue ? Ext.String.htmlEncode(val.displayValue) : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Started on'),
                    name: 'startTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.finishedOn', 'DSH', 'Finished successfully on'),
                    name: 'successfulFinishTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.nextComm', 'DSH', 'Next communication'),
                    name: 'nextCommunication',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '';
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