Ext.define('Dsh.view.widget.PreviewConnection', {
    extend: 'Ext.form.Panel',
    alias: 'widget.preview_connection',
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
                    fieldLabel: Uni.I18n.translate('connection.widget.details.device', 'DSH', 'Device'),
                    name: 'device',
                    renderer: function (val) {
                        return val ? '<a href="#/devices/' + val.id + '">' + val.name + '</a>' : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.deviceType', 'DSH', 'Device type'),
                    name: 'deviceType',
                    renderer: function (val) {
                        return val ? '<a href="#/administration/devicetypes/' + val.id + '">' + val.name + '</a>' : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.deviceConfig', 'DSH', 'Device configuration'),
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
                    fieldLabel: Uni.I18n.translate('connection.widget.details.connMethod', 'DSH', 'Connection method'),
                    name: 'connectionMethod',
                    renderer: function (val) {
                        return val ? val.name : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.connType', 'DSH', 'Connection type'),
                    name: 'connectionType'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.direction', 'DSH', 'Direction'),
                    name: 'direction'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.connWindow', 'DSH', 'Connection window'),
                    name: 'window'
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.strategy', 'DSH', 'Strategy'),
                    name: 'connectionStrategy',
                    renderer: function (val) {
                        return val ? val.displayValue : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commPortPool', 'DSH', 'Communication port pool'),
                    name: 'comPortPool',
                    renderer: function (val) {
                        return val ? val.name : ''
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
                    name: 'currentState',
                    renderer: function (val) {
                        return val ? val.displayValue : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.latestStatus', 'DSH', 'Latest status'),
                    name: 'latestStatus',
                    renderer: function (val) {
                        return val ? val.displayValue : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.latestResult', 'DSH', 'Latest result'),
                    name: 'latestResult',
                    renderer: function (val) {
                        return val ? val.displayValue : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commTasks', 'DSH', 'Communication tasks'),
                    name: 'taskCount',
                    renderer: function (val) {
                        var failed = val.numberOfFailedTasks ? val.numberOfFailedTasks: '',
                            success = val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '',
                            notCompleted = val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '';
                        return '<tpl><img src="/apps/dsh/resources/images/widget/running.png" class="ct-result ct-success"><span style="position: relative; top: -3px; left: 4px">' + success + '</span><br></tpl>' +
                            '<tpl><img src="/apps/dsh/resources/images/widget/blocked.png" class="ct-result ct-failure"><span style="position: relative; top: -3px; left: 4px">' + failed  + '</span><br></tpl>' +
                            '<tpl><img src="/apps/dsh/resources/images/widget/stopped.png" class="ct-result ct-incomplete"><span  style="position: relative; top: -3px; left: 4px">' + notCompleted + '</span></tpl>'
                            ;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.startedOn', 'DSH', 'Started on'),
                    name: 'startDateTime',
                    renderer: function (val) {
                        return val ? Ext.Date.format(val, 'd/m/Y h:i:s') : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.finishedOn', 'DSH', 'Finished on'),
                    name: 'endDateTime',
                    renderer: function (val) {
                        return val ? Ext.Date.format(val, 'd/m/Y h:i:s') : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.duration', 'DSH', 'Duration'),
                    name: 'duration',
                    renderer: function (val) {
                        return val ? val.count + ' ' + val.timeUnit : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commPort', 'DSH', 'Communication port'),
                    name: 'comPort',
                    renderer: function (val) {
                        return val ? val.name : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.nextConnection', 'DSH', 'Next connection'),
                    name: 'nextExecution',
                    renderer: function (val) {
                        return val ? Ext.Date.format(val, 'd/m/Y h:i:s') : '';
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