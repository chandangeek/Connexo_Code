/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.PreviewConnection', {
    extend: 'Ext.form.Panel',
    alias: 'widget.preview_connection',
    requires: [
        'Uni.util.Common'
    ],
    title: '',
    frame: true,
    communicationViewMode: false,
    layout: {
        type: 'column'
    },

    initComponent: function() {
        var me = this;
        me.tools =  [
            {
                xtype: 'uni-button-action',
                itemId: 'connectionsPreviewActionBtn',
                menu: {
                    xtype: 'connection-action-menu',
                    itemId: 'connectionsActionMenu',
                    communicationViewMode: me.communicationViewMode
                }
            }
        ];
        me.callParent(arguments);
    },
    items: [
        {
            columnWidth: 0.5,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.device', 'DSH', 'Device'),
                    name: 'device',
                    renderer: function (val) {
                        var res = '-';
                        if (val) {
                            res = Mdc.privileges.Device.canViewOrAdministrateDeviceData()
                                ? '<a href="#/devices/' + Uni.util.Common.encodeURIComponent(val.name) + '">' + Ext.String.htmlEncode(val.name) + '</a>'
                                : Ext.String.htmlEncode(val.name);
                        }
                        return res;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.deviceType', 'DSH', 'Device type'),
                    name: 'deviceType',
                    renderer: function (val) {
                        var res = '-';
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
                        var res = '-';
                        val && (res = '<a href="#/administration/devicetypes/' +
                            val.devType.id + '/deviceconfigurations/' +
                            val.config.id +
                            '">' +
                            Ext.String.htmlEncode(val.config.name) +
                            '</a>');
                        if (res !== '' && !Mdc.privileges.DeviceType.canView()) {
                            res = Ext.String.htmlEncode(val.config.name);
                        }
                        return res;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.connection', 'DSH', 'Connection'),
                    name: 'connectionMethod',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.name) : '-'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.connectionType', 'DSH', 'Connection type'),
                    name: 'connectionType',
                    renderer: function(value) {
                        return Ext.isEmpty(value) ? '-' : value;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.direction', 'DSH', 'Direction'),
                    name: 'direction',
                    renderer: function(value) {
                        return Ext.isEmpty(value) ? '-' : value;
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
                        return val ? Ext.String.htmlEncode(val.displayValue) : '-'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.commPortPool', 'DSH', 'Communication port pool'),
                    name: 'comPortPool',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.name) : '-'
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
                    fieldLabel: Uni.I18n.translate('general.lastConnection', 'DSH', 'Last connection'),
                    name: 'latestResult',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.displayValue) : '-'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.lastCommTasks', 'DSH', 'Last communication tasks'),
                    name: 'taskCount',
                    height: 60,
                    cls: 'communication-tasks-status',
                    renderer: function (val) {
                        var failed = val.numberOfFailedTasks ? val.numberOfFailedTasks : 0,
                            success = val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : 0,
                            notCompleted = val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : 0;
                        if (failed === 0 && success === 0 && notCompleted === 0) {
                            return '-';
                        } else {
                            return '<tpl><span class="icon-checkmark"></span>' + success + '<br></tpl>' +
                                '<tpl><span class="icon-cross"></span>' + failed + '<br></tpl>' +
                                '<tpl><span class="icon-stop2"></span>' + notCompleted + '</tpl>'
                                ;
                        }
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.lastResult', 'DSH', 'Last result'),
                    name: 'latestStatus',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.displayValue) : '-'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'DSH', 'Status'),
                    name: 'currentState',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.displayValue) : '-'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.startedOn', 'DSH', 'Started on'),
                    name: 'startDateTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.finishedOn', 'DSH', 'Finished on'),
                    name: 'endDateTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.duration', 'DSH', 'Duration'),
                    name: 'duration',
                    renderer: function (val) {
                        return val ? val.count + ' ' + val.timeUnit : '-';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.commPort', 'DSH', 'Communication port'),
                    name: 'comPort',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.name) : '-'
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('connection.widget.details.nextConnection', 'DSH', 'Next connection'),
                    name: 'nextExecution',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                    }
                }
            ]
        }
    ]
});