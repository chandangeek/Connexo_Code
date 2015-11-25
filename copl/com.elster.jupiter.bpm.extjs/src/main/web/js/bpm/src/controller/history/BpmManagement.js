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
                tasks: {
                    title: Uni.I18n.translate('bpm.task.title', 'BPM', 'Tasks'),
                    route: 'tasks',
                    controller: 'Bpm.controller.Task',
                    action: 'showTasks',
                    privileges: Bpm.privileges.BpmManagement.view,
                    params: {
                        use: false,
                        sort: '',
                        user: '',
                        dueDate:'',
                        status:'',
                        process: ''
                    },
                    items: {
                        openTask: {
                            title: Uni.I18n.translate('bpm.task.openTask', 'BPM', 'Open task'),
                            route: '{taskId}/openTask',
                            controller: 'Bpm.controller.OpenTask',
                            privileges: Bpm.privileges.BpmManagement.assignOrExecute,
                            action: 'showOpenTask',
                            params: {
                                sort: '',
                                user: '',
                                dueDate:'',
                                status:'',
                                process: ''
                            },
                            callback: function (route) {
                                this.getApplication().on('openTask', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        bulkaction: {
                            title: Uni.I18n.translate('bpm.task.bulkAction', 'BPM', 'Bulk action'),
                            route: 'bulkaction',
                            controller: 'Bpm.controller.TaskBulk',
                            privileges: Bpm.privileges.BpmManagement.assignAndExecute,
                            action: 'showOverview',
                            params: {
                                sort: '',
                                user: '',
                                dueDate:'',
                                status:'',
                                process: ''
                            }
                        }
                    }					
                }
            }
        }
    },

    tokenizePreviousTokens: function () {
        return this.tokenizePath(this.getApplication().getController('Uni.controller.history.EventBus').previousPath);
    }
});