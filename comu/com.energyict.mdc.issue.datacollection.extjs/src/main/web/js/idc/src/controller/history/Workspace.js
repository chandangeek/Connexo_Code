/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    requires:[
        'Isu.privileges.Issue'
    ],
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        "workspace/issues/view/startProcess": {
            title: Uni.I18n.translate('general.startProcess','IDC','Start process'),
            route: 'workspace/issues/{issueId}/startProcess',
            controller: 'Isu.controller.StartProcess',
            action: 'showStartProcess',
            privileges: Isu.privileges.Issue.viewAdminProcesses
        },
        "workspace/issues/view/viewProcesses": {
            title: Uni.I18n.translate('general.processes','IDC','Processes'),
            route: 'workspace/issues/{issueId}/processes',
            controller: 'Bpm.monitorissueprocesses.controller.MonitorIssueProcesses',
            action: 'showProcesses',
            privileges: Isu.privileges.Issue.viewAdminProcesses,
            params: {
                process: '',

            },
        }
    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
