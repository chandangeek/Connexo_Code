/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.controller.EstimationTasksAddEdit', {
    extend: 'Ext.app.Controller',

    requires: ['Uni.util.Application'],

    stores: [
        'Est.estimationtasks.store.DeviceGroups',
        'Est.estimationtasks.store.UsagePointGroups',
        'Est.estimationtasks.store.MetrologyPurposes',
        'Est.estimationtasks.store.DaysWeeksMonths'
    ],

    views: [
        'Est.estimationtasks.view.AddEdit'
    ],

    refs: [
        {ref: 'addEditEstimationtaskPage', selector: 'estimationtasks-addedit'},
        {ref: 'addEditEstimationtaskForm', selector: '#add-edit-estimationtask-form'},
        {ref: 'deviceGroupCombo', selector: '#device-group-combo'},
        {ref: 'estimationPeriodCombo', selector: '#estimationPeriod-id'},
        {ref: 'noDeviceGroupBlock', selector: '#no-device'},
        {ref: 'recurrenceTypeCombo', selector: '#recurrence-type'}
    ],

    fromDetails: null,
    taskModel: null,
    taskId: null,

    init: function () {
        this.control({
            'estimationtasks-addedit #add-button': {
                click: this.createEstimationTask
            },
            'estimationtasks-addedit #recurrence-trigger': {
                change: this.recurrenceChange
            },
            'estimationtasks-addedit #reset-purpose-btn': {
                click: this.resetPurpose
            }
        });
    },

    showAddEstimationTasksView: function () {
        var me = this,
            widget = Ext.widget('estimationtasks-addedit',{
                appName: Uni.util.Application.getAppName()
            }),
            dataSourcesContainer = widget.down('est-data-sources-container');
        Ext.suspendLayouts();
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getEstimationPeriodCombo().store.load({
            params: {
                category: 'relativeperiod.category.estimation'
            },
            callback: function () {
                dataSourcesContainer.loadGroupStore();
            }
        });

        me.getRecurrenceTypeCombo().setValue(me.getRecurrenceTypeCombo().store.getAt(2));
        me.recurrenceEnableDisable();
        Ext.resumeLayouts(true);
    },

    createEstimationTask: function (button) {
        var me = this,
            appName = Uni.util.Application.getAppName(),
            newEstimationTaskDto = me.getAddEditEstimationtaskForm().getValues(),
            previousPath = me.getController('Uni.controller.history.EventBus').getPreviousPath();

        me.getAddEditEstimationtaskForm().down('#form-errors').hide();

        if (me.getAddEditEstimationtaskForm().isValid()) {
            var newEstimationTask = me.taskModel || Ext.create('Est.estimationtasks.model.EstimationTask');

            newEstimationTask.beginEdit();

            newEstimationTask.set('name', newEstimationTaskDto.name);
            newEstimationTask.set('application', appName);
            newEstimationTask.set('active', true);
            newEstimationTask.set('lastEstimationOccurrence', null);
            switch(appName){
                case 'MultiSense':{
                    newEstimationTask.set('deviceGroup', {
                        id: me.getAddEditEstimationtaskForm().down('#device-group-combo').getValue(),
                        name: me.getAddEditEstimationtaskForm().down('#device-group-combo').getRawValue()
                    });
                } break;
                case 'MdmApp':{
                    newEstimationTask.set('usagePointGroup', {
                        id: me.getAddEditEstimationtaskForm().down('#usagePoint-group-id').getValue(),
                        displayValue: me.getAddEditEstimationtaskForm().down('#usagePoint-group-id').getRawValue()
                    });
                    newEstimationTask.set('metrologyPurpose', {
                        id: me.getAddEditEstimationtaskForm().down('#cbo-estimation-task-purpose').getValue() || 0,
                        displayValue: me.getAddEditEstimationtaskForm().down('#cbo-estimation-task-purpose').getRawValue()
                    });
                } break;
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
                newEstimationTask.set('nextRun', null);
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

            newEstimationTask.endEdit();

            me.getAddEditEstimationtaskPage().setLoading(true);

            newEstimationTask.save({
                backUrl: previousPath
                    ? '#' + previousPath
                    : me.getController('Uni.controller.history.Router').getRoute('administration/estimationtasks/estimationtask').buildUrl({taskId: newEstimationTask.getId() ? newEstimationTask.getId() : 0}),
                success: function () {
                    if (button.action === 'editTask' && me.fromDetails) {
                        me.getController('Uni.controller.history.Router').getRoute('administration/estimationtasks/estimationtask').forward({taskId: newEstimationTask.getId() ? newEstimationTask.getId() : 0});
                    } else {
                        me.getController('Uni.controller.history.Router').getRoute('administration/estimationtasks').forward();
                    }
                    if (button.action === 'editTask') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationtasks.saveTask.successMsg', 'EST', 'Estimation task saved'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationtasks.addTask.successMsg', 'EST', 'Estimation task added'));
                    }
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        Ext.each(json.errors, function (item) {
                            me.getAddEditEstimationtaskForm().down('[name=' + item.id + ']').setActiveError(item.msg);
                            me.getAddEditEstimationtaskForm().down('#form-errors').show();
                        })
                    }
                },
                callback: function () {
                    me.getAddEditEstimationtaskPage().setLoading(false);
                }
            })
        } else {
            me.getAddEditEstimationtaskForm().down('#form-errors').show();
        }
    },

    showEditEstimationTasksView: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Est.estimationtasks.model.EstimationTask'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget = Ext.widget('estimationtasks-addedit', {
                appName: Uni.util.Application.getAppName(),
                edit: true
            });

        me.taskId = currentTaskId;

        if (me.fromDetails) {
            widget.returnLink = router.getRoute('administration/estimationtasks/estimationtask').buildUrl({taskId: currentTaskId});
        } else {
            widget.returnLink = router.getRoute('administration/estimationtasks').buildUrl();
        }
        pageMainContent.setLoading(true);

        var taskForm = widget.down('#add-edit-estimationtask-form'),
            dataSourcesContainer = widget.down('est-data-sources-container'),
            recurrenceTypeCombo = widget.down('#recurrence-type');

        Ext.suspendLayouts();
        taskModel.load(currentTaskId, {
            success: function (record) {
                var schedule = record.get('schedule'),
                    period = record.get('period');
                me.taskModel = record;
                taskForm.loadRecord(record);
                me.getApplication().fireEvent('estimationTaskLoaded', record);
                taskForm.setTitle(Uni.I18n.translate('general.editx', 'EST', "Edit '{0}'",[record.get('name')]));
                dataSourcesContainer.loadGroupStore(function(){
                    dataSourcesContainer.setComboValue(record);
                    me.getEstimationPeriodCombo().store.load(function () {
                        if (period && (period.id !== 0)) {
                            widget.down('#estimation-period-trigger').setValue({estimationPeriod: true});
                            me.getEstimationPeriodCombo().setValue(period.id);
                        }
                        pageMainContent.setLoading(false);
                    });
                });

                if (record.get('nextRun') && (record.get('nextRun') !== 0)) {
                    widget.down('#recurrence-trigger').setValue({recurrence: true});
                    widget.down('#recurrence-number').setValue(schedule.count);
                    recurrenceTypeCombo.setValue(schedule.timeUnit);
                    widget.down('#start-on').setValue(record.get('nextRun'));
                } else {
                    recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(2));
                }

            },
            failure: function () {
                pageMainContent.setLoading(false);
            }
        });

        me.getApplication().fireEvent('changecontentevent', widget);
        me.recurrenceEnableDisable();
        Ext.resumeLayouts(true);
    },

    recurrenceChange: function(field, newValue, oldValue) {
        var me = this;
        me.recurrenceEnableDisable();
    },

    recurrenceEnableDisable: function() {
        var me = this,
            page = me.getAddEditEstimationtaskPage();
        if(!page.down('#recurrence-trigger').getValue().recurrence) {
            page.down('#recurrence-values').disable();
        } else {
            page.down('#recurrence-values').enable();
        }
    },

    resetPurpose: function (btn) {
        var me = this,
            page = me.getAddEditEstimationtaskPage();
        page.down('#cbo-estimation-task-purpose').clearValue();
        btn.disable();
    }

});
