Ext.define('Dsh.view.widget.PreviewConnection', {
    extend: 'Ext.form.Panel',
    alias: 'widget.preview_connection',
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
            menu: {
                xtype: 'dsh-action-menu'
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
                    fieldLabel: Uni.I18n.translate('connection.widget.details.device', 'DSH', 'Device'),
                    name: 'device',
                    renderer: function (val) {
                        return '<a href="#/devices/' + val.id + '">' + val.name + '</a>'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.deviceType', 'DSH', 'Device type'),
                    name: 'deviceType',
                    renderer: function (val) {
                        return '<a href="#/administration/devicetypes/' + val.id + '">' + val.name + '</a>'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.deviceConfig', 'DSH', 'Device configuration'),
                    name: 'deviceConfiguration',
                    renderer: function (val) {
                        return '<a href="#/administration/devicetypes/' + val.id + '">' + val.name + '</a>'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.direction', 'DSH', 'Direction'),
                    name: 'direction'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.connType', 'DSH', 'Connection type'),
                    name: 'connectionType'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.connMethod', 'DSH', 'Connection method'),
                    name: 'connectionMethod',
                    renderer: function (val) {
                        return val.name
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.connWindow', 'DSH', 'Connection window'),
                    name: 'window'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.strategy', 'DSH', 'Strategy'),
                    name: 'connectionStrategy',
                    renderer: function (val) {
                        return val.displayValue
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.nextConnection', 'DSH', 'Next connection'),
                    name: 'nextExecution',
                    renderer: function (val) {
                        return Ext.Date.format(val, 'm/d/Y h:i:s');
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
                    fieldLabel: Uni.I18n.translate('connection.widget.details.currentState', 'DSH', 'Current state'),
                    name: 'currentState'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.latestStatus', 'DSH', 'Latest status'),
                    name: 'latestStatus',
                    renderer: function (val) {
                        return val.displayValue;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.latestResult', 'DSH', 'Latest result'),
                    name: 'latestResult',
                    renderer: function (val) {
                        return val.displayValue;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commTasks', 'DSH', 'Communication tasks'),
                    name: 'taskCount',
                    renderer: function (val) {
                        return '<tpl><span class="fa fa-check fa-lg"></span>' + val.numberOfSuccessfulTasks + '<br></tpl>' +
                            '<tpl><span class="fa fa-times fa-lg"></span>' + val.numberOfFailedTasks + '<br></tpl>' +
                            '<tpl><span class="fa fa-ban fa-lg"></span>' + val.numberOfIncompleteTasks + '</tpl>'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.startedOn', 'DSH', 'Started on'),
                    name: 'startDateTime'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.finishedOn', 'DSH', 'Finished on'),
                    name: 'endDateTime'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.duration', 'DSH', 'Duration'),
                    name: 'duration',
                    renderer: function (val) {
                        return val.count + ' ' + val.timeUnit;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commServer', 'DSH', 'Communication server'),
                    name: 'comServer',
                    renderer: function (val) {
                        return '<a href="#/administration/comservers/' + val.id + '">' + val.name + '</a>'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commPort', 'DSH', 'Communication port'),
                    name: 'comPortPool',
                    renderer: function (val) {
                        return val.name
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