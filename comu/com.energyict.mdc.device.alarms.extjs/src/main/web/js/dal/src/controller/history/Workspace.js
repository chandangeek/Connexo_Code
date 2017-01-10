Ext.define('Dal.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Dal.privileges.Alarm'
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
                        startProcess:{
                            title: Uni.I18n.translate('general.startProcess','DAL','Start process'),
                            route: 'startProcess',
                            controller: 'Dal.controller.StartProcess',
                            action: 'showStartProcess',
                            privileges: Dal.privileges.Alarm.viewAdminProcesses,
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

        }

        /*
         "workspace/issues/view/viewProcesses": {
         title: Uni.I18n.translate('general.processes','DAL','Processes'),
         route: 'workspace/issues/{issueId}/processes',
         controller: 'Bpm.monitorissueprocesses.controller.MonitorIssueProcesses',
         action: 'showProcesses',
         privileges: Isu.privileges.Issue.viewAdminProcesses,
         params: {
         process: '',

         },
         }*/
    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
