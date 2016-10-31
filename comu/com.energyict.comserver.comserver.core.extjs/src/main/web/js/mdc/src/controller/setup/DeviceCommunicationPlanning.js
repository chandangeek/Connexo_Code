Ext.define('Mdc.controller.setup.DeviceCommunicationPlanning', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicecommunicationschedule.DeviceCommunicationPlanning',
        'Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedule',
        'Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationScheduleGrid'
    ],

    stores: [
        'DeviceSchedules',
        'AvailableCommunicationSchedulesForDevice'
    ],

    requires: [
        'Mdc.store.DeviceSchedules'
    ],

    refs: [
        {ref: 'addSharedCommunicationScheduleGrid', selector: '#addSharedCommunicationScheduleGrid'},
        {ref: 'addSharedCommunicationSchedulePage', selector: 'addSharedCommunicationSchedule'},
        {ref: 'uniFormErrorMessage', selector: 'addSharedCommunicationSchedule #form-errors'},
        {ref: 'warningMessage', selector: 'addSharedCommunicationSchedule #warningMessage'},
    ],

    deviceMRID: undefined,

    init: function () {
        this.control({
            '#mdc-device-communication-planning #mdc-device-communication-planning-addSharedCommunicationScheduleButton': {
                click: this.navigateToAddSharedScheduleView
            },
            '#addSharedScheduleButtonForm button[action=cancelAction]': {
                click: this.navigateBackToSchedulesOverview
            },
            '#addSharedScheduleButtonForm button[action=addAction]': {
                click: this.addSharedSchedules
            },
            '#addSharedCommunicationScheduleGrid': {
                selectionchange: this.onSharedComScheduleSelectionChange
            }
        })
    },

    showDeviceCommunicationPlanning: function (deviceMRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.deviceMRID = deviceMRID;

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceMRID, {
            success: function (device) {
                var scheduleStore = me.getDeviceSchedulesStore();
                scheduleStore.getProxy().setUrl(deviceMRID);
                scheduleStore.load({
                    callback: function () {
                        var widget = Ext.widget('deviceCommunicationPlanning', {
                            device: device,
                            scheduleStore: scheduleStore
                        });
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getApplication().fireEvent('loadDevice', device);

                        if (scheduleStore.getCount() === 0) {
                            widget.down('#mdc-device-communication-planning-grid-msg').show();
                            widget.down('#mdc-device-communication-planning-grid grid').hide();
                        } else {
                            widget.down('#mdc-device-communication-planning-grid-msg').hide();
                            widget.down('#mdc-device-communication-planning-grid grid').show();
                        }
                    }
                })
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    },

    navigateToAddSharedScheduleView: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/communicationschedules/add').forward();
    },

    showAddSharedSchedule: function (mRID) {
        var me = this,
            widget = Ext.widget('addSharedCommunicationSchedule', {mRID: me.deviceMRID}),
            availableScheduleStore = this.getAvailableCommunicationSchedulesForDeviceStore();

        me.deviceMRID = mRID;
        widget.down('#addSharedCommunicationScheduleGrid').reconfigure(availableScheduleStore);
        availableScheduleStore.getProxy().setExtraParam('filter', Ext.encode([
            {property: 'mrid', value: mRID},
            {property: 'available', value: true}
        ]));
        availableScheduleStore.load({
            callback: function () {
                me.getApplication().fireEvent('changecontentevent', widget);
                if (availableScheduleStore.getCount() === 0) {
                    widget.down('#addSharedScheduleButtonForm').setVisible(false);
                } else {
                    widget.down('#addSharedScheduleButtonForm').setVisible(true);
                }
            }
        });

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget.device = device;
                me.getApplication().fireEvent('loadDevice', device);
            }
        });
    },

    navigateBackToSchedulesOverview: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        router.getRoute('devices/device/communicationschedules').forward();
    },

    addSharedSchedules: function () {
        var me = this,
            communicationSchedules = this.getAddSharedCommunicationScheduleGrid().getSelectionModel().getSelection(),
            scheduleIds = [],
            device = me.getAddSharedCommunicationSchedulePage().device,
            mRID = device.get('mRID'),
            route = me.getController('Uni.controller.history.Router').getRoute('devices/device/communicationschedules');

        if (this.checkValidSelection(communicationSchedules)) {
            Ext.each(communicationSchedules, function (communicationSchedule) {
                scheduleIds.push(communicationSchedule.get('id'));
            });

            Ext.Ajax.request({
                url: '/api/ddr/devices/' + mRID + '/sharedschedules',
                method: 'PUT',
                params: '',
                jsonData: {
                    device: _.pick(device.getRecordData(), 'mRID', 'version', 'parent'),
                    scheduleIds: scheduleIds
                },
                timeout: 180000,
                success: function () {
                    route.forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationSchedule.addSharedScheduleSucceeded', 'MDC', 'Shared communication schedule successfully added'));
                }
            });
        }
    },

    checkValidSelection: function (communicationSchedules) {
        var me = this;
        me.getUniFormErrorMessage().hide();
        if (communicationSchedules.length === 1) {
            return true;
        } else if (communicationSchedules.length === 0) {
            me.getUniFormErrorMessage().show();
            me.getWarningMessage().update('<span style="color:red">' + Uni.I18n.translate('deviceCommunicationSchedule.noScheduleSelected', 'MDC', 'Select at least one shared communication schedule') + '</span>');
            me.getWarningMessage().setVisible(true);
            return false;
        } else if (communicationSchedules.length > 1) {
            return me.checkOverlap(communicationSchedules);

        }
    },

    onSharedComScheduleSelectionChange: function () {
        var me = this,
            communicationSchedules = me.getAddSharedCommunicationScheduleGrid().getSelectionModel().getSelection();
        me.getWarningMessage().setVisible(false);
        me.getUniFormErrorMessage().hide();
        if (communicationSchedules.length > 1) {
            me.checkOverlap(communicationSchedules);
        }
    },

    checkOverlap: function(communicationSchedules) {
        var me = this;
        var valuesToCheck = [];
        Ext.each(communicationSchedules, function (item) {
            valuesToCheck.push.apply(valuesToCheck, item.get('comTaskUsages'));
        });
        if (_.uniq(valuesToCheck,function (item) {
                return item.id;
            }).length === valuesToCheck.length) {
            me.getUniFormErrorMessage().hide();
            me.getWarningMessage().setVisible(false);
            return true;
        } else {
            me.getUniFormErrorMessage().show();
            me.getWarningMessage().update('<span style="color:red">' + Uni.I18n.translate('deviceCommunicationSchedule.ComTaskOverlap', 'MDC', 'The current selection has overlapping communication tasks.') + '</span>');
            me.getWarningMessage().setVisible(true);
            return false;
        }
    }
})
;