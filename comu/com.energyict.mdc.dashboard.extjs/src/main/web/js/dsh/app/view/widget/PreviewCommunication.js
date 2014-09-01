Ext.define('Dsh.view.widget.PreviewCommunication', {
    extend: 'Ext.form.Panel',
    alias: 'widget.preview_communication',
    title: '',
    frame: true,
    layout: {
        type: 'column'
    },
    tools: [
//        {
//            xtype: 'button',
//            text: Uni.I18n.translate('general.actions', 'ISE', 'Actions'),
//            iconCls: 'x-uni-action-iconD',
//            menu: {
//                xtype: 'dsh-action-menu'
//            }
//        }
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
                    fieldLabel: Uni.I18n.translate('communication.widget.details.commTask', 'DSH', 'Name'),
                    name: 'name'
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.commTask', 'DSH', 'Communication task(s)'),
                    name: 'title'
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.device', 'DSH', 'Device'),
                    name: 'device',
                    renderer: function (val) {
                        return '<a href="#/devices/' + val.id + '">' + val.name + '</a>'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.deviceType', 'DSH', 'Device type'),
                    name: 'deviceType',
                    renderer: function (val) {
                        return '<a href="#/administration/devicetypes/' + val.id + '">' + val.name + '</a>'
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
                        return res
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.schedule', 'DSH', 'Frequency'),
                    name: 'comScheduleName'
                },
                {
                    fieldLabel: ' ',
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
                        return val ? 'Yes' : 'No'
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
                        return val.displayValue
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.latestResult', 'DSH', 'Latest result'),
                    name: 'latestResult'
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Started on'),
                    name: 'startTime',
                    renderer: function (val) {
                        return Ext.Date.format(val, 'm/d/Y h:i:s');
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.finishedOn', 'DSH', 'Finished successful on'),
                    name: 'successfulFinishTime',
                    renderer: function (val) {
                        return Ext.Date.format(val, 'm/d/Y h:i:s');
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('communication.widget.details.nextComm', 'DSH', 'Next communication'),
                    name: 'nextCommunication',
                    renderer: function (val) {
                        return Ext.Date.format(val, 'm/d/Y h:i:s');
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