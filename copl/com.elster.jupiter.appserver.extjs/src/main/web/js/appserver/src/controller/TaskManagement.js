/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.controller.TaskManagement', {
    extend: 'Ext.app.Controller',

    views: [
        'Apr.view.taskmanagement.Setup',
        'Apr.view.taskmanagement.TaskGrid',
        'Apr.view.taskmanagement.TaskPreview',
        'Apr.view.taskmanagement.TaskFilter',
        'Apr.view.taskmanagement.Add',
        'Apr.view.taskmanagement.QueueAndPriorityWindow'
    ],
    stores: [
        'Apr.store.Tasks',
        'Apr.store.QueuesByApplication',
        'Apr.store.TasksQueueTypes',
        'Apr.store.CustomTaskTypes',
        'Apr.store.TasksType',
        'Apr.store.Queues'
    ],
    models: [
        'Apr.model.Task',
        'Apr.model.Queue',
        'Apr.model.TaskInfo',
        'Apr.model.Triggers',
        'Apr.model.CustomTaskType'
    ],
    requires: [
        'Apr.privileges.AppServer'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'task-management-setup'
        },
        {
            ref: 'taskPreview',
            selector: 'task-management-setup #task-management-preview'
        },
        {
            ref: 'filter',
            selector: 'task-management-filter'
        },
        {
            ref: 'addPage',
            selector: 'task-management-add'
        },
        {
            ref: 'taskManagementGrid',
            selector: 'task-management-grid'
        },
        {
            ref: 'queuePriorityWindow',
            selector: 'queue-priority-window-management'
        }
    ],

    taskTypeDefaultValue: null,

    init: function () {
        this.control({
            'task-management-grid': {
                select: this.showPreview
            },
            'task-management-add combobox[itemId=task-management-task-type]': {
                change: this.taskTypeChanged
            },
            'task-management-add #add-button': {
                click: this.addTask
            },
            'task-management-action-menu': {
                click: this.chooseMenuAction,
                show: this.onMenuShow
            },
            'queue-priority-window-management #save-queue-priority-button': {
                click: this.saveQueuePriority
            }
        });
    },

    showTaskManagement: function () {
        var me = this,
            queuesStore = me.getStore('Apr.store.QueuesByApplication'), widget;
            queueTypesStore = me.getStore('Apr.store.TasksQueueTypes'), widget;

        queuesStore.getProxy().extraParams = {application: this.applicationKey};
        queueTypesStore.getProxy().extraParams = {application: this.applicationKey};
        widget = Ext.widget('task-management-setup', {
            applicationKey: me.applicationKey,
            addTaskRoute: me.addTaskRoute,
            queuesStore: queuesStore,
            queueTypesStore: queueTypesStore,
            router: me.getController('Uni.controller.history.Router')
        });
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    viewTaskManagement: function (taskType, taskManagementId) {
        var me = this,
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        if ((taskManagement && taskManagement.controller && taskManagement.controller.canView()) ||
            (taskManagement && taskManagement.controller && taskManagement.controller.getTaskRoute)) {

            taskManagement.controller.getTask(this, taskManagementId, function (taskController, taskManagementId, task) {
                var route = '';
                if (taskManagement.controller.getTaskRoute) {
                    route = me.getController('Uni.controller.history.Router').getRoute(taskManagement.controller.getTaskRoute());
                    window.location.replace(route.buildUrl({taskId: task.get('id')}));
                }
                else {
                    route = me.getController('Uni.controller.history.Router').getRoute('administration/taskmanagement/view');
                    window.location.replace(route.buildUrl({type: taskType, taskManagementId: taskManagementId, taskId: task.get('id')}));
                }
            })
        }
        else {
            var generalTask = me.getController('Apr.controller.TaskManagementGeneralTask')
            generalTask.getTask(this, taskManagementId, function (taskController, taskManagementId, task) {
                var actionMenu = null;
                if (task && task.get("recurrentTask") && task.get("recurrentTask").queue === "AddCertReqDataTopic" && Uni.Auth.checkPrivileges(Apr.privileges.AppServer.reccurentTaskAdmin))
                {
                    actionMenu = {
                        xtype: 'task-management-action-menu',
                        itemId: 'task-management-action-menu'
                    }
                }
                generalTask.viewTaskManagement(taskManagementId, actionMenu, task);
            })
        }
    },

    showPreview: function (records, record) {
        var me = this,
            taskPreview = me.getTaskPreview(),
            taskType = record.get('queue'),
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        Ext.suspendLayouts();
        taskPreview.setTitle(Ext.String.htmlEncode(record.get('name')));
        taskPreview.down('#task-management-preview-form').loadRecord(record);
        taskPreview.down('#btn-task-management-preview-action-menu').setVisible(
            (taskManagement && taskManagement.controller && taskManagement.controller.canAdministrate())
            || me.canSetQueuePriority(record) || Uni.Auth.checkPrivileges('privilege.suspend.SuspendTaskOverview')
        );
        taskPreview.down('task-management-action-menu').record = record;
        if (record.get('queueStatus') == 'Busy') {
            taskPreview.down('#durationField').show();
            taskPreview.down('#currentRunField').show();
        } else {
            taskPreview.down('#durationField').hide();
            taskPreview.down('#currentRunField').hide();
        }
        Ext.resumeLayouts(true);

        taskPreview.setLoading();
        me.getModel('Apr.model.Triggers').load(record.get('id'), {
            success: function (triggers) {
                taskPreview.setRecurrentTasks('#followedBy-field-container', triggers.get('nextRecurrentTasks'));
                taskPreview.setRecurrentTasks('#precededBy-field-container', triggers.get('previousRecurrentTasks'));
                taskPreview.setLoading(false);
            }
        });
    },

    chooseMenuAction: function (menu, item) {
        var me = this,
            record = menu.record || me.getTaskManagementGrid().getSelectionModel().getLastSelected(),
            taskType = record.get('queue'),
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType),
            route;

        switch (item.action) {
            case 'runTask':
                taskManagement && taskManagement.controller && taskManagement.controller.runTaskManagement(record, me.operationStart, me.operationCompleted, this);
                break;
            case 'editTask':
                taskManagement && taskManagement.controller && taskManagement.controller.getTask(this, record.get('id'), me.viewDetailsTaskLoaded);
                break;
            case 'historyTask':
                taskManagement && taskManagement.controller && taskManagement.controller.getTask(this, record.get('id'), me.viewHistoryTaskLoaded)
                break;
            case 'removeTask':
                taskManagement && taskManagement.controller && taskManagement.controller.removeTaskManagement(record, me.removeOperationStart, me.removeOperationCompleted, this);
                break;
            case 'setTriggers':
                me.showSetTriggers(record);
                break;
            case 'suspendTask':
                me.suspendTaskManagement(record, me.suspendOperationStart, me.suspendOperationCompleted, this);
                break;
            case 'setQueueAndPriority':
                me.setQueueAndPriority(record);
                break;
        }
    },

    onMenuShow: function (menu) {
        var taskType = menu.record.get('queue'),
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);
        Ext.suspendLayouts();
        var hasTaskAdminPrivilege = !(menu.record.get('application').id === "MultiSense") || Uni.Auth.hasPrivilege('privilege.edit.AdministerTaskOverview');
        menu.down('#run-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canRun() && hasTaskAdminPrivilege);
        menu.down('#edit-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canEdit() && hasTaskAdminPrivilege);
        menu.down('#history-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canHistory());
        menu.down('#remove-task').setVisible(taskManagement && taskManagement.controller && taskManagement.controller.canRemove() && hasTaskAdminPrivilege);
        menu.down('#set-queue-priority').setVisible(this.canSetQueuePriority(menu.record));
        menu.reorderItems();
        Ext.resumeLayouts(true);
    },

    canSetQueuePriority: function (record) {
        var application = record.get('application').id,
            privilege = false;
        if (application === "MultiSense") {
            privilege = Uni.Auth.checkPrivileges(Mdc.privileges.TaskManagement.administrateTaskOverview)
               && (record.get('extraQueueCreationEnabled') || record.get('queuePrioritized'));
        };
        return privilege;
    },

    /* add task section */
    showAddTask: function () {
        var me = this,
            appName = Uni.util.Application.getAppName(),
            view = Ext.create('Apr.view.taskmanagement.Add', {
                edit: false,
                taskTypeDefaultValue: me.taskTypeDefaultValue,
                addReturnLink: me.rootRouteWithArguments,
                storeTypes: me.getTypesStore()
            });

        me.taskTypeDefaultValue = null;

        this.getApplication().on('saveCurrentFormState', function () {
            me.taskTypeDefaultValue = view && view.down('#task-management-task-type') && view.down('#task-management-task-type').getValue();
        });

        me.getApplication().fireEvent('changecontentevent', view);
    },

    getTypesStore: function () {
        var me = this,
            customTaskTypes = me.getStore('Apr.store.CustomTaskTypes');

        var apps = Apr.TaskManagementApp.getTaskManagementApps(),
            data = [];

        apps.each(function (key, value, length) {
            value.controller && value.controller.canAdministrate() && data.push([key, value.name]);
        });

        return new Ext.data.ArrayStore({
            fields: ['id', 'name'],
            data: data,
            sorters: [
                {
                    property: 'name',
                    direction: 'ASC'
                }
            ]
        });
    },

    taskTypeChanged: function (field, newValue) {
        var me = this;

        if (field.suspendComboChange) {
            return;
        }

        Ext.suspendLayouts();
        me.getAddPage().down('#add-button').setDisabled(false);
        me.getAddPage().down('#task-management-attributes').removeAll();
        Apr.TaskManagementApp.getTaskManagementApps().get(newValue).controller.getTaskForm(this, function (form) {
            me.getAddPage().down('#task-management-attributes').add(form);
            Ext.resumeLayouts(true);
        });
    },

    addTask: function (button) {
        var me = this,
            taskType = me.getAddPage().down('#task-management-task-type').getValue();

        var saveStatus = Apr.TaskManagementApp.getTaskManagementApps().get(taskType).controller.saveTaskForm(
            me.getAddPage().down('#task-management-attributes'),
            me.getAddPage().down('#form-errors'),
            me.saveOperationComplete,
            this
        );
    },

    /* edit task section */
    editTask: function (taskType, taskId) {
        var me = this,
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType),
            appName = Uni.util.Application.getAppName(),
            view = Ext.create('Apr.view.taskmanagement.Add', {
                edit: true,
                addReturnLink: me.rootRouteWithArguments,
                storeTypes: me.getTypesStore()
            });

        view.down('#task-management-task-type').setDisabled(true);
        view.down('#add-button').setDisabled(false);
        view.down('#task-management-task-type').suspendComboChange = true;
        view.down('#task-management-task-type').setValue(taskType);
        view.down('#task-management-task-type').suspendComboChange = false;
        view.down('#task-management-attributes').removeAll();
        Apr.TaskManagementApp.getTaskManagementApps().get(taskType).controller.getTaskForm(this, function (form) {
            view.down('#task-management-attributes').add(form);
            me.getApplication().fireEvent('changecontentevent', view);
            taskManagement.controller.editTaskManagement(taskId, view.down('#form-errors'),
                me.editOperationStart, me.editOperationCompleteLoading, me.editOperationCompleted, me.editSetTitle, this);
        });
    },

    editSetTitle: function (taskName) {
        var me = this;

        me.getAddPage().down('#frm-add-task').setTitle(Uni.I18n.translate('general.editx', 'APR', "Edit '{0}'", taskName, false));
        me.getApplication().fireEvent('loadTask', taskName);
    },

    editOperationStart: function () {
        this.getAddPage().setLoading(true);
    },

    editOperationCompleteLoading: function () {
        this.getAddPage().setLoading(false);
    },

    editOperationCompleted: function (status) {
        var me = this;
        me.getController('Uni.controller.history.Router').getRoute(me.rootRoute).forward(null, me.rootRouteArguments);
    },

    /* suspend task section */
    suspendTaskManagement: function (record, operationStartFunc, operationCompletedFunc, controller)  {
        var me = this,
            suspendedDataTime;

        if (record.get('suspended') == 'suspended') {
            suspendedDataTime = new Date(record.get('suspendedDataTime'));
        }
        else {
            if (record.get('nextRunTimeStamp') == 0){
                var tomorrowMidnight = new Date();
                tomorrowMidnight.setHours(24, 0, 0, 1);
                suspendedDataTime = tomorrowMidnight;
            }
            else {
                suspendedDataTime = new Date(record.get('nextRunTimeStamp'));
            }
        }

        confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            itemId: 'snooze-snoozeConfirmationWindow',
            confirmText: Uni.I18n.translate('general.suspend', 'APR', 'Suspend'),
            closeAction: 'destroy',
            green: true,
            confirmation: function () {
                me.suspendTask(record, operationStartFunc, operationCompletedFunc, controller, this);
            }
        })
        ;

        confirmationWindow.insert(1, {
            xtype: 'snooze-date',
            itemId: 'issue-sel-snooze-run',
            defaultDate: suspendedDataTime,
            padding: '-10 0 0 45'
        });
        confirmationWindow.insert(2, {
            xtype: 'label',
            margin: '0 0 10 50',  // add "It willl be suspended" in suspended dialog
            text: Uni.I18n.translate('general.suspend.text', 'APR', 'The next run will start after suspended period'),
        });
        confirmationWindow.insert(3, {
            itemId: 'snooze-now-window-errors',
            xtype: 'label',
            margin: '0 0 10 50',
            hidden: true
        });
        confirmationWindow.show({
            title: Uni.I18n.translate('general.suspendNow', 'APR', "Suspend '{0}'?",
                record.getData().name, false)
        });
    },

    suspendTask : function(recordTask, operationStartSuspend, operationCompletedSuspend, controller, confWindow){
        var me = this;
        operationStartSuspend.call(controller);

        var suspendTime = confWindow.down('#issue-snooze-until-date').getValue().getTime();

        Ext.Ajax.request({
            url: '/api/tsk/task/tasks/' + recordTask.get('id') + '/suspend/' + suspendTime,
            method: 'POST',

            success: function (operation) {
                console.log('success');
                var response = Ext.JSON.decode(operation.responseText);
                recordTask.set('lastRunDate',response.lastRunDate);
                recordTask.set('suspendUntilTime', response.suspendUntilTime);
                recordTask.set('queueStatusDate', response.queueStatusDate);
                recordTask.set('queueStatus', response.queueStatus);
                recordTask.set('nextRun',response.nextRun);
                recordTask.set('nextRunTimeStamp',response.nextRun);
                recordTask.set('queueStatusString', '');
                recordTask.commit();
                operationCompletedSuspend.call(controller, true);

                confWindow.destroy();
            },

            failure: function (response) {
                operationCompletedSuspend.call(controller, false);
                if (response.status === 400) {
                    var res = Ext.JSON.decode(response.responseText);
                    confWindow.update(res.errors[0].msg);
                    confWindow.setVisible(true);
                }
                else {
                    confWindow.destroy();
                }
            }
        });
    },
    suspendOperationStart:function() {
        var me = this;
        me.getPage() && me.getPage().setLoading(true);
    },

    suspendOperationCompleted:function(succeeded) {
        var me = this;
        me.getPage() && me.getPage().setLoading(false);
        var grid = me.getTaskManagementGrid();
        if(grid){
            var selection = grid.getSelectionModel().getSelection(); // get current item selected
            grid.getSelectionModel().deselectAll();  // deselect all items
            grid.getSelectionModel().select(selection); // select current item selected, again.
            // This will force refresh of details, needed for "Suspended" detail
        }
        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('suspendExportTask.successMsg', 'APR', 'Export task suspended'));
    },

   setQueueAndPriority: function (record) {
            var me = this,
            storeTaskType = Ext.getStore('Apr.store.TasksType');
            storeTaskType.getProxy().setUrl(record.getId());
        storeTaskType.load(function(records, operation, success) {
            if (success) {
                var window = Ext.widget('queue-priority-window-management', {
                    record: record,
                    storeTaskType: storeTaskType
                });
                window.show();
            };
        });
    },
   saveQueuePriority: function() {
        var me = this,
            window = me.getQueuePriorityWindow(),
            record = window.record,
            taskId = record.getId(),
            priority = window.down('#priority-field').getValue(),
            queue = window.down('#queue-field').getValue(),
            updatedData;

        updatedData = {
            id: taskId,
            queue: queue,
            priority: priority
        };

        Ext.Ajax.request({
            url: '/api/tsk/task',
            method: 'PUT',
            jsonData: Ext.encode(updatedData),
            success: function (response) {
            record.set({
                'queue': queue,
                'priority': priority
            });
                window.close();
                me.getApplication().fireEvent('acknowledge', 'Task queue and priority changed.');
                me.getTaskPreview().down('form').loadRecord(record);
            },
        });
   },

    /* common section */
    saveOperationComplete: function () {
        var me = this;
        me.getController('Uni.controller.history.Router').getRoute(me.rootRoute).forward(null, me.rootRouteArguments);
    },

    operationStart: function () {
        var me = this;
        me.getPage() && me.getPage().setLoading(true);
    },

    operationCompleted: function (status) {
        var me = this;

        me.getPage() && me.getPage().setLoading(false);
        me.removeOperationCompleted(status);

    },

    removeOperationStart: function () {
    },

    removeOperationCompleted: function () {
        var me = this;

        if (status && me.getPage()) {
            var grid = me.getTaskManagementGrid();
            grid.down('pagingtoolbartop').totalCount = 0;
            grid.down('pagingtoolbarbottom').resetPaging();
            grid.getStore().load();
        } else {
            me.getController('Uni.controller.history.Router').getRoute('administration/taskmanagement').forward();
        }
    },

    /* view history task section */
    viewHistoryTaskLoaded: function (taskController, taskManagementId, task) {
        var me = this,
            route = me.getController('Uni.controller.history.Router').getRoute('administration/taskmanagement/view/history');

        route.forward({type: taskController.getType(), taskManagementId: taskManagementId, taskId: task.get('id')});
    },

    viewHistoryTask: function (taskType, taskManagementId, taskId) {
        var me = this,
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        taskManagement.controller.detailRoute = 'administration/taskmanagement/view';
        taskManagement.controller.historyRoute = 'administration/taskmanagement/view/history';
        taskManagement.controller.viewLogRoute = 'administration/taskmanagement/view/history/occurrence';
        taskManagement.controller.historyTaskManagement(taskId);
    },

    /* view history task section */
    viewHistoryTaskLog: function (taskType, taskManagementId, taskId, occurrenceId) {
        var me = this,
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        taskManagement.controller.detailLogRoute = 'administration/taskmanagement/view';
        taskManagement.controller.logRoute = 'administration/taskmanagement/view/history/occurrence';
        taskManagement.controller.historyLogTaskManagement(taskId, occurrenceId);
    },


    /* view task section*/
    viewDetailsTaskLoaded: function (taskController, taskManagementId, task) {
        var me = this,
            route = me.getController('Uni.controller.history.Router').getRoute('administration/taskmanagement/view/edit');

        route.forward({type: taskController.getType(), taskManagementId: taskManagementId, taskId: task.get('id')});
    },

    viewTask: function (taskType, taskManagementId, taskId) {
        var me = this,
            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

        if (!taskManagement && Apr.TaskManagementApp.dependencesCounter != 0) {
            var route = me.getController('Uni.controller.history.Router');
            window.location.replace(route.getRoute().buildUrl(route.arguments));
            return;
        }
        me.getModel('Apr.model.Task').load(taskManagementId, {
            success: function (record) {
                taskManagement.controller.detailRoute = 'administration/taskmanagement/view';
                taskManagement.controller.historyRoute = 'administration/taskmanagement/view/history';
                taskManagement.controller.viewTaskManagement(taskId, {
                    xtype: 'task-management-action-menu',
                    itemId: 'task-management-action-menu'
                }, record);
            }
        });
    }
});
