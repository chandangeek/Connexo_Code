Ext.define('Bpm.controller.history.BpmManagement', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Bpm.privileges.BpmManagement'
    ],
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace: {
            title: Uni.I18n.translate('general.workspace','BPM','Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                taksmanagementtasks: {
                    title: Uni.I18n.translate('bpm.task.title', 'BPM', 'Tasks'),
                    route: 'taksmanagementtasks',
                    controller: 'Bpm.controller.Task',
                    action: 'showTasks',
                    privileges: Bpm.privileges.BpmManagement.view
                }
            }
        }
    },

    tokenizePreviousTokens: function () {
        return this.tokenizePath(this.getApplication().getController('Uni.controller.history.EventBus').previousPath);
    }
});