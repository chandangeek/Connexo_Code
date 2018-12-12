/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.controller.CustomTask', {
    extend: 'Ext.app.Controller',

    views: [
        'Apr.view.customtask.AddCustomTask',
        'Apr.view.customtask.Details',
        'Apr.view.customtask.History',
        'Apr.view.customtask.HistoryPreview',
        'Apr.view.customtask.HistoryPreviewForm',
        'Apr.view.customtask.log.Setup',
        'Uni.form.field.DateTime'
    ],
    stores: [
        'Apr.store.AllTasks',
        'Apr.store.CustomTasks',
        'Apr.store.CustomTaskHistory',
        'Apr.store.DaysWeeksMonths',
        'Apr.store.CustomTaskTypes',
        'Apr.store.Logs',
        'Apr.store.TaskProperties',
        'Apr.store.Status'
    ],
    models: [
        'Apr.model.CustomTask',
        'Apr.model.CustomTaskHistory',
        'Apr.model.HistoryFilter'
    ],
    refs: [
        {
            ref: 'addPage',
            selector: 'ctk-add-custom-task'
        },
        {
            ref: 'history',
            selector: 'ctk-custom-task-history'
        },
        {
            ref: 'sideFilterForm',
            selector: '#side-filter #filter-form'
        },
        {
            ref: 'detailsPage',
            selector: 'ctk-task-details'
        },
        {
            ref: 'history',
            selector: 'ctk-custom-task-history'
        }
    ],

    init: function () {
        this.control({
            'ctk-custom-task-history #ctk-tasks-history-grid': {
                select: this.showHistoryPreview
            },
            'ctk-add-custom-task #ctk-recurrence-trigger': {
                change: this.onRecurrenceTriggerChange
            },
            'ctk-tasks-history-action-menu': {
                click: this.chooseAction
            }
        });

        this.loadCustomTasksTypes();
    },

    ACTIONS: {
        ADMINISTRATE: 'action.admin',
        RUN: 'action.run',
        EDIT: 'action.edit',
        VIEW: 'action.view',
        VIEW_HISTORY: 'action.viewHistory',
        VIEW_HISTORY_LOG: 'action.viewHistoryLog'
    },

    historyActionItemId: 'cfg-tasks-history-action-menu-tsk',
    canAdministrate: function () {
        return Ext.Array.contains(this.taskRecord.get('actions'), this.ACTIONS.ADMINISTRATE);
    },

    canView: function () {
        return Ext.Array.contains(this.taskRecord.get('actions'), this.ACTIONS.VIEW);
    },

    canRun: function () {
        return Ext.Array.contains(this.taskRecord.get('actions'), this.ACTIONS.RUN);
    },

    canEdit: function () {
        return Ext.Array.contains(this.taskRecord.get('actions'), this.ACTIONS.EDIT);
    },

    canSetTriggers: function () {
        return Ext.Array.contains(this.taskRecord.get('actions'), this.ACTIONS.ADMINISTRATE);
    },

    canHistory: function () {
        return Ext.Array.contains(this.taskRecord.get('actions'), this.ACTIONS.VIEW_HISTORY);
    },

    canRemove: function () {
        return Ext.Array.contains(this.taskRecord.get('actions'), this.ACTIONS.ADMINISTRATE);
    },

    canHistoryLog: function () {
        return Ext.Array.contains(this.taskRecord.get('actions'), this.ACTIONS.VIEW_HISTORY_LOG);
    },

    getType: function () {
        return this.taskType;
    },

    getTaskForm: function (caller, completedFunc) {
        var me = this,
            customTaskTypes = me.getStore('Apr.store.CustomTaskTypes'),
            view = Ext.create('Apr.view.customtask.AddCustomTask',
                {
                    edit: false
                }),
            followByStore = view.down('#ctk-followedBy-combo').getStore(),
            recurrenceTypeCombo = view.down('#cbo-recurrence-type');


        // refresh custom task types
        customTaskTypes.load({
            callback: function (records, operation, success) {
                Ext.Array.each(records, function (record) {
                    var taskType = record.get('name'),
                        taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

                    if (taskManagement) {
                        taskManagement.controller.taskRecord = record;
                    }
                    else {
                        var controller = Ext.create('Apr.controller.CustomTask', {
                            application: me.application,
                            views: me.views
                        });
                        controller.taskType = record.get('name');
                        controller.taskRecord = record;
                        Apr.TaskManagementApp.addTaskManagementApp(record.get('name'), {
                            name: record.get('displayName'),
                            controller: controller
                        });
                    }
                });

                if (me.taskRecord && me.taskRecord.properties && me.taskRecord.properties().count() > 0) {
                    view.down('#ctk-no-properties').setVisible(false);
                    Ext.Array.each(me.taskRecord.properties(), function (property) {
                        var propertiesContainer = view.down('#ctk-properties');
                        property.each(function (record) {
                                var propertyForm = Ext.create('Uni.property.form.Property', {
                                    itemId: 'ctk-group-' + record.get('name'),
                                    title: record.get('displayName'),
                                    defaults: {
                                        labelWidth: 235,
                                        width: 335
                                    },
                                    ui: 'medium'
                                });
                                if (record && record.properties() && record.properties().count()) {
                                    propertyForm.loadRecord(record);
                                    propertyForm.show();
                                } else {
                                    propertyForm.hide();
                                }

                                propertiesContainer.add(propertyForm);
                            }
                        );
                    });
                }
                else {
                    view.down('#ctk-no-properties').setVisible(true);
                }
                view.doLayout();
                followByStore.load({
                    callback: function () {
                        view.down('#ctk-loglevel').setValue(900);
                        me.recurrenceEnableDisable(view);
                        completedFunc.call(caller, view);
                    }
                });
            }
        });

        return view;
    },

    saveTaskForm: function (panel, formErrorsPanel, saveOperationComplete, controller) {
        var me = this,
            form = panel.down('ctk-add-custom-task'),
            page = me.getAddPage(),
            lastDayOfMonth = false,
            startOnDate,
            timeUnitValue,
            dayOfMonth,
            hours,
            minutes;

        var record = page.getRecord() || Ext.create('Apr.model.CustomTask');

        form.getForm().clearInvalid();
        record.beginEdit();
        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
        }

        record.set('name', form.down('#ctk-task-name').getValue());
        record.set('type', me.taskType);
        record.set('logLevel', form.down('#ctk-loglevel').getValue());
        startOnDate = moment(form.down('#start-on').getValue()).valueOf();
        if (form.down('#ctk-recurrence-trigger').getValue().recurrence) {
            timeUnitValue = form.down('#cbo-recurrence-type').getValue();
            dayOfMonth = moment(startOnDate).date();
            if (dayOfMonth >= 29) {
                lastDayOfMonth = true;
            }
            hours = form.down('#date-time-field-hours').getValue();
            minutes = form.down('#date-time-field-minutes').getValue();
            switch (timeUnitValue) {
                case 'years':
                    record.set('schedule', {
                        count: form.down('#ctk-num-recurrence-number').getValue(),
                        timeUnit: timeUnitValue,
                        offsetMonths: moment(startOnDate).month() + 1,
                        offsetDays: dayOfMonth,
                        lastDayOfMonth: lastDayOfMonth,
                        dayOfWeek: null,
                        offsetHours: hours,
                        offsetMinutes: minutes,
                        offsetSeconds: 0
                    });
                    break;
                case 'months':
                    record.set('schedule', {
                        count: form.down('#ctk-num-recurrence-number').getValue(),
                        timeUnit: timeUnitValue,
                        offsetMonths: 0,
                        offsetDays: dayOfMonth,
                        lastDayOfMonth: lastDayOfMonth,
                        dayOfWeek: null,
                        offsetHours: hours,
                        offsetMinutes: minutes,
                        offsetSeconds: 0
                    });
                    break;
                case 'weeks':
                    record.set('schedule', {
                        count: form.down('#ctk-num-recurrence-number').getValue(),
                        timeUnit: timeUnitValue,
                        offsetMonths: 0,
                        offsetDays: 0,
                        lastDayOfMonth: lastDayOfMonth,
                        dayOfWeek: moment(startOnDate).format('dddd').toUpperCase(),
                        offsetHours: hours,
                        offsetMinutes: minutes,
                        offsetSeconds: 0
                    });
                    break;
                case 'days':
                    record.set('schedule', {
                        count: form.down('#ctk-num-recurrence-number').getValue(),
                        timeUnit: timeUnitValue,
                        offsetMonths: 0,
                        offsetDays: 0,
                        lastDayOfMonth: lastDayOfMonth,
                        dayOfWeek: null,
                        offsetHours: hours,
                        offsetMinutes: minutes,
                        offsetSeconds: 0
                    });
                    break;
                case 'hours':
                    record.set('schedule', {
                        count: form.down('#ctk-num-recurrence-number').getValue(),
                        timeUnit: timeUnitValue,
                        offsetMonths: 0,
                        offsetDays: 0,
                        lastDayOfMonth: lastDayOfMonth,
                        dayOfWeek: null,
                        offsetHours: hours,
                        offsetMinutes: minutes,
                        offsetSeconds: 0
                    });
                    break;
                case 'minutes':
                    record.set('schedule', {
                        count: form.down('#ctk-num-recurrence-number').getValue(),
                        timeUnit: timeUnitValue,
                        offsetMonths: 0,
                        offsetDays: 0,
                        lastDayOfMonth: lastDayOfMonth,
                        dayOfWeek: null,
                        offsetHours: hours,
                        offsetMinutes: minutes,
                        offsetSeconds: 0
                    });
                    break;
            }
        } else {
            record.set('schedule', null);
        }
        record.set('nextRun', startOnDate);

        // set selected tasks
        var selectedTask = [];
        Ext.Array.each(form.down('#ctk-followedBy-combo').getValue(), function (value) {
            selectedTask.push({id: value});
        })
        record.nextRecurrentTasksStore = Ext.create('Ext.data.Store', {
            fields: ['id'],
            data: selectedTask
        });

        //save custom parameters
        if (record.hasId() == false) {
            this.taskRecord.properties().each(function (rec) {
                record.properties().add(rec);
            });
        }
        if (record.properties && record.properties().count() > 0) {
            //view.down('#ctk-no-properties').setVisible(false);
            Ext.Array.each(record.properties(), function (property) {
                //var propertiesContainer = view.down('#ctk-properties');
                property.each(function (propertyRecord) {

                        form.down('#ctk-group-' + propertyRecord.get('name')).updateRecord();
                        propertyRecord.beginEdit();
                        propertyRecord.propertiesStore = form.down('#ctk-group-' + propertyRecord.get('name')).getRecord().properties();
                        propertyRecord.endEdit();
                    }
                );
            });
        }


        page.setLoading(true);
        record.endEdit();
        record.save({
            success: function (record, operation) {
                saveOperationComplete.call(controller);
                switch (operation.action) {
                    case 'update':
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('customTask.successMsg.saved', 'APR', 'Task saved'));
                        break;
                    case 'create':
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('customTask.successMsg.added', 'APR', 'Task added'));
                        break;
                }
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            },
            callback: function () {
                page.setLoading(false);
            }
        })
    },

    runTaskManagement: function (taskManagement, operationStartFunc, operationCompletedFunc, controller) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('customTask.general.run', 'APR', 'Run'),
                confirmation: function () {
                    me.submitRunTask(taskManagement, operationStartFunc, operationCompletedFunc, controller, this);
                }
            });

        confirmationWindow.insert(1,
            {
                xtype: 'panel',
                itemId: 'date-errors',
                hidden: true,
                cls: 'confirmation-window',
                html: 'sssss'
            }
        );

        confirmationWindow.show({
            msg: Uni.I18n.translate('customTask.runMsg', 'APR', 'This task will be queued to run at the earliest possible time.'),
            title: Uni.I18n.translate('customTask.runTask', 'APR', "Run task '{0}'?", [taskManagement.get('name')])
        });
    },

    submitRunTask: function (taskManagement, operationStartFunc, operationCompletedFunc, controller, confWindow) {
        var me = this;

        operationStartFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/ctk/customtask/recurrenttask/' + taskManagement.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Apr.store.CustomTasks');

                store.loadRawData([response]);
                store.each(function (record) {
                    Ext.Ajax.request({
                        url: '/api/ctk/customtask/' + record.get('id') + '/trigger',
                        method: 'PUT',
                        jsonData: record.getProxy().getWriter().getRecordData(record),
                        isNotEdit: true,
                        success: function () {
                            confWindow.destroy();
                            operationCompletedFunc.call(controller, true);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('customTask.run', 'APR', 'Data task run queued'));
                        },
                        failure: function (response) {
                            operationCompletedFunc.call(controller, false);
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
                });
            }
        })
    },

    editTaskManagement: function (taskManagementId, formErrorsPanel,
                                  operationStartFunc, editOperationCompleteLoading,
                                  operationCompletedFunc, setTitleFunc, controller) {
        var me = this,
            appName = Uni.util.Application.getAppName(),
            taskForm = me.getAddPage();

        operationStartFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/ctk/customtask/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Apr.store.CustomTasks');

                store.loadRawData([response]);
                store.each(function (record) {
                        setTitleFunc.call(controller, record.get('name'));

                        var schedule = record.get('schedule'),
                            recurrenceTypeCombo,
                            callback = function () {
                                recurrenceTypeCombo = taskForm.down('#cbo-recurrence-type');

                                Ext.suspendLayouts();
                                taskForm.loadRecord(record);

                                var nextRecurrentTasks = record.get('nextRecurrentTasks');
                                if (nextRecurrentTasks) {
                                    var selectedTasks = [];
                                    Ext.Array.each(nextRecurrentTasks, function (nextRecurrentTask) {
                                        selectedTasks.push(nextRecurrentTask.id);
                                    });
                                    taskForm.down('[name=nextRecurrentTasks]').setValue(selectedTasks);
                                }

                                if (record.properties && record.properties().count() > 0) {
                                    taskForm.down('#ctk-no-properties').setVisible(false);
                                    taskForm.down('#ctk-properties').removeAll(true);

                                    Ext.Array.each(record.properties(), function (property) {
                                        var propertiesContainer = taskForm.down('#ctk-properties');
                                        property.each(function (propertyRecord) {
                                                var propertyForm = Ext.create('Uni.property.form.Property', {
                                                    itemId: 'ctk-group-' + propertyRecord.get('name'),
                                                    title: propertyRecord.get('displayName'),
                                                    defaults: {
                                                        labelWidth: 235,
                                                        width: 335
                                                    },
                                                    ui: 'medium'
                                                });
                                                if (propertyRecord && propertyRecord.properties() && propertyRecord.properties().count()) {
                                                    propertyForm.loadRecord(propertyRecord);
                                                    propertyForm.show();
                                                } else {
                                                    propertyForm.hide();
                                                }

                                                propertiesContainer.add(propertyForm);
                                            }
                                        );
                                    });
                                }
                                else {
                                    taskForm.down('#ctk-no-properties').setVisible(true);
                                }
                                taskForm.doLayout();

                                if (record.data.nextRun && (record.data.nextRun !== 0)) {
                                    taskForm.down('#start-on').setValue(record.data.nextRun);
                                }

                                if (schedule) {
                                    taskForm.down('#ctk-recurrence-trigger').setValue({recurrence: true});
                                    taskForm.down('#ctk-num-recurrence-number').setValue(schedule.count);
                                    recurrenceTypeCombo.setValue(schedule.timeUnit);
                                } else {
                                    recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(2));
                                }

                                Ext.resumeLayouts(true);
                            };
                        if (taskForm.rendered) {
                            callback();
                        }
                        editOperationCompleteLoading.call(controller)
                    }
                );
            }
        })
    },

    historyTaskManagement: function (taskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Apr.store.CustomTaskHistory'),
            taskModel = me.getModel('Apr.model.CustomTask'),
            view;

        store.getProxy().setUrl(router.arguments);

        view = Ext.widget('ctk-custom-task-history', {
            router: router,
            taskId: taskId,
            objectType: me.taskRecord.get('displayName'),
            canHistory: me.canHistory(),
            canHistoryLog: me.canHistoryLog(),
            detailRoute: 'administration/taskmanagement/view',
            historyRoute: 'administration/taskmanagement/view/history',
            viewLogRoute: 'administration/taskmanagement/view/history/occurrence'
        });

        taskModel.load(taskId, {
            success: function (record) {
                view.record = record;
                view.down('#tasks-view-menu').setHeader(record.get('name'));
                me.getApplication().fireEvent('loadTask', record);
                me.getApplication().fireEvent('changecontentevent', view);
                store.load();
            }
        });
    },

    historyLogTaskManagement: function (taskId, occurrenceId) {
        var me = this,
            taskModel = me.getModel('Apr.model.CustomTaskHistory'),
            logsStore = me.getStore('Apr.store.Logs'),
            router = me.getController('Uni.controller.history.Router'),
            view,
            runStartedOnFormatted,
            taskLink;


        logsStore.getProxy().setUrl(router.arguments);
        taskModel.load(occurrenceId, {
            success: function (occurrenceTask) {
                var task = occurrenceTask.getTask();
                runStartedOnFormatted = occurrenceTask.data.startedOn_formatted;
                view = Ext.widget('ctk-log-setup', {
                    router: router,
                    task: task,
                    runStartedOn: runStartedOnFormatted,
                    detailRoute: 'administration/taskmanagement/view',
                    objectType: me.taskRecord.get('displayName')
                });
                Ext.suspendLayouts();

                me.getApplication().fireEvent('loadTask', occurrenceTask);
                me.getApplication().fireEvent('viewHistoryTaskLog', Uni.I18n.translate('customTask.log', 'APR', 'Log'));


                view.down('#log-preview-form').loadRecord(occurrenceTask);
                //????????????????????
                view.down('#reason-field').setVisible(occurrenceTask.get('statusType') === 'FAILED');
                view.down('#run-started-on').setValue(runStartedOnFormatted);
                //view.down('#name-field').setFieldLabel(me.taskRecord.get('displayName'));
                view.down('#ctk-log-preview').updateSummary(occurrenceTask.get('summary'));
                me.getApplication().fireEvent('changecontentevent', view);

                Ext.resumeLayouts(true);
                view.down('#log-view-menu').setHeader(occurrenceTask.get('name'));
            }
        });
    },

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/ctk/customtask/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Apr.store.CustomTasks');
                store.loadRawData([response]);
                store.each(function (record) {
                    operationCompleted.call(controller, me, taskManagementId, record);
                });
            }
        })
    },

    removeTaskManagement: function (taskManagement, startRemovingFunc, removeCompleted, controller) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show({
            msg: Uni.I18n.translate('customTask.general.remove.msg', 'APR', 'This task will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'APR', "Remove '{0}'?", [taskManagement.get('name')]),
            config: {},
            fn: function (state) {
                if (state === 'confirm') {
                    me.removeOperation(taskManagement, startRemovingFunc, removeCompleted, controller);
                } else if (state === 'cancel') {
                    this.close();
                }
            }
        });
    },

    removeOperation: function (taskManagement, startRemovingFunc, removeCompleted, controller) {
        var me = this;

        startRemovingFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/ctk/customtask/recurrenttask/' + taskManagement.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Apr.store.CustomTasks');
                store.loadRawData([response]);
                store.each(function (record) {
                    record.destroy({
                        success: function () {
                            removeCompleted.call(controller, true);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('customTask.general.remove.confirm.msg', 'APR', 'Task removed'));
                        },
                        failure: function (record, operation) {
                            removeCompleted.call(controller, false);
                            if (operation.response.status === 409) {
                                return;
                            }
                        }
                    });
                });
            }
        })
    },

    viewTaskManagement: function (taskId, actionMenu, taskManagementRecord) {
        var me = this,
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Apr.model.CustomTask'),
            view = Ext.widget('ctk-task-details', {
                actionMenu: actionMenu,
                canAdministrate: me.canAdministrate(),
                canHistory: me.canHistory(),
                router: router,
                taskId: taskId,
                objectType: me.taskRecord.get('displayName'),
                detailRoute: 'administration/taskmanagement/view',
                historyRoute: 'administration/taskmanagement/view/history'
            });

        pageMainContent.setLoading(true);

        Ext.Ajax.request({
            url: '/api/ctk/customtask/recurrenttask/' + taskManagementRecord.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Apr.store.CustomTasks');
                store.loadRawData([response]);
                store.each(function (record) {
                    var detailsForm = view.down('ctk-task-preview-form');
                    me.getApplication().fireEvent('changecontentevent', view);

                    me.getApplication().fireEvent('loadTask', record);
                    detailsForm.loadRecord(record);
                    view.down('#tasks-view-menu').setHeader(record.get('name'));
                    detailsForm.setRecurrentTasks('#followedBy-field-container', record.get('nextRecurrentTasks'));
                    detailsForm.setRecurrentTasks('#precededBy-field-container', record.get('previousRecurrentTasks'));
                    view.down('#' + actionMenu.itemId) && (view.down('#' + actionMenu.itemId).record = taskManagementRecord);

                    //??????????????????????????????????????????????????
                    //if (record.get('status') !== 'Busy') {
                    //    if (record.get('status') === 'Failed') {
                    //        view.down('#lbl-reason-field').show();
                    //    }
                    // if (Cfg.privileges.Validation.canRun()) {
                    //       view.down('#run-task') && view.down('#run-task').show();
                    //   }
                    // }

                    var taskForm = detailsForm;
                    if (record.properties && record.properties().count() > 0) {
                        taskForm.down('#ctk-no-properties').setVisible(false);
                        taskForm.down('#ctk-properties').removeAll(true);

                        Ext.Array.each(record.properties(), function (property) {
                            var propertiesContainer = taskForm.down('#ctk-properties');
                            property.each(function (propertyRecord) {
                                var propertyForm = Ext.create('Uni.property.form.Property', {
                                    itemId: 'ctk-group-' + propertyRecord.get('name'),
                                    //title: propertyRecord.get('displayName'),
                                    defaults: {
                                        resetButtonHidden: true,
                                        labelWidth: 250,
                                        width: 500
                                    },
                                    isEdit: false,
                                    isReadOnly: true
                                });
                                if (propertyRecord && propertyRecord.properties() && propertyRecord.properties().count()) {
                                    propertyForm.loadRecord(propertyRecord);
                                    propertyForm.show();
                                } else {
                                    propertyForm.hide();
                                }

                                var fieldContainer = Ext.create('Ext.form.FieldContainer', {
                                    fieldLabel: propertyRecord.get('displayName'),
                                    labelAlign: 'top',
                                    layout: 'vbox',
                                    items: propertyForm
                                });


                                propertiesContainer.add(fieldContainer);
                            });
                        });
                    }
                    else {
                        taskForm.down('#ctk-no-properties').setVisible(true);
                    }
                });
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    },

    recurrenceEnableDisable: function () {
        var me = this,
            page = me.getAddPage();
        if (!page.down('#ctk-recurrence-trigger').getValue().recurrence) {
            page.down('#ctk-recurrence-values').disable();
        } else {
            page.down('#ctk-recurrence-values').enable();
        }
    },

    loadCustomTasksTypes: function () {
        var me = this,
            customTaskTypes = me.getStore('Apr.store.CustomTaskTypes');

        Apr.TaskManagementApp.increaseDependency();
        customTaskTypes.load({
            callback: function (records, operation, success) {
                Ext.Array.each(records, function (record) {
                    var controller = Ext.create('Apr.controller.CustomTask', {
                        application: me.application,
                        views: me.views
                    });
                    controller.taskType = record.get('name');
                    controller.taskRecord = record;
                    Apr.TaskManagementApp.addTaskManagementApp(record.get('name'), {
                        name: record.get('displayName'),
                        controller: controller
                    });
                });
                Apr.TaskManagementApp.reduceDependency();
                var route = me.getController('Uni.controller.history.Router');
                route.fireEvent('dependenciesLoaded', route);

            }
        });

    },

    showHistoryPreview: function (selectionModel, record) {
        var me = this,
            page = me.getHistory(),
            preview = page.down('ctk-tasks-history-preview'),
            previewForm = page.down('ctk-tasks-history-preview-form');

        if (record) {
            Ext.suspendLayouts();
            preview.setTitle(record.get('startedOn_formatted'));

            previewForm.loadRecord(record);
            preview.down('ctk-tasks-history-action-menu') && (preview.down('ctk-tasks-history-action-menu').record = record);

            //???????????????????????????????
            if (record.get('statusType') === 'FAILED') {
                previewForm.down('#lbl-reason-field').show();
            } else {
                previewForm.down('#lbl-reason-field').hide();
            }

            previewForm.loadRecord(record);

            var taskForm = previewForm;
            if (record.getTask().properties && record.getTask().properties().count() > 0) {
                taskForm.down('#ctk-no-properties').setVisible(false);
                taskForm.down('#ctk-properties').removeAll(true);

                Ext.Array.each(record.getTask().properties(), function (property) {
                    var propertiesContainer = taskForm.down('#ctk-properties');

                    property.each(function (propertyRecord) {
                        var propertyForm = Ext.create('Uni.property.form.Property', {
                            itemId: 'ctk-group-' + propertyRecord.get('name'),
                            defaults: {
                                resetButtonHidden: true,
                                labelWidth: 250,
                                width: 500
                            },
                            isEdit: false,
                            isReadOnly: true
                        });
                        if (propertyRecord && propertyRecord.properties && propertyRecord.properties().count()) {
                            propertyForm.loadRecord(propertyRecord);
                            propertyForm.show();
                        } else {
                            propertyForm.hide();
                        }

                        var fieldContainer = Ext.create('Ext.form.FieldContainer', {
                            fieldLabel: propertyRecord.get('displayName'),
                            labelAlign: 'top',
                            layout: 'vbox',
                            items: propertyForm
                        });


                        propertiesContainer.add(fieldContainer);
                    });
                });
            }
            else {
                taskForm.down('#ctk-no-properties').setVisible(true);
            }
            Ext.resumeLayouts(true);
        }
    },

    onRecurrenceTriggerChange: function (field, newValue, oldValue) {
        var me = this,
            page = me.getAddPage(),
            recurrenceNumberField = page.down('#ctk-num-recurrence-number'),
            recurrenceTypeCombo = page.down('#cbo-recurrence-type');

        if (newValue.recurrence && !recurrenceNumberField.getValue()) {
            recurrenceNumberField.setValue(recurrenceNumberField.minValue);
        }
        if (newValue.recurrence && !recurrenceTypeCombo.getValue()) {
            recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(0));
        }
        me.recurrenceEnableDisable();
    },

    recurrenceEnableDisable: function () {
        var me = this,
            page = me.getAddPage();
        if (!page.down('#ctk-recurrence-trigger').getValue().recurrence) {
            page.down('#ctk-recurrence-values').disable();
        } else {
            page.down('#ctk-recurrence-values').enable();
        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route, arguments;

        switch (item.action) {
            case 'viewLog':
                route = 'administration/taskmanagement/view/history/occurrence';
                break;
        }

        arguments = router.arguments;
        arguments.occurrenceId = menu.record.getId();
        route && (route = router.getRoute(route));
        route && route.forward(arguments);
    }
});