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
        {ref: 'noDeviceGroupBlock', selector: '#no-device'},
        {ref: 'recurrenceTypeCombo', selector: '#recurrence-type'}
    ],

    //fromDetails: false,
    //fromEdit: false,
    //taskModel: null,
    //taskId: null,

    init: function () {
        this.control({
            '#add-button': {
                click: this.createEstimationTask
            }
        });
    },

    showAddEstimationTasksView: function () {
        var me = this, widget = Ext.widget('estimationtasks-addedit');
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getDeviceGroupCombo().store.load(function () {
            if (this.getCount() === 0) {
                me.getDeviceGroupCombo().allowBlank = true;
                me.getDeviceGroupCombo().hide();
                me.getNoDeviceGroupBlock().show();
            }
        });
        me.getRecurrenceTypeCombo().setValue(me.getRecurrenceTypeCombo().store.getAt(2));
    },

    createEstimationTask: function () {
        var me = this, newEstimationTaskDto = me.getAddEditEstimationtaskForm().getValues();

        me.getAddEditEstimationtaskForm().down('#form-errors').hide();

        if (me.getAddEditEstimationtaskForm().isValid()) {
            var newEstimationTask = Ext.create('Est.estimationtasks.model.EstimationTask');

            newEstimationTask.beginEdit();

            newEstimationTask.set('name', newEstimationTaskDto.name);
            newEstimationTask.set('active', true);
            newEstimationTask.set('lastExportOccurrence', null); //TODO: Change to lastEstimationOccurrence
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

            delete newEstimationTask.data.lastEstimationOccurrence; // TODO: Remove after REST will be completely done

            newEstimationTask.endEdit();

            me.getAddEditEstimationtaskPage().setLoading(true);

            newEstimationTask.save({
                success: function () {
                    me.getController('Uni.controller.history.Router').getRoute('administration/estimationtasks').forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationtasks.addTask.successMsg', 'EST', 'Estimation task added'));
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
    }
});
