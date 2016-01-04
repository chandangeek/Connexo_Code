Ext.define('Bpm.controller.OpenTask', {
    extend: 'Ext.app.Controller',
    stores: [
        'Bpm.store.task.TasksFilterAllUsers'
    ],
    models: [
        'Bpm.model.task.TaskEdit',
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
            ref: 'btnStart',
            selector: 'bpm-task-perform-task #btn-start'
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

    init: function () {
        this.control({
            'bpm-task-edit-task #btn-task-save': {
                click: this.saveTask
            },
            'bpm-task-edit-task #num-priority': {
                change: this.updatePriority
            },
            'bpm-task-perform-task #btn-start': {
                click: this.chooseAction
            },
            'bpm-task-perform-task #btn-save': {
                click: this.chooseAction
            },
            'bpm-task-perform-task #btn-complete': {
                click: this.chooseAction
            }
        });

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

        var tasksRoute = router.getRoute('workspace/tasks');
        tasksRoute.params.sort = tasksRoute.params.user = tasksRoute.params.dueDate = tasksRoute.params.status =
            tasksRoute.params.process = undefined;
        tasksRoute.params.use = true;

        sort && (sort != '') && (queryParams.sort = tasksRoute.params.sort = sort);
        user && (user != '') && (queryParams.user = tasksRoute.params.user = user);
        dueDate && (dueDate != '') && (queryParams.dueDate = tasksRoute.params.dueDate = dueDate);
        taskStatus && (taskStatus != '') && (queryParams.status = tasksRoute.params.status = taskStatus);
        process && (process != '') && (queryParams.process = tasksRoute.params.process = process);

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
                me.getApplication().fireEvent('editTask', taskRecord);

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

        var tasksRoute = router.getRoute('workspace/tasks');
        tasksRoute.params.sort = tasksRoute.params.user = tasksRoute.params.dueDate = tasksRoute.params.status =
            tasksRoute.params.process = undefined;
        tasksRoute.params.use = true;

        sort && (sort != '') && (queryParams.sort = tasksRoute.params.sort = sort);
        user && (user != '') && (queryParams.user = tasksRoute.params.user = user);
        dueDate && (dueDate != '') && (queryParams.dueDate = tasksRoute.params.dueDate = dueDate);
        taskStatus && (taskStatus != '') && (queryParams.status = tasksRoute.params.status = taskStatus);
        process && (process != '') && (queryParams.process = tasksRoute.params.process = process);

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
                me.getApplication().fireEvent('performTask', taskRecord);

                topTitle = editTaskView.down('#detail-top-title');
                topTitle.setTitle(Ext.String.format(Uni.I18n.translate('bpm.task.performTaskTitle', 'BPM', "Perform '{0}'"), taskRecord.get('name')));

                me.getApplication().fireEvent('changecontentevent', editTaskView);
                me.getPriority().setVisible(taskRecord.get('status') != 'Completed');
                me.getTaskExecutionForm().setVisible(taskRecord.get('status') != 'Completed');
                editTaskView.down('#frm-assignee-user').loadRecord(taskRecord);
                editTaskView.down('#frm-edit-task').loadRecord(taskRecord);
                editTaskView.down('#frm-about-task').loadRecord(taskRecord);
                me.loadJbpmForm(taskRecord);
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

        var assigneeCombo = assigneeForm.down('#cbo-assignee-user');
        assigneeCombo.store.load({
            callback: function (records, operation, success) {
                assigneeForm.loadRecord(taskRecord);
            }
        });
    },

    loadEditTaskForm: function (taskRecord) {
        var me = this,
            editTaskForm = me.getEditTaskForm() || me.getPerformEditTaskForm();

        editTaskForm && editTaskForm.loadRecord(taskRecord);
    },

    saveTask: function (button) {
        var me = this;

        me.saveAssigneeUser(button);
        setTimeout(function () {
            me.saveEditTask(button);
        }, 100);

    },

    saveAssigneeUser: function (button) {
        var me = this,
            taskRecord = button.taskRecord,
            assignUser = Ext.create('Bpm.model.task.Assign'),
            assigneeForm = me.getAssigneeUserForm();

        assignUser.getProxy().extraParams = {username: assigneeForm.down('#cbo-assignee-user').getValue()};
        assignUser.getProxy().setUrl(taskRecord.get('id'));
        assigneeForm.setLoading();
        assignUser.save({
            success: function () {
                assigneeForm.setLoading(false);
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                    assigneeForm.getForm().markInvalid(json.errors);
                }

                assigneeForm.setLoading(false);
            }
        })
    },

    saveEditTask: function (button) {
        var me = this,
            taskRecord = button.taskRecord,
            taskEdit = Ext.create('Bpm.model.task.TaskEdit'),
            editTaskForm = me.getEditTaskForm();

        me.loadJbpmForm(taskRecord);

        editTaskForm.setLoading();
        editTaskForm.updateRecord(taskRecord);
        taskEdit.getProxy().extraParams = {
            priority: editTaskForm.down('#num-priority').getValue(),
            duedate: editTaskForm.down('#due-date').getValue() ? moment(editTaskForm.down('#due-date').getValue()).valueOf() : ''
        };

        taskEdit.getProxy().setUrl(taskRecord.get('id'));
        taskEdit.save({
            success: function () {
                editTaskForm.setLoading(false);

                var task = me.getModel('Bpm.model.task.Task');
                task.load(taskRecord.get('id'), {
                        success: function (taskRec) {
                            button.taskRecord = taskRec;
                            me.loadAboutTaskForm(taskRec);
                        }
                    }
                );

                me.loadJbpmForm(taskRecord);
            },
            failure: function (record, operation) {
                editTaskForm.setLoading(false);
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

    loadJbpmForm: function (taskRecord) {
        var me = this,
            taskExecutionContent = me.getTaskExecutionContent(),
            openTask = me.getModel('Bpm.model.task.OpenTask'),
            propertyForm;

        if (taskExecutionContent == undefined) {
            return;
        }
        propertyForm = taskExecutionContent.down('property-form');
        taskExecutionContent.setLoading();

        openTask.load(taskRecord.get('id'), {
            success: function (openTaskRecord) {

                taskExecutionContent.openTaskRecord = openTaskRecord;
                if (openTaskRecord && openTaskRecord.properties() && openTaskRecord.properties().count()) {
                    propertyForm.loadRecord(openTaskRecord);
                    propertyForm.show();
                } else {
                    propertyForm.hide();
                }
                propertyForm.up('#frm-task').doLayout();
                me.refreshButtons(openTaskRecord, taskRecord);
                taskExecutionContent.setLoading(false);
            },
            failure: function (record, operation) {
            }
        });

    },

    refreshButtons: function (openTaskRecord, taskRecord) {
        var me = this,
            status = openTaskRecord.get('status'),
            actualOwner = taskRecord.get('actualOwner');

        Ext.Ajax.request({
            url: '/api/bpm/runtime/assignees?me=true',
            method: 'GET',
            success: function (operation) {

                var loggedUser = Ext.JSON.decode(operation.responseText).data[0].name;
                if (actualOwner === loggedUser){
                    me.getBtnStart().setVisible((status == "Reserved"));
                    me.getBtnSave().setVisible(status == "InProgress");
                    me.getBtnComplete().setVisible(status == "InProgress");
                }
                else {
                    var taskExecutionContent = me.getTaskExecutionContent(),
                        propertyForm = taskExecutionContent.down('property-form');
                    propertyForm.getForm().getFields().each (function (field) {
                        field.setReadOnly (true);
                    });
                    me.getBtnStart().setVisible(false);
                    me.getBtnSave().setVisible(false);
                    me.getBtnComplete().setVisible(false);
                }

            }
        })

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
                } else if (button.action === 'startTask') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.task.openTask.started', 'BPM', 'Task started.'));
                } else if (button.action === 'completeTask') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.task.openTask.completed', 'BPM', 'Task completed.'));
                }

                var task = me.getModel('Bpm.model.task.Task');
                task.load(openTaskRecord.get('id'), {
                    success: function (taskRecord) {
                        me.loadAssigneeForm(taskRecord);
                        me.loadEditTaskForm(taskRecord);
                        me.loadAboutTaskForm(taskRecord);

                        if (button.action === 'completeTask') {
                            me.getPriority().setVisible(false);
                            me.getTaskExecutionForm().setVisible(false);
                        }
                        else {
                            me.getPriority().setVisible(true);
                            me.getTaskExecutionForm().setVisible(true);
                            me.loadJbpmForm(taskRecord);
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