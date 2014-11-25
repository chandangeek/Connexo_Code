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
                                    result = result + '<li>'+ item.name+'</li>'
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
                            Uni.Auth.hasAnyPrivilege(['privilege.administrate.device','privilege.view.device'])
                                ? res = '<a href="#/devices/' + val.id + '">' + val.name + '</a>' : res = val.name;
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
                            Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'])
                                ? res = '<a href="#/administration/devicetypes/' + val.id + '">' + val.name + '</a>' : res = val.name;
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
                            val.config.name +
                            '</a>');
                        if (res !== '' && !Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'])) {
                            res = val.config.name;
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
                        return val.displayValue ? val.displayValue : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.latestResult', 'DSH', 'Latest result'),
                    name: 'latestResult',
                    renderer: function (val) {
                        return val.displayValue ? val.displayValue : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Started on'),
                    name: 'startTime',
                    renderer: function (val) {
                        return Ext.Date.format(val, 'd/m/Y h:i:s');
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.finishedOn', 'DSH', 'Finished successfully on'),
                    name: 'successfulFinishTime',
                    renderer: function (val) {
                        return Ext.Date.format(val, 'd/m/Y h:i:s');
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.nextComm', 'DSH', 'Next communication'),
                    name: 'nextCommunication',
                    renderer: function (val) {
                        return Ext.Date.format(val, 'd/m/Y h:i:s');
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