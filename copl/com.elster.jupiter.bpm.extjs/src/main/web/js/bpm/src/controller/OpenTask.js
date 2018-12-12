/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.controller.OpenTask', {
    extend: 'Ext.app.Controller',
    stores: [
        'Bpm.store.task.TasksFilterAllUsers'
    ],
    models: [
        'Bpm.model.task.Assign',
        'Bpm.model.task.OpenTask'
    ],
    views: [
        'Bpm.view.task.EditTask',
        'Bpm.view.task.PerformTask'
    ],
    refs: [
        {
            ref: 'editTaskPage',
            selector: 'bpm-task-edit-task'
        },
        {
            ref: 'performTaskPage',
            selector: 'bpm-task-perform-task'
        },
        {
            ref: 'assigneeUserForm',
            selector: 'bpm-task-edit-task #frm-assignee-user'
        },
        {
            ref: 'editTaskForm',
            selector: 'bpm-task-edit-task #frm-edit-task'
        },
        {
            ref: 'numPriority',
            selector: 'bpm-task-edit-task #num-priority'
        },
        {
            ref: 'priorityDisplay',
            selector: 'bpm-task-edit-task #priority-display'
        },
        {
            ref: 'aboutTaskForm',
            selector: 'bpm-task-edit-task #frm-about-task'
        },
        {
            ref: 'performEditTaskForm',
            selector: 'bpm-task-perform-task #frm-edit-task'
        },
        {
            ref: 'performAboutTaskForm',
            selector: 'bpm-task-perform-task #frm-about-task'
        },
        {
            ref: 'taskExecutionContent',
            selector: 'bpm-task-perform-task #task-execution-content'
        },
        {
            ref: 'taskExecutionForm',
            selector: 'bpm-task-perform-task #task-execution-form'
        },
        {
            ref: 'priority',
            selector: 'bpm-task-perform-task #priority'
        },
        {
            ref: 'btnSave',
            selector: 'bpm-task-perform-task #btn-save'
        },
        {
            ref: 'btnComplete',
            selector: 'bpm-task-perform-task #btn-complete'
        }
    ],
    taskId: null,

    init: function () {
        this.control({
            'bpm-task-edit-task #btn-task-save': {
                click: this.saveTask
            },
            'bpm-task-edit-task #num-priority': {
                change: this.updatePriority
            },
            'bpm-task-perform-task #btn-save': {
                click: this.chooseAction
            },
            '#btn-task-cancel-link': {
                click: this.returnToPreviousPage
            },
            'bpm-task-perform-task #btn-complete': {
                click: this.chooseAction
            }
        });

    },

    returnToPreviousPage: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        if (Ext.isEmpty(me.taskId)) {
            router.getRoute('workspace/tasks').forward();
        } else {
            router.getRoute('workspace/tasks/task').forward({taskId: me.taskId});
        }
    },

    showEditTask: function (taskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            tasksRoute = router.getRoute('workspace/tasks'),
            editTaskView, topTitle, taskRecord, queryParams = {};

        sort = router.arguments.sort;
        user = router.arguments.user;
        dueDate = router.arguments.dueDate;
        taskStatus = router.arguments.status;
        process = router.arguments.process;
        process = router.arguments.process;
        workgroup = router.arguments.workgroup;

        var tasksRoute = router.getRoute('workspace/tasks');
        tasksRoute.params.sort = tasksRoute.params.user = tasksRoute.params.dueDate = tasksRoute.params.status =
            tasksRoute.params.process = undefined;
        tasksRoute.params.use = true;

        sort && (sort != '') && (queryParams.sort = tasksRoute.params.sort = sort);
        user && (user != '') && (queryParams.user = tasksRoute.params.user = user);
        dueDate && (dueDate != '') && (queryParams.dueDate = tasksRoute.params.dueDate = dueDate);
        taskStatus && (taskStatus != '') && (queryParams.status = tasksRoute.params.status = taskStatus);
        process && (process != '') && (queryParams.process = tasksRoute.params.process = process);
        workgroup && (workgroup != '') && (queryParams.workgroup = tasksRoute.params.workgroup = workgroup);

        var task = me.getModel('Bpm.model.task.Task');
        task.load(taskId, {
            success: function (taskRecord) {

                editTaskView = Ext.create('Bpm.view.task.EditTask', {
                    itemNameLink: '<a href="' + tasksRoute.buildUrl({}, queryParams) + '">' + Uni.I18n.translate('bpm.task.tasksName', 'BPM', 'tasks') + '</a>',
                    router: me.getController('Uni.controller.history.Router'),
                    taskRecord: taskRecord,
                    showNavigation: queryString.showNavigation
                });

                editTaskView.taskRecord = taskRecord;
                me.getApplication().fireEvent('task', taskRecord);

                topTitle = editTaskView.down('#detail-top-title');
                topTitle.setTitle(Ext.String.format(Uni.I18n.translate('bpm.task.editTaskTitle', 'BPM', "Edit '{0}'"), taskRecord.get('name')));

                me.getApplication().fireEvent('changecontentevent', editTaskView);
                me.loadAssigneeForm(taskRecord);
                me.loadEditTaskForm(taskRecord);
                me.loadAboutTaskForm(taskRecord);
            },
            failure: function (record, operation) {
            }
        });
    },

    showPerformTask: function (taskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            tasksRoute = router.getRoute('workspace/tasks'),
            editTaskView, topTitle, taskRecord, queryParams = {};

        sort = router.arguments.sort;
        user = router.arguments.user;
        dueDate = router.arguments.dueDate;
        taskStatus = router.arguments.status;
        process = router.arguments.process;
        workgroup = router.arguments.workgroup;

        var tasksRoute = router.getRoute('workspace/tasks');
        tasksRoute.params.sort = tasksRoute.params.user = tasksRoute.params.dueDate = tasksRoute.params.status =
            tasksRoute.params.process = undefined;
        tasksRoute.params.use = true;

        sort && (sort != '') && (queryParams.sort = tasksRoute.params.sort = sort);
        user && (user != '') && (queryParams.user = tasksRoute.params.user = user);
        dueDate && (dueDate != '') && (queryParams.dueDate = tasksRoute.params.dueDate = dueDate);
        taskStatus && (taskStatus != '') && (queryParams.status = tasksRoute.params.status = taskStatus);
        process && (process != '') && (queryParams.process = tasksRoute.params.process = process);
        workgroup && (workgroup != '') && (queryParams.workgroup = tasksRoute.params.workgroup = workgroup);

        var task = me.getModel('Bpm.model.task.Task');
        task.load(taskId, {
            success: function (taskRecord) {

                editTaskView = Ext.create('Bpm.view.task.PerformTask', {
                    itemNameLink: '<a href="' + tasksRoute.buildUrl({}, queryParams) + '">' + Uni.I18n.translate('bpm.task.tasksName', 'BPM', 'tasks') + '</a>',
                    router: me.getController('Uni.controller.history.Router'),
                    taskRecord: taskRecord,
                    showNavigation: queryString.showNavigation
                });

                editTaskView.taskRecord = taskRecord;
                me.getApplication().fireEvent('task', taskRecord);

                topTitle = editTaskView.down('#detail-top-title');
                if(taskRecord.get('status') === 'InProgress') {
                    topTitle.setTitle(Ext.String.format(Uni.I18n.translate('bpm.task.completeTaskTitle', 'BPM', "Complete '{0}'"), taskRecord.get('name')));
                } else {
                    topTitle.setTitle(Ext.String.format(Uni.I18n.translate('bpm.task.startTaskTitle', 'BPM', "Start '{0}'"), taskRecord.get('name')));
                }

                me.getApplication().fireEvent('changecontentevent', editTaskView);
                if (me.getPriority()) {
                    me.getPriority().setVisible(taskRecord.get('status') != 'Completed');
                }
                me.getTaskExecutionForm().setVisible(taskRecord.get('status') != 'Completed');
                if (editTaskView.down('#frm-assignee-user')) {
                    editTaskView.down('#frm-assignee-user').loadRecord(taskRecord);
                }
                if (editTaskView.down('#frm-edit-task')) {
                    editTaskView.down('#frm-edit-task').loadRecord(taskRecord);
                }
                if (editTaskView.down('#frm-about-task')) {
                    editTaskView.down('#frm-about-task').loadRecord(taskRecord);
                }
                me.getModel('Bpm.model.task.OpenTask').load(taskId, {
                    success: function (openTaskRecord) {
                        Ext.Ajax.request({
                            url: '/api/bpm/runtime/assignees?me=true',
                            method: 'GET',
                            success: function (operation) {

                                var loggedUser = Ext.JSON.decode(operation.responseText).data[0].name;
                                var actualOwner = taskRecord.get('actualOwner');
                                var isAssignee = loggedUser === actualOwner;
                                if (isAssignee && openTaskRecord.get('status') !== 'InProgress' && openTaskRecord.get('status') !== 'Completed') {
                                    openTaskRecord.beginEdit();
                                    openTaskRecord.set('action', 'startTask');
                                    openTaskRecord.endEdit();
                                    openTaskRecord.save({
                                        success: function () {
                                            me.getModel('Bpm.model.task.OpenTask').load(taskId, {
                                                success: function (newOpenTaskRecord) {
                                                    me.loadJbpmForm(taskRecord, isAssignee, newOpenTaskRecord);
                                                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.task.openTask.started', 'BPM', 'Task started.'));
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    me.loadJbpmForm(taskRecord, isAssignee, openTaskRecord);
                                }
                            }
                        });
                    }
                });
            },
            failure: function (record, operation) {
            }
        });
    },

    loadAssigneeForm: function (taskRecord) {
        var me = this,
            assigneeForm = me.getAssigneeUserForm();

        if (assigneeForm == undefined) {
            return;
        }
        assigneeForm.loadRecord(taskRecord);
        /*
        var assigneeCombo = assigneeForm.down('#cbo-assignee-user');
        assigneeCombo.store.load({
            callback: function (records, operation, success) {
                assigneeForm.loadRecord(taskRecord);
            }
         });*/
    },

    loadEditTaskForm: function (taskRecord) {
        var me = this,
            editTaskForm = me.getEditTaskForm() || me.getPerformEditTaskForm();

        editTaskForm && editTaskForm.loadRecord(taskRecord);
    },

    saveTask: function (button) {
        var me = this;

        me.saveAssigneeUser(button);

    },

    saveAssigneeUser: function (button) {
        var me = this,
            taskRecord = button.taskRecord,
            assignUser = Ext.create('Bpm.model.task.Assign'),
            assigneeForm = me.getAssigneeUserForm(),
            editTaskForm = me.getEditTaskForm();

        assignUser.getProxy().extraParams = {
            userId: assigneeForm.down('#cbo-user-assignee').getValue(),
            workgroupId: assigneeForm.down('#cbo-workgroup-assignee').getValue(),
            priority: editTaskForm.down('#num-priority').getValue(),
            duedate: editTaskForm.down('#due-date').getValue() ? moment(editTaskForm.down('#due-date').getValue()).valueOf() : ''
        };
        assignUser.getProxy().setUrl(taskRecord.get('id'), taskRecord.get('optLock'));
        assignUser.save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.task.openTask.saved', 'BPM', 'Task saved.'));
                me.returnToPreviousPage();
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                    assigneeForm.getForm().markInvalid(json.errors);
                }
            }
        })
    },

    loadAboutTaskForm: function (taskRecord) {
        var me = this,
            aboutTaskForm = me.getAboutTaskForm() || me.getPerformAboutTaskForm();

        if (aboutTaskForm == undefined) {
            return;
        }

        aboutTaskForm.setLoading();
        aboutTaskForm && aboutTaskForm.loadRecord(taskRecord);
        aboutTaskForm.setLoading(false);
    },

    updatePriority: function (control, newValue, oldValue) {
        var me = this,
            label = '';

        if (newValue <= 3) {
            label = Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High');
        }
        else if (newValue <= 7) {
            label = Uni.I18n.translate('bpm.task.priority.medium', 'BPM', 'Medium');
        }
        else {
            label = Uni.I18n.translate('bpm.task.priority.low', 'BPM', 'Low');
        }

        me.getPriorityDisplay().setText(label);
    },

    loadJbpmForm: function (taskRecord, isAssignee, openTaskRecord) {
        var me = this,
            taskExecutionContent = me.getTaskExecutionContent(),
            propertyForm;

        if (taskExecutionContent == undefined) {
            return;
        }
        propertyForm = taskExecutionContent.down('property-form');
        taskExecutionContent.setLoading();


        taskExecutionContent.openTaskRecord = openTaskRecord;
        if (openTaskRecord && openTaskRecord.properties() && openTaskRecord.properties().count()) {
            propertyForm.loadRecord(openTaskRecord);
            propertyForm.show();
        } else {
            propertyForm.hide();
        }
        propertyForm.up('#frm-task').doLayout();
        me.refreshButtons(openTaskRecord, taskRecord, isAssignee);
        taskExecutionContent.setLoading(false);


    },

    refreshButtons: function (openTaskRecord, taskRecord, isAssignee) {
        var me = this,
            status = openTaskRecord.get('status');

        if (isAssignee) {
            me.getBtnSave().setVisible(status == "InProgress");
            me.getBtnComplete().setVisible(status == "InProgress");
        }
        else {
            var taskExecutionContent = me.getTaskExecutionContent(),
                propertyForm = taskExecutionContent.down('property-form');
            propertyForm.getForm().getFields().each(function (field) {
                field.setReadOnly(true);
            });
            me.getBtnSave().setVisible(false);
            me.getBtnComplete().setVisible(false);
            me.getPerformTaskPage().down('uni-form-empty-message').show();
        }
    },


    chooseAction: function (button, item) {
        var me = this,
            action = button.action,
            taskExecutionForm = me.getTaskExecutionForm(),
            taskExecutionContent = me.getTaskExecutionContent(),
            openTaskRecord = taskExecutionContent.openTaskRecord,
            propertyForm = taskExecutionContent.down('property-form');

        propertyForm.updateRecord();

        openTaskRecord.beginEdit();
        if (propertyForm.getRecord()) {
            openTaskRecord.propertiesStore = propertyForm.getRecord().properties();
        }
        openTaskRecord.set('action', action);
        openTaskRecord.endEdit();

        openTaskRecord.save({
            success: function () {

                if (button.action === 'saveTask') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.task.openTask.saved', 'BPM', 'Task saved.'));
                    me.returnToPreviousPage();
                    return;
                } else if (button.action === 'completeTask') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.task.openTask.completed', 'BPM', 'Task completed.'));
                    me.taskId = null;
                    me.returnToPreviousPage();
                    return;
                }

                var task = me.getModel('Bpm.model.task.Task');
                task.load(openTaskRecord.get('id'), {
                    success: function (taskRecord) {
                        me.loadAssigneeForm(taskRecord);
                        me.loadEditTaskForm(taskRecord);
                        me.loadAboutTaskForm(taskRecord);

                        if (button.action === 'completeTask') {
                            if (me.getPriority()) {
                                me.getPriority().setVisible(false);
                            }
                            me.getTaskExecutionForm().setVisible(false);
                        }
                        else {
                            if (me.getPriority()) {
                                me.getPriority().setVisible(true);
                            }
                            me.getTaskExecutionForm().setVisible(true);
                            me.loadJbpmForm(taskRecord, true);
                        }
                    }
                });
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        taskExecutionForm.getForm().markInvalid(json.errors);
                    }
                }
            }
        })
    }

});