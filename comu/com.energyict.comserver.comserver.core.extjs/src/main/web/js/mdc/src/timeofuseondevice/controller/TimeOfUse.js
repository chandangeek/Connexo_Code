Ext.define('Mdc.timeofuseondevice.controller.TimeOfUse', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.timeofuseondevice.store.AllowedCalendars',
        'Mdc.timeofuseondevice.store.CalendarTypes',
        'Mdc.timeofuseondevice.store.CalendarContracts'
    ],

    views: [
        'Mdc.timeofuseondevice.view.Setup',
        'Mdc.timeofuseondevice.view.ViewCalendarSetup',
        'Mdc.timeofuseondevice.view.SendCalendarSetup',
        'Mdc.timeofuseondevice.view.ActivatePassiveCalendarWindow'
    ],

    models: [
        'Mdc.model.Device',
        'Uni.model.timeofuse.Calendar',
        'Mdc.timeofuseondevice.model.CalendarOnDevice',
        'Mdc.timeofuseondevice.model.NextPassiveCalendar',
        'Mdc.timeofuseondevice.model.CalendarType',
        'Mdc.timeofuseondevice.model.CalendarContract'
    ],

    refs: [
        {
            ref: 'sendCalendarForm',
            selector: 'tou-device-send-cal-form form'
        },
        {
            ref: 'sendCalendarContainer',
            selector: 'tou-device-send-cal-form'
        },
        {
            ref: 'commandWillNotBePickedUp',
            selector: '#commandWillNotBePickedUp'
        },
        {
            ref: 'activatePassiveCalendarWindow',
            selector: 'activate-passive-calendar-window'
        }
    ],

    init: function () {
        var me = this;
        this.control({
            'tou-device-action-menu': {
                click: me.chooseAction
            },
            'tou-device-send-cal-form #tou-save-calendar-command-button': {
                click: me.saveCommand
            },
            '#empty-comp-send-calendar-tou': {
                click: me.goToSendCalendarFormFromEmptyComponent
            },
            '#empty-comp-verify-calendars-tou': {
                click: me.verifyCalendarFromEmptyComponent
            },
            '#activate-passive-calendar-button': {
                click: this.activatePassiveCalendar
            }
        });
    },

    showTimeOfUseOverview: function (deviceId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            reader = Ext.create('Ext.data.reader.Json', {
                model: 'Mdc.timeofuseondevice.model.CalendarOnDevice'
            }),
            result,
            resultSet,
            view,
            noActiveCalendar = false;

        deviceModel.load(deviceId, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                view = Ext.widget('device-tou-setup', {
                    device: record
                });
                me.getApplication().fireEvent('changecontentevent', view);
                if (view.down('#wrappingPanel')) {
                    view.down('#wrappingPanel').setLoading(true);
                }
                Ext.Ajax.request(
                    {
                        url: '/api/ddr/devices/' + encodeURIComponent(deviceId) + '/timeofuse',
                        method: 'GET',
                        success: function (response, opt) {
                            result = JSON.parse(response.responseText);
                            resultSet = reader.read(result);
                            if (resultSet.records[0].getActiveCalendar() === null && resultSet.records[0].get('passiveCalendars') === null && resultSet.records[0].getNextPassiveCalendar() === null) {
                                view.showEmptyComponent();
                                view.down('tou-device-action-menu').showPreview = false;
                                view.down('#tou-device-actions-button').hide();
                            } else {

                                if (view.down('device-tou-preview-form')) {
                                    if (!(Object.keys(result).length === 0 && result.constructor === Object) && resultSet.records[0].getActiveCalendar() !== null) {
                                        view.down('device-tou-preview-form').fillFieldContainers(resultSet.records[0]);
                                        view.down('device-tou-preview-form').show();
                                        if (resultSet.records[0].get('activeIsGhost') === true) {
                                            view.down('tou-device-action-menu').showPreview = false;
                                        }
                                        view.down('tou-device-action-menu').record = resultSet.records[0].getActiveCalendar();

                                    } else {

                                        view.down('device-tou-preview-form').fillWithDashes();
                                        view.down('tou-device-action-menu').showPreview = false;
                                        noActiveCalendar = true;
                                    }
                                    view.down('device-tou-preview-form').fillPassiveCalendars(resultSet.records[0].get('passiveCalendars'));
                                    view.down('#wrappingPanel').setLoading(false);

                                    if(resultSet.records[0].get('passiveCalendars') === null && noActiveCalendar) {
                                        view.down('device-tou-preview-form').hide();
                                    }
                                }

                                if (view.down('device-tou-planned-on-form')) {
                                    if (resultSet.records[0].getNextPassiveCalendar() === null) {
                                        view.down('device-tou-planned-on-form').hide();
                                    } else {
                                        view.down('device-tou-planned-on-form').show();
                                        view.down('device-tou-planned-on-form').checkWillNotBePickedUp(resultSet.records[0].getNextPassiveCalendar().get('willBePickedUpByPlannedComtask'),
                                            resultSet.records[0].getNextPassiveCalendar().get('willBePickedUpByComtask'));
                                        view.down('device-tou-planned-on-form').down('form').loadRecord(resultSet.records[0].getNextPassiveCalendar());
                                    }

                                }

                            }
                            if(!Uni.Auth.checkPrivileges(Mdc.privileges.DeviceCommands.executeCommands) && view.down('tou-device-action-menu').showPreview === false) {
                                view.down('#tou-device-actions-button').hide();
                            }
                        }
                        ,
                        failure: function () {
                            if (view.down('#wrappingPanel')) {
                                view.down('#wrappingPanel').setLoading(false);
                            }
                        }
                    }
                );
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            deviceId = menu.device.get('name');

        switch (item.action) {
            case 'activatecalendar':
                me.showActivatePassiveCalendarWindow(deviceId);
                break;
            case 'cleartariff':
                me.clearPassiveCalendar(deviceId);
                break;
            case 'sendcalendar':
                me.goToSendCalendarForm(deviceId);
                break;
            case 'verifycalendars':
                me.verifyCalendars(deviceId);
                break;
            case 'viewpreview':
                me.redirectToPreview(menu.record.get('id'));
                break;
        }
    },

    verifyCalendars: function (deviceId) {
        var me = this;
        Ext.Ajax.request(
            {
                url: '/api/ddr/devices/' + encodeURIComponent(deviceId) + '/timeofuse/verify',
                method: 'PUT',
                success: function (response, opt) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('tou.verifyCalendarTaskPlannedMessage', 'MDC', 'The task has been planned. Actual calendar information will be available as soon as the task has completed.'));
                }
            }
        );
    },

    clearPassiveCalendar: function (deviceId) {
        var me = this;
        Ext.Ajax.request(
            {
                url: '/api/ddr/devices/' + deviceId + '/timeofuse/clearpassive',
                method: 'PUT',
                success: function (response, opt) {
                    var json = Ext.decode(response.responseText, true);
                    if (json.willBePickedUpByPlannedComtask && json.willBePickedUpByComtask) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.commandAdded', 'MDC', 'Command added'));
                    } else {
                        if (!json.willBePickedUpByPlannedComtask && json.willBePickedUpByComtask) {
                            me.getCommandWillNotBePickedUp().setText(Uni.I18n.translate('tou.clearPassiveWillNotBePickedUp', 'MDC', 'The \'Clear passive calendar\' command is part of a communication task that is not planned and will not be picked up'));
                        } else if (!json.willBePickedUpByComtask) {
                            me.getCommandWillNotBePickedUp().setText(Uni.I18n.translate('tou.clearPassiveNotPartOfTask', 'MDC', 'The \'Clear passive calendar\' command is not part of a communication task on this device'));
                        }
                        me.getCommandWillNotBePickedUp().show();
                    }
                }
            }
        );
    },

    showActivatePassiveCalendarWindow: function (deviceId) {
        Ext.widget('activate-passive-calendar-window', {
            deviceId: deviceId
        }).show();
    },

    activatePassiveCalendar: function () {
        var me = this,
            activateWindow = me.getActivatePassiveCalendarWindow(),
            form = activateWindow.down('#activate-passive-window-form'),
            activationDate;

        if (form && form.down('#on-activation-passive-date').checked) {
            activationDate = form.down('#activation-date-values').down('#activation-on').getValue().getTime();
        } else {
            activationDate = new Date().getTime()
        }

        Ext.Ajax.request(
            {
                params: {
                    activationDate: activationDate
                },
                url: '/api/ddr/devices/' + form.deviceId + '/timeofuse/activatepassive',
                method: 'PUT',
                success: function (response, opt) {
                    var json = Ext.decode(response.responseText, true);
                    if (json.willBePickedUpByPlannedComtask && json.willBePickedUpByComtask) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.commandAdded', 'MDC', 'Command added'));
                    } else {
                        if (!json.willBePickedUpByPlannedComtask && json.willBePickedUpByComtask) {
                            me.getCommandWillNotBePickedUp().setText(Uni.I18n.translate('tou.activatePassiveWillNotBePickedUp', 'MDC', 'The \'Activate passive calendar\' command is part of a communication task that is not planned and will not be picked up'));
                        } else if (!json.willBePickedUpByComtask) {
                            me.getCommandWillNotBePickedUp().setText(Uni.I18n.translate('tou.activatePassiveNotPartOfTask', 'MDC', 'The \'Activate passive calendar\' command is not part of a communication task on this device'));
                        }
                        me.getCommandWillNotBePickedUp().show();
                    }
                }
            }
        );
        activateWindow.close();
    },

    redirectToPreview: function (calendarId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('devices/device/timeofuse/viewpreview');
        route.forward(Ext.merge(router.arguments, {calendarId: calendarId}));
    },

    showPreviewCalendarView: function (deviceId, calendarId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            view;

        deviceModel.load(deviceId, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                view = Ext.widget('tou-device-view-calendar-setup', {
                    url: '/api/ddr/devices/' + deviceId + '/timeofuse',
                    calendarId: calendarId,
                    device: record
                });
                view.on('timeofusecalendarloaded', function (newRecord) {
                    me.getApplication().fireEvent('timeofusecalendarloaded', newRecord.get('name'));
                    return true;
                }, {single: true});
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    redirectToOverview: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('devices/device/timeofuse', {deviceId: deviceId});
        route.forward();
    },

    goToSendCalendarFormFromEmptyComponent: function (stepButton) {
        this.goToSendCalendarForm(stepButton.deviceId);
    },

    verifyCalendarFromEmptyComponent: function (stepButton) {
        this.verifyCalendars(stepButton.deviceId);
    },

    goToSendCalendarForm: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('devices/device/timeofuse/send', {deviceId: deviceId});
        route.forward();
    },

    showSendCalendarView: function (deviceId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            view,
            store = me.getStore('Mdc.timeofuseondevice.store.AllowedCalendars');

        store.getProxy().setExtraParam('deviceId', deviceId);
        deviceModel.load(deviceId, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                view = Ext.widget('tou-device-send-cal-setup', {
                    device: record
                });
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    saveCommand: function () {
        var me = this,
            form = me.getSendCalendarForm(),
            formErrorsPanel = form.down('#form-errors'),
            json = {},
            id;

        formErrorsPanel.hide();

        if (!form.isValid()) {
            formErrorsPanel.show();
        } else {
            if (form.down('#calendarComboBox').down('combo')) {
                json.allowedCalendarId = form.down('#calendarComboBox').down('combo').value;
            }
            if (form.down('#on-release-date').checked) {
                json.releaseDate = form.down('#release-date-values').down('#release-on').getValue().getTime();
            } else {
                json.releaseDate = new Date().getTime()
            }
            if (form.down('#typeCombo').isVisible()) {
                json.type = form.down('#typeCombo').value;
            }
            if (form.down('#contractCombo').isVisible()) {
                json.contract = form.down('#contractCombo').value;
            }
            if (form.down('#activate-calendar-container').isVisible()) {
                if (form.down('#immediate-activation-date').checked) {
                    json.activationDate = new Date().getTime();
                } else if (form.down('#on-activation-date').checked) {
                    json.activationDate = form.down('#activation-date-values').down('#activation-on').getValue().getTime();
                }
            }

            if (form.down('#update-calendar').isVisible()) {
                json.calendarUpdateOption = form.down('#update-calendar').getValue().updateCalendar;
            } else if (Mdc.dynamicprivileges.DeviceState.supportsSpecialDays() && !Mdc.dynamicprivileges.DeviceState.supportsNormalSend()) {
                json.calendarUpdateOption = form.down('#only-special-days').inputValue
            } else if (!Mdc.dynamicprivileges.DeviceState.supportsSpecialDays() && Mdc.dynamicprivileges.DeviceState.supportsNormalSend()) {
                json.calendarUpdateOption = form.down('#full-calendar').inputValue
            }

            me.sendCalendar(form.deviceId, json);
        }
    },

    sendCalendar: function (deviceId, payload) {
        var me = this,
            url = '/api/ddr/devices/' + encodeURIComponent(deviceId) + '/timeofuse/send';


        me.getSendCalendarContainer().setLoading(true);
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            jsonData: Ext.encode(payload),
            success: function () {
                me.redirectToOverview(deviceId);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('timeofuse.commandScheduled', 'MDC', 'Command scheduled'));
            },
            callback: function () {
                me.getSendCalendarContainer().setLoading(false);
            }
        });
    }
});




