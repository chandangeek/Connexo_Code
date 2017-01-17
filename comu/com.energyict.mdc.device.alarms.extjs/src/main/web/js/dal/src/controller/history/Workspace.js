Ext.define('Dal.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Dal.privileges.Alarm',
        'Dal.controller.Overview',
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
            action: 'showIssuesOverview',
            privileges: Dal.privileges.Alarm.viewAdminAlarm
        }

    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
