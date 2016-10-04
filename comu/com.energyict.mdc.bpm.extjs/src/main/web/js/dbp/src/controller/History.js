Ext.define('Dbp.controller.History', {
    extend: 'Uni.controller.history.Converter',
    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        workspace: {
            title: Uni.I18n.translate('general.workspace','DBP','Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                tasks: {
                    title: Uni.I18n.translate('bpm.task.title', 'DBP', 'Tasks'),
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
                        task: {
                            title: Uni.I18n.translate('bpm.task', 'DBP', 'Task'),
                            route: '{taskId}',
                            controller: 'Bpm.controller.Task',
                            privileges: Bpm.privileges.BpmManagement.view,
                            action: 'showTask',
                            params: {
                                sort: '',
                                user: '',
                                dueDate:'',
                                status:'',
                                process: ''
                            },
                            callback: function (route) {
                                this.getApplication().on('task', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        editTask: {
                            title: Uni.I18n.translate('bpm.task.editTask', 'DBP', 'Edit task'),
                            route: '{taskId}/edit',
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
                                    route.setTitle(Ext.String.format(Uni.I18n.translate('bpm.task.editTaskTitle', 'DBP', "Edit '{0}'"), record.get('name')));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        performTask: {
                            title: Uni.I18n.translate('bpm.task.performTask', 'DBP', 'Perform task'),
                            route: '{taskId}/perform',
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
                                    route.setTitle(Ext.String.format(Uni.I18n.translate('bpm.task.performTaskTitle', 'DBP', "Perform '{0}'"), record.get('name')));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        bulkaction: {
                            title: Uni.I18n.translate('bpm.task.bulkAction', 'DBP', 'Bulk action'),
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
    }
});

