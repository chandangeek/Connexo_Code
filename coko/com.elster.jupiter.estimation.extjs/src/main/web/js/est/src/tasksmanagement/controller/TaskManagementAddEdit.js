/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.tasksmanagement.controller.TaskManagementAddEdit', {
    extend: 'Est.estimationtasks.controller.EstimationTasksAddEdit',

    requires: [
        'Uni.util.Application'
    ],
    stores: [
        'Est.tasksmanagement.store.AllTasks'
    ],
    views: [
        'Est.tasksmanagement.view.AddEdit'
    ],

    refs: [
        {ref: 'addEditEstimationtaskPage', selector: 'taskmanagement-addedit'},
        {ref: 'addEditEstimationtaskForm', selector: 'taskmanagement-addedit'},
        {ref: 'deviceGroupCombo', selector: '#device-group-combo'},
        {ref: 'estimationPeriodCombo', selector: '#estimationPeriod-id'},
        {ref: 'noDeviceGroupBlock', selector: '#no-device'},
        {ref: 'recurrenceTypeCombo', selector: '#recurrence-type'}
    ],

    init: function () {
        this.control({
            'taskmanagement-addedit #add-button': {
                click: this.createEstimationTask
            },
            'taskmanagement-addedit #recurrence-trigger': {
                change: this.recurrenceChange
            },
            'taskmanagement-addedit #reset-purpose-btn': {
                click: this.resetPurpose
            }
        });
        Apr.TaskManagementApp.addTaskManagementApp(this.getType(), {
            name: Uni.I18n.translate('general.dataEstimation', 'EST', 'Data estimation'),
            controller: this
        });
    },

    canAdministrate: function () {
        return Est.privileges.EstimationConfiguration.canAdministrate();
    },

    canView: function () {
        return Est.privileges.EstimationConfiguration.canViewOrAdministrate();
    },

    canRun: function () {
        return Est.privileges.EstimationConfiguration.canRun();
    },

    canEdit: function () {
        return Est.privileges.EstimationConfiguration.canUpdate();
    },

    canSetTriggers: function () {
        return Est.privileges.EstimationConfiguration.canUpdate();
    },

    canHistory: function () {
        return Est.privileges.EstimationConfiguration.canView();
    },

    canRemove: function () {
        return Est.privileges.EstimationConfiguration.canAdministrate();
    },

    getType: function () {
        return 'EstimationTask';
    },

    getTaskForm: function (caller, completedFunc) {
        var me = this,
            appName = Uni.util.Application.getAppName(),
            widget = Ext.create('Est.tasksmanagement.view.AddEdit',
                {
                    appName: appName
                }),
            followByStore = widget.down('#followedBy-combo').getStore(),
            dataSourcesContainer = widget.down('est-data-sources-container');

        Ext.suspendLayouts();
        followByStore.load({
            callback: function () {
                me.getEstimationPeriodCombo().store.load({
                    params: {
                        category: 'relativeperiod.category.estimation'
                    },
                    callback: function () {
                        dataSourcesContainer.loadGroupStore();
                        completedFunc.call(caller, widget);
                    }
                });
            }
        });

        me.getRecurrenceTypeCombo().setValue(me.getRecurrenceTypeCombo().store.getAt(2));
        widget.down('#est-tasks-add-loglevel').setValue(900); // = WARNING, the default value at creation time
        me.recurrenceEnableDisable();
        Ext.resumeLayouts(true);
        return widget;
    },

    saveTaskForm: function (panel, formErrorsPanel, saveOperationComplete, controller) {
        var me = this,
            appName = Uni.util.Application.getAppName(),
            newEstimationTaskDto = me.getAddEditEstimationtaskForm().getValues(),
            previousPath = me.getController('Uni.controller.history.EventBus').getPreviousPath();

        formErrorsPanel.hide();

        if (me.getAddEditEstimationtaskForm().isValid()) {
            var newEstimationTask = me.getAddEditEstimationtaskForm().getRecord() || Ext.create('Est.estimationtasks.model.EstimationTask');
            newEstimationTask.beginEdit();

            newEstimationTask.set('name', newEstimationTaskDto.name);
            newEstimationTask.set('logLevel', newEstimationTaskDto.logLevel);
            newEstimationTask.set('revalidate', newEstimationTaskDto.revalidate);
            newEstimationTask.set('application', appName);
            newEstimationTask.set('active', true);
            newEstimationTask.set('lastEstimationOccurrence', null);
            switch (appName) {
                case 'MultiSense':
                {
                    newEstimationTask.set('deviceGroup', {
                        id: me.getAddEditEstimationtaskForm().down('#device-group-combo').getValue(),
                        name: me.getAddEditEstimationtaskForm().down('#device-group-combo').getRawValue()
                    });
                }
                    break;
                case 'MdmApp':
                {
                    newEstimationTask.set('usagePointGroup', {
                        id: me.getAddEditEstimationtaskForm().down('#usagePoint-group-id').getValue(),
                        displayValue: me.getAddEditEstimationtaskForm().down('#usagePoint-group-id').getRawValue()
                    });
                    newEstimationTask.set('metrologyPurpose', {
                        id: me.getAddEditEstimationtaskForm().down('#cbo-estimation-task-purpose').getValue() || 0,
                        displayValue: me.getAddEditEstimationtaskForm().down('#cbo-estimation-task-purpose').getRawValue()
                    });
                }
                    break;
            }

            if (newEstimationTaskDto.recurrence) {
                var startOnDate = moment(newEstimationTaskDto.startOn).valueOf(),
                    timeUnitValue = newEstimationTaskDto.recurrenceType,
                    dayOfMonth = moment(startOnDate).date(),
                    lastDayOfMonth = dayOfMonth >= 29,
                    hours = me.getAddEditEstimationtaskForm().down('#date-time-field-hours').getValue(),
                    minutes = me.getAddEditEstimationtaskForm().down('#date-time-field-minutes').getValue();

                newEstimationTask.set('nextRun', startOnDate);

                switch (timeUnitValue) {
                    case 'years':
                        newEstimationTask.set('schedule', {
                            count: newEstimationTaskDto.recurrenceNumber,
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
                        newEstimationTask.set('schedule', {
                            count: newEstimationTaskDto.recurrenceNumber,
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
                        newEstimationTask.set('schedule', {
                            count: newEstimationTaskDto.recurrenceNumber,
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
                        newEstimationTask.set('schedule', {
                            count: newEstimationTaskDto.recurrenceNumber,
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
                        newEstimationTask.set('schedule', {
                            count: newEstimationTaskDto.recurrenceNumber,
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
                        newEstimationTask.set('schedule', {
                            count: newEstimationTaskDto.recurrenceNumber,
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
                var startOnDate_noRecurrence = moment(newEstimationTaskDto.startOn).valueOf();
                newEstimationTask.set('nextRun', startOnDate_noRecurrence);
                newEstimationTask.set('schedule', null);
            }

            if (newEstimationTaskDto.estimationPeriod) {
                newEstimationTask.set('period', {
                    id: newEstimationTaskDto.estimationPeriodId,
                    name: me.getAddEditEstimationtaskForm().down('#estimationPeriod-id').getRawValue()
                });
            } else {
                newEstimationTask.set('period', null);
            }
            // set selected tasks
            var selectedTask = [];
            Ext.Array.each(me.getAddEditEstimationtaskForm().down('#followedBy-combo').getValue(), function (value) {
                selectedTask.push({id: value});
            })
            newEstimationTask.nextRecurrentTasksStore = Ext.create('Ext.data.Store', {
                fields: ['id'],
                data: selectedTask
            });
            newEstimationTask.endEdit();

            me.getAddEditEstimationtaskPage().setLoading(true);

            newEstimationTask.save({
                // backUrl: previousPath
                //    ? '#' + previousPath
                //    : me.getController('Uni.controller.history.Router').getRoute('administration/estimationtasks/estimationtask').buildUrl({taskId: newEstimationTask.getId() ? newEstimationTask.getId() : 0}),
                success: function (record, operation) {
                    //me.getController('Uni.controller.history.Router').getRoute(me.getController('Uni.controller.history.Router').currentRoute.replace('/add', '')).forward();
                    saveOperationComplete.call(controller);
                    switch (operation.action) {
                        case 'update':
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationtasks.saveTask.successMsg', 'EST', 'Estimation task saved'));
                            break;
                        case 'create':
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationtasks.addTask.successMsg', 'EST', 'Estimation task added'));
                            break;
                    }
                    //if (button.action === 'editTask' && me.fromDetails) {
                    //    me.getController('Uni.controller.history.Router').getRoute('administration/estimationtasks/estimationtask').forward({taskId: newEstimationTask.getId() ? newEstimationTask.getId() : 0});
                    //} else {
                    //    me.getController('Uni.controller.history.Router').getRoute('administration/estimationtasks').forward();
                    //}
                    //if (button.action === 'editTask') {
                    //    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationtasks.saveTask.successMsg', 'EST', 'Estimation task saved'));
                    //} else {
                    //    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationtasks.addTask.successMsg', 'EST', 'Estimation task added'));
                    //}
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        Ext.each(json.errors, function (item) {
                            me.getAddEditEstimationtaskForm().down('[name=' + item.id + ']').setActiveError(item.msg);
                            formErrorsPanel.show();
                        })
                    }
                },
                callback: function () {
                    me.getAddEditEstimationtaskPage().setLoading(false);
                }
            })
        } else {
            formErrorsPanel.show();
        }
    },

    runTaskManagement: function (taskManagement, operationStartFunc, operationCompletedFunc, controller) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('estimationtasks.general.run', 'EST', 'Run'),
                confirmation: function () {
                    me.submitRunTask(taskManagement, operationStartFunc, operationCompletedFunc, controller, this);
                }
            });

        confirmationWindow.insert(1,
            {
                xtype: 'panel',
                itemId: 'date-errors',
                hidden: true,
                bodyStyle: {
                    color: '#eb5642',
                    padding: '0 0 15px 65px'
                },
                html: ''
            }
        );

        confirmationWindow.show({
            msg: Uni.I18n.translate('estimationtasks.general.runmsg', 'EST', 'The estimation task will be queued to run at the earliest possible time.'),
            title: Uni.I18n.translate('estimationtasks.general.runestimationtask', 'EST', "Run estimation task '{0}'?", [taskManagement.get('name')])
        });
    },

    submitRunTask: function (taskManagement, operationStartFunc, operationCompletedFunc, controller, confWindow) {
        var me = this;

        operationStartFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/est/estimation/recurrenttask/' + taskManagement.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.store.ValidationTasks');

                store.loadRawData([response]);
                store.each(function (record) {
                    Ext.Ajax.request({
                        url: '/api/est/estimation/tasks/' + record.get('id') + '/trigger',
                        method: 'PUT',
                        jsonData: record.getProxy().getWriter().getRecordData(record),
                        isNotEdit: true,
                        success: function () {
                            confWindow.destroy();
                            operationCompletedFunc.call(controller, true);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.run', 'EST', 'Data validation task run queued'));
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
            taskForm = me.getAddEditEstimationtaskForm();

        operationStartFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/est/estimation/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Est.estimationtasks.store.EstimationTasks');
                store.loadRawData([response]);
                store.each(function (record) {
                    setTitleFunc.call(controller, record.get('name'));
                    var
                        dataSourcesContainer = taskForm.down('est-data-sources-container'),
                        recurrenceTypeCombo = taskForm.down('#recurrence-type'),
                        schedule = record.get('schedule'),
                        period = record.get('period');

                    taskForm.loadRecord(record);

                    var nextRecurrentTasks = record.get('nextRecurrentTasks');
                    if (nextRecurrentTasks) {
                        var selectedTasks = [];
                        Ext.Array.each(nextRecurrentTasks, function (nextRecurrentTask) {
                            selectedTasks.push(nextRecurrentTask.id);
                        });
                        taskForm.down('[name=nextRecurrentTasks]').setValue(selectedTasks);
                    }

                    dataSourcesContainer.loadGroupStore(function () {
                        dataSourcesContainer.setComboValue(record);
                        me.getEstimationPeriodCombo().store.load(function () {
                            if (period && (period.id !== 0)) {
                                taskForm.down('#estimation-period-trigger').setValue({estimationPeriod: true});
                                me.getEstimationPeriodCombo().setValue(period.id);
                            }
                        });
                    });

                    if (schedule) {
                        taskForm.down('#recurrence-trigger').setValue({recurrence: true});
                        taskForm.down('#recurrence-number').setValue(schedule.count);
                        recurrenceTypeCombo.setValue(schedule.timeUnit);
                        taskForm.down('#start-on').setValue(record.get('nextRun'));
                    } else {
                        recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(2));
                    }

                    editOperationCompleteLoading.call(controller)
                });
            }
        })
    },

    historyTaskManagement: function (taskId) {
        var me = this,
            controllerActionMenu = me.getController('Est.estimationtasks.controller.EstimationTasksActionMenu'),
            controller = me.getController('Est.estimationtasks.controller.EstimationTasksHistory');


        controllerActionMenu.viewLogRoute = me.viewLogRoute;
        controller.detailRoute = me.detailRoute;
        controller.historyRoute = me.historyRoute;
        controller.showEstimationTaskHistory(taskId);
    },

    historyLogTaskManagement: function (taskId, occurrenceId, viewLogRoute) {
        var me = this,
            controller = me.getController('Est.estimationtasks.controller.EstimationTasksLog');

        controller.detailLogRoute = me.detailLogRoute;
        controller.logRoute = me.logRoute;
        controller.showLog(taskId, occurrenceId);
    },

    removeTaskManagement: function (taskManagement, startRemovingFunc, removeCompleted, controller) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show({
            msg: Uni.I18n.translate('estimationtasks.general.remove.msg', 'EST', 'This estimation task will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'EST', "Remove '{0}'?", [taskManagement.get('name')]),
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
            url: '/api/est/estimation/recurrenttask/' + taskManagement.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Est.estimationtasks.store.EstimationTasks');
                store.loadRawData([response]);
                store.each(function (rec) {
                    rec.destroy({
                        success: function () {
                            removeCompleted.call(controller, true);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationtasks.general.remove.confirm.msg', 'EST', 'Estimation task removed'));
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

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/est/estimation/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Est.estimationtasks.store.EstimationTasks');
                store.loadRawData([response]);
                store.each(function (record) {
                    operationCompleted.call(controller, me, taskManagementId, record);
                });
            }
        })
    },

    viewTaskManagement: function (taskId, actionMenu, taskManagementRecord) {
        var me = this,
            controller = me.getController('Est.estimationtasks.controller.EstimationTasksDetails');

        controller.detailRoute = me.detailRoute;
        controller.historyRoute = me.historyRoute;
        controller.actionMenu = actionMenu;
        controller.showEstimationTaskDetails(taskId);
        controller.getDetailsView().down('#' + actionMenu.itemId).record = taskManagementRecord;
    }
});
