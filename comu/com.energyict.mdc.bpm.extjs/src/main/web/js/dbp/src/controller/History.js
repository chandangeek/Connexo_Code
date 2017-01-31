/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                        process: '',
                        workgroup: ''
                    },
                    items: {
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
                                process: '',
                                workgroup: ''
                            }
                        },
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
                                process: '',
                                workgroup: ''
                            },
                            callback: function (route) {
                                this.getApplication().on('task', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            },
                            items: {
                                editTask: {
                                    title: Uni.I18n.translate('bpm.task.edit', 'DBP', 'Edit'),
                                    route: 'edit',
                                    controller: 'Bpm.controller.OpenTask',
                                    privileges: Bpm.privileges.BpmManagement.assign,
                                    action: 'showEditTask',
                                    params: {
                                        sort: '',
                                        user: '',
                                        dueDate:'',
                                        status:'',
                                        process: '',
                                        workgroup: ''
                                    }
                                },
                                performTask: {
                                    title: Uni.I18n.translate('bpm.task.start', 'DBP', 'Start'),
                                    route: 'start',
                                    controller: 'Bpm.controller.OpenTask',
                                    privileges: Bpm.privileges.BpmManagement.execute,
                                    action: 'showPerformTask',
                                    params: {
                                        sort: '',
                                        user: '',
                                        dueDate:'',
                                        status:'',
                                        process: '',
                                        workgroup: ''
                                    }
                                },
                                completeTask: {
                                    title: Uni.I18n.translate('bpm.task.comlete', 'DBP', 'Complete'),
                                    route: 'complete',
                                    controller: 'Bpm.controller.OpenTask',
                                    privileges: Bpm.privileges.BpmManagement.execute,
                                    action: 'showPerformTask',
                                    params: {
                                        sort: '',
                                        user: '',
                                        dueDate:'',
                                        status:'',
                                        process: '',
                                        workgroup: ''
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
});

