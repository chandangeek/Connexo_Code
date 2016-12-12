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
            items: {
                view: {
                    title: Uni.I18n.translate('general.alarmDetails', 'DAL', 'Alarm details'),
                    route: '{alarmId}',
                    controller: 'Dal.controller.Detail',
                    action: 'showOverview',
                    privileges: Dal.privileges.Alarm.viewAdminDevice,
                    callback: function (route) {
                        this.getApplication().on('issueLoad', function (record) {
                            route.setTitle(record.get('title'));
                            return true;
                        }, {single: true});
                        return this;
                    }
                }
            }

            //privileges: Isu.privileges.Issue.viewAdminProcesses
        }
        /*    "workspace/issues/view/startProcess": {
         title: Uni.I18n.translate('general.startProcess','DAL','Start process'),
         route: 'workspace/issues/{issueId}/startProcess',
         controller: 'Isu.controller.StartProcess',
         action: 'showStartProcess',
         privileges: Isu.privileges.Issue.viewAdminProcesses
         },
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
