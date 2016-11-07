Ext.define('Mdc.controller.setup.DeviceCommunicationSchedules', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification',
        'Mdc.store.DeviceSchedules'
    ],

    views: [
        'Mdc.view.setup.devicecommunicationschedule.DeviceCommunicationScheduleSetup',
        'Mdc.view.setup.devicecommunicationschedule.SharedCommunicationScheduleGrid',
        'Mdc.view.setup.devicecommunicationschedule.IndividualCommunicationScheduleGrid',
        'Mdc.view.setup.devicecommunicationschedule.OnRequestCommunicationScheduleGrid',
        'Mdc.view.setup.devicecommunicationschedule.AddSchedulePopUp'
    ],

    stores: [
        'DeviceSchedules',
        'CommunicationTaskConfigsOfDeviceConfiguration',
        'AvailableCommunicationSchedulesForDevice'
    ],

    refs: [
        {ref: 'sharedCommunicationScheduleGrid', selector: '#sharedCommunicationScheduleGrid'},
        {ref: 'onRequestCommunicationScheduleGrid', selector: '#onRequestComtaskGrid'},
        {ref: 'individualCommunicationScheduleGrid', selector: '#individualComtaskGrid'},
        {ref: 'scheduleField', selector: '#scheduleField'},
        {ref: 'addButton', selector: '#addButton'}
    ],

    init: function () {
        this.control({
            'menuitem[action=runCommunicationSchedule]': {
                click: this.runCommunicationSchedule
            },
            'actioncolumn': {
                removeSharedCommunicationSchedule: this.removeSharedCommunicationScheduleConfirmation
            },
            'menuitem[action=changeCommunicationSchedule]': {
                click: this.changeCommunicationSchedule
            },
            'button[action=changeIndividualScheduleAction]': {
                click: this.updateIndividualSchedule
            }
        });
    },

    showDeviceCommunicationScheduleView: function (mrid) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        this.mrid = mrid;

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mrid, {
            success: function (device) {
                var widget = Ext.widget('deviceCommunicationScheduleSetup', {device: device});
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadDevice', device);

                var scheduleStore = me.getDeviceSchedulesStore();
                scheduleStore.getProxy().setUrl(mrid);
                scheduleStore.load({
                    callback: function () {
                        var shared = [];
                        var individual = [];
                        var adHocComTasks = [];
                        scheduleStore.each(function (schedule) {
                            if (schedule.get('type') === 'INDIVIDUAL') {
                                individual.push(schedule);

                            } else if (schedule.get('type') === 'SCHEDULED') {
                                shared.push(schedule);
                            } else {
                                adHocComTasks.push(schedule);
                            }
                        });
                        if (shared.length > 0) {
                            var sharedGrid = {
                                xtype: 'sharedCommunicationScheduleGrid',
                                shared: shared
                            };
                            widget.down('#sharedDeviceCommunicationScheduleSetupPanel').add(sharedGrid);
                        } else {
                            var msg = {
                                xtype: 'displayfield',
                                itemId: 'msg-no-shared-communication-schedule',
                                value: Uni.I18n.translate('deviceCommunicationSchedule.noSharedCommunicationSchedules', 'MDC', 'The device doesn\'t use shared communication schedules.')
                            };
                            widget.down('#sharedDeviceCommunicationScheduleSetupPanel').add(msg);
                        }

                        //add individual component
                        if (individual.length > 0) {
                            var individualGrid = {
                                xtype: 'individualCommunicationScheduleGrid',
                                individual: individual
                            };
                            widget.down('#individualDeviceCommunicationScheduleSetupPanel').add(individualGrid);
                        } else {
                            var msg = {
                                xtype: 'displayfield',
                                itemId: 'msg-no-individual-shared-communication-schedule',
                                value: Uni.I18n.translate('deviceCommunicationSchedule.noIndividualCommunicationSchedules', 'MDC', 'The device doesn\'t use individual shared communication schedules.')
                            };
                            widget.down('#individualDeviceCommunicationScheduleSetupPanel').add(msg);
                        }

                        //add on request component
                        if (adHocComTasks.length > 0) {
                            var onRequestGrid = {
                                xtype: 'onRequestCommunicationScheduleGrid',
                                onRequest: adHocComTasks
                            };
                            widget.down('#onRequestDeviceCommunicationScheduleSetupPanel').add(onRequestGrid);
                        } else {
                            var msg = {
                                xtype: 'displayfield',
                                itemId: 'msg-no-communication-task-run-on-request',
                                value: Uni.I18n.translate('deviceCommunicationSchedule.noOnRequestCommunicationSchedules', 'MDC', 'There are no communication tasks that only run on request.')
                            };
                            widget.down('#onRequestDeviceCommunicationScheduleSetupPanel').add(msg);
                        }
                        viewport.setLoading(false);
                    }
                });
            }
        });
    },

    removeSharedCommunicationScheduleConfirmation: function (record2Remove) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceCommunicationSchedule.deleteConfirmationSharedSchedule.msg', 'MDC', 'This shared communication schedule will no longer be available on this device.'),
            title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', record2Remove.get('name')),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.removeSharedCommunicationSchedule(record2Remove);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    removeSharedCommunicationSchedule: function (record) {
        var me = this;
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(this.mrid) + '/schedules/' + record.get('id'),
            method: 'DELETE',
            params: '',
            jsonData: _.pick(record.getRecordData(), 'id', 'name', 'version', 'parent'),
            timeout: 180000,
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('communicationSchedule.removed', 'MDC', 'Shared communication schedule removed'));
                me.getController('Uni.controller.history.Router').getRoute().forward();
            }
        });
    },

    changeCommunicationSchedule: function () {
        var me = this;
        var widget = Ext.widget('addSchedulePopUp', {action: 'changeIndividualScheduleAction'});
        var comTask = this.getIndividualCommunicationScheduleGrid().getSelectionModel().getSelection()[0];
        widget.setTitle(Uni.I18n.translate('deviceCommunicationSchedule.changeFrequencyx', 'MDC', "Change frequency of communication task '{0}'",[comTask.get('comTaskInfos')[0].name]));
        widget.down('#addScheduleForm').loadRecord(comTask);
        widget.show();
    },

    runCommunicationSchedule: function () {
        var me = this;
        var jsonData;
        var request = {};
        request.id = this.getOnRequestCommunicationScheduleGrid().getSelectionModel().getSelection()[0].get('comTaskInfos')[0].id;
        jsonData = Ext.encode(request);
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(me.mrid) + '/schedules',
            method: 'POST',
            params: '',
            jsonData: jsonData,
            timeout: 180000,
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationSchedule.taskPlanned', 'MDC', 'The task has been planned'));
                me.showDeviceCommunicationScheduleView(me.mrid);
            }
        });
    },

    updateIndividualSchedule: function (button) {
        var me = this,
            schedule = me.getIndividualCommunicationScheduleGrid().getSelectionModel().getSelection()[0],
            scheduleField = me.getScheduleField();

        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(me.mrid) + '/schedules',
            isNotEdit: true,
            method: 'PUT',
            params: '',
            jsonData: Ext.merge(_.pick(schedule.getRecordData(), 'id', 'version', 'parent', 'name'), {
                schedule: scheduleField.getValue()
            }),
            timeout: 180000,
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationSchedule.editIndividualScheduleSucceeded', 'MDC', 'Individual communication schedule saved'));
                me.getController('Uni.controller.history.Router').getRoute().forward();
            }
        });
        button.up('.window').close();
    }

});
