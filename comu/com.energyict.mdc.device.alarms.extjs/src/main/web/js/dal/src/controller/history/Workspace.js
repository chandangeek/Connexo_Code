/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Dal.privileges.Alarm',
        'Dal.controller.Overview',
        'Dal.controller.CreationRules',
        'Dal.controller.CreationRuleEdit',
        'Dal.controller.CreationRuleActionEdit'
    ],
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        "workspace/alarms": {
            title: Uni.I18n.translate('device.alarms', 'DAL', 'Alarms'),
            route: 'workspace/alarms',
            controller: 'Dal.controller.Alarms',
            action: 'showOverview',
            privileges: Dal.privileges.Alarm.viewAdminAlarm,
            items: {
                bulkaction: {
                    title: Uni.I18n.translate('general.bulkAction','DAL','Bulk action'),
                    route: 'bulkaction',
                    privileges: Dal.privileges.Alarm.viewAdminAlarm,
                    controller: 'Dal.controller.BulkChangeAlarms'
                },
                view: {
                    title: Uni.I18n.translate('general.alarmDetails', 'DAL', 'Alarm details'),
                    route: '{alarmId}',
                    controller: 'Dal.controller.Detail',
                    action: 'showOverview',
                    privileges: Dal.privileges.Alarm.viewAdminAlarm,
                    callback: function (route) {
                        this.getApplication().on('issueLoad', function (record) {
                            route.setTitle(record.get('title'));
                            return true;
                        }, {single: true});
                        return this;
                    },

                    items: {
                        startProcess: {
                            title: Uni.I18n.translate('general.startProcess', 'DAL', 'Start process'),
                            route: 'startProcess',
                            controller: 'Dal.controller.StartProcess',
                            action: 'showStartProcess',
                            privileges: Dal.privileges.Alarm.viewAdminProcesses,
                        },
                        viewProcesses: {
                            title: Uni.I18n.translate('general.processes', 'DAL', 'Processes'),
                            route: 'processes',
                            controller: 'Bpm.monitorissueprocesses.controller.MonitorIssueProcesses',
                            action: 'showAlarmProcesses',
                            privileges: Dal.privileges.Alarm.viewAdminProcesses,
                            params: {
                                process: '',

                            },
                        },
                        setpriority:{
                            title: Uni.I18n.translate('general.setpriority','DAL','Set priority'),
                            route: 'setpriority',
                            controller: 'Dal.controller.SetPriority',
                            action: 'setPriority',
                            privileges: Dal.privileges.Alarm.viewAdminAlarm
                        },
                        action: {
                            title: Uni.I18n.translate('general.action', 'DAL', 'Action'),
                            route: 'action/{actionId}',
                            controller: 'Dal.controller.ApplyAction',
                            privileges: Dal.privileges.Alarm.viewAdminAlarm,
                            callback: function (route) {
                                this.getApplication().on('issueActionLoad', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        }

                    }
                }
            }

        },
        "workspace/alarmsoverview":{
            title: Uni.I18n.translate('device.alarms.overview', 'DAL', 'Alarms overview'),
            route: 'workspace/alarmsoverview',
            controller: 'Dal.controller.Overview',
            action: 'showAlarmOverview',
            privileges: Dal.privileges.Alarm.viewAdminAlarm
        },
        administration : {
            title: Uni.I18n.translate('route.administration', 'DAL', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                alarmcreationrules: {
                    title: Uni.I18n.translate('general.alarmCreationRules', 'DAL', 'Alarm creation rules'),
                    route: 'alarmcreationrules',
                    controller: 'Dal.controller.CreationRules',
                    privileges: Dal.privileges.Alarm.viewAdminAlarmCreationRule,
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.alarmCreationRules.add', 'DAL', 'Add alarm creation rule'),
                            route: 'add',
                            controller: 'Dal.controller.CreationRuleEdit',
                            privileges: Dal.privileges.Alarm.createAlarmRule,
                            action: 'showEdit',
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('general.addAction', 'DAL', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Dal.controller.CreationRuleActionEdit',
                                    action: 'showEdit'
                                }
                            }
                        },
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'DAL', 'Edit'),
                            route: '{id}/edit',
                            controller: 'Dal.controller.CreationRuleEdit',
                            action: 'showEdit',
                            privileges: Dal.privileges.Alarm.createAlarmRule,
                            callback: function (route) {
                                this.getApplication().on('alarmCreationRuleEdit', function (record) {
                                    route.setTitle(Uni.I18n.translate('administration.alarmCreationRules.title.editIssueCreationRule', 'DAL', "Edit '{0}'", record.get('name'), false));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('general.addAction', 'DAL', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Dal.controller.CreationRuleActionEdit',
                                    action: 'showEdit'
                                }
                            }
                        }
                    }
                }
            }
        }
    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
