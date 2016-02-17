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
            route: 'startProcess',
            controller: 'Isu.controller.StartProcess',
            action: 'showStartProcess',
            privileges: Isu.privileges.Issue.viewAdminProcesses
        }
    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
