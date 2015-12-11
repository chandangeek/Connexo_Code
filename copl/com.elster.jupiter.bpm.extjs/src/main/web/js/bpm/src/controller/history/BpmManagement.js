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
                        editTask: {
                            title: Uni.I18n.translate('bpm.task.editTask', 'BPM', 'Edit task'),
                            route: '{taskId}/editTask',
                            controller: 'Bpm.controller.OpenTask',
                            privileges: Bpm.privileges.BpmManagement.assign,
                            action: 'showEditTask',
                            params: {
                                sort: '',
                                user: '',
                                dueDate:'',
                                status:'',
                                process: ''
                            },
                            callback: function (route) {
                                this.getApplication().on('editTask', function (record) {
                                    route.setTitle(Ext.String.format(Uni.I18n.translate('bpm.task.editTaskTitle', 'BPM', "Edit '{0}'"), record.get('name')));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        performTask: {
                            title: Uni.I18n.translate('bpm.task.performTask', 'BPM', 'Perform task'),
                            route: '{taskId}/performTask',
                            controller: 'Bpm.controller.OpenTask',
                            privileges: Bpm.privileges.BpmManagement.execute,
                            action: 'showPerformTask',
                            params: {
                                sort: '',
                                user: '',
                                dueDate:'',
                                status:'',
                                process: ''
                            },
                            callback: function (route) {
                                this.getApplication().on('performTask', function (record) {
                                    route.setTitle(Ext.String.format(Uni.I18n.translate('bpm.task.performTaskTitle', 'BPM', "Perform '{0}'"), record.get('name')));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        bulkaction: {
                            title: Uni.I18n.translate('bpm.task.bulkAction', 'BPM', 'Bulk action'),
                            route: 'bulkaction',
                            controller: 'Bpm.controller.TaskBulk',
                            privileges: Bpm.privileges.BpmManagement.assignOrExecute,
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