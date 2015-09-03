Ext.define('Est.estimationtasks.controller.EstimationTasksAddEdit', {
    extend: 'Ext.app.Controller',

    requires: [],

    stores: [
        'Est.estimationtasks.store.DeviceGroups',
        'Est.estimationtasks.store.DaysWeeksMonths'
    ],

    views: [
        'Est.estimationtasks.view.AddEdit'
    ],

    refs: [
        {ref: 'addEditEstimationtaskPage', selector: 'estimationtasks-addedit'},
        {ref: 'addEditEstimationtaskForm', selector: '#add-edit-estimationtask-form'},
        {ref: 'deviceGroupCombo', selector: '#device-group-id'},
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
            }
        });
    },

    showAddEstimationTasksView: function () {
        var me = this,
            widget = Ext.widget('estimationtasks-addedit');
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getEstimationPeriodCombo().store.load({
            params: {
                category: 'relativeperiod.category.estimation'
            },
            callback: function () {
                me.getDeviceGroupCombo().store.load(function () {
                    if (this.getCount() === 0) {
                        me.getDeviceGroupCombo().allowBlank = true;
                        me.getDeviceGroupCombo().hide();
                        me.getNoDeviceGroupBlock().show();
                    }
                });
            }
        });

        me.getRecurrenceTypeCombo().setValue(me.getRecurrenceTypeCombo().store.getAt(2));
    },

    createEstimationTask: function (button) {
        var me = this, newEstimationTaskDto = me.getAddEditEstimationtaskForm().getValues();

        me.getAddEditEstimationtaskForm().down('#form-errors').hide();

        if (me.getAddEditEstimationtaskForm().isValid()) {
            var newEstimationTask = me.taskModel || Ext.create('Est.estimationtasks.model.EstimationTask');

            newEstimationTask.beginEdit();

            newEstimationTask.set('name', newEstimationTaskDto.name);
            newEstimationTask.set('active', true);
            newEstimationTask.set('lastEstimationOccurrence', null);
            newEstimationTask.set('deviceGroup', {
                id: newEstimationTaskDto.deviceGroupId,
                name: me.getAddEditEstimationtaskForm().down('#device-group-id').getRawValue()
            });

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
                success: function () {
                    if (button.action === 'editTask' && me.fromDetails) {
                        me.getController('Uni.controller.history.Router').getRoute('administration/estimationtasks/estimationtask').forward({taskId: newEstimationTask.getId()});
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
            widget;

        me.taskId = currentTaskId;

        if (me.fromDetails) {
            widget = Ext.widget('estimationtasks-addedit', {
                edit: true,
                returnLink: router.getRoute('administration/estimationtasks/estimationtask').buildUrl({taskId: currentTaskId})
            })
        } else {
            widget = Ext.widget('estimationtasks-addedit', {
                edit: true,
                returnLink: router.getRoute('administration/estimationtasks').buildUrl()
            })
        }
        pageMainContent.setLoading(true);

        var taskForm = widget.down('#add-edit-estimationtask-form'),
            deviceGroupCombo = widget.down('#device-group-id'),
            recurrenceTypeCombo = widget.down('#recurrence-type');

        taskModel.load(currentTaskId, {
            success: function (record) {
                var schedule = record.get('schedule'),
                    deviceGroup = record.get('deviceGroup'),
                    period = record.get('period');
                me.taskModel = record;
                taskForm.loadRecord(record);
                me.getApplication().fireEvent('estimationTaskLoaded', record);
                taskForm.setTitle(Uni.I18n.translate('general.editx', 'EST', "Edit '{0}'",[record.get('name')]));
                deviceGroupCombo.store.load(function () {
                    if (this.getCount() === 0) {
                        deviceGroupCombo.allowBlank = true;
                        deviceGroupCombo.hide();
                        me.getNoDeviceGroupBlock().show();
                    }
                    deviceGroupCombo.setValue(deviceGroupCombo.store.getById(deviceGroup.id));
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
    }
});
