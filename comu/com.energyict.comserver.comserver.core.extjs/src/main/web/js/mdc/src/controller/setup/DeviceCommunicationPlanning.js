Ext.define('Mdc.controller.setup.DeviceCommunicationPlanning', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicecommunicationschedule.DeviceCommunicationPlanning',
        'Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedule',
        'Mdc.view.setup.devicecommunicationschedule.RemoveSharedCommunicationSchedule',
        'Mdc.view.setup.devicecommunicationschedule.SharedCommunicationScheduleSelectionGrid',
        'Mdc.view.setup.devicecommunicationschedule.SharedCommunicationSchedulePreview'
    ],

    stores: [
        'DeviceSchedules',
        'AvailableCommunicationSchedulesForDevice',
        'UsedCommunicationSchedulesForDevice'
    ],

    requires: [
        'Mdc.store.DeviceSchedules'
    ],

    refs: [
        {ref: 'addSharedCommunicationScheduleGrid', selector: '#sharedCommunicationScheduleSelectionGrid'},
        {ref: 'addSharedCommunicationSchedulePage', selector: 'addSharedCommunicationSchedule'},
        {ref: 'removeSharedCommunicationSchedulePage', selector: 'removeSharedCommunicationSchedule'},
        {ref: 'uniFormErrorMessage', selector: '#form-errors-shared-schedules'},
        {ref: 'warningMessage', selector: '#warningMessageSchedules'},
        {ref: 'sharedCommunicationScheduleSelectionGrid', selector: '#sharedCommunicationScheduleSelectionGrid'},
        {ref: 'sharedCommunicationSchedulePreview', selector: '#sharedCommunicationSchedulePreview'},
        {ref: 'sharedCommunicationSchedulePreviewForm', selector: '#sharedCommunicationSchedulePreviewForm'}
    ],

    deviceMRID: undefined,

    init: function () {
        this.control({
            '#mdc-device-communication-planning #mdc-device-communication-planning-addSharedCommunicationScheduleButton': {
                click: this.navigateToAddSharedScheduleView
            },
            '#addSharedScheduleButtonForm button[action=cancelAction]': {
                click: this.navigateToSchedulesOverview
            },
            '#removeSharedScheduleButtonForm button[action=cancelAction]': {
                click: this.navigateToSchedulesOverview
            },
            '#addSharedScheduleButtonForm button[action=addAction]': {
                click: this.addSharedSchedules
            },
            '#removeSharedScheduleButtonForm button[action=removeAction]': {
                click: this.removeSharedSchedules
            },
            '#addSharedCommunicationSchedule #sharedCommunicationScheduleSelectionGrid': {
                selectionchange: this.onAddSharedComScheduleSelectionChange
            },
            '#removeSharedCommunicationSchedule #sharedCommunicationScheduleSelectionGrid': {
                selectionchange: this.onRemovedSharedComScheduleSelectionChange
            },
            '#mdc-device-communication-planning #mdc-device-communication-planning-removeSharedCommunicationScheduleButton': {
                click: this.navigateToRemoveScheduleView
            },
            '#sharedCommunicationScheduleSelectionGrid': {
                itemclick: this.previewCommunicationSchedule
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
            widget,
            availableScheduleStore = this.getAvailableCommunicationSchedulesForDeviceStore();

        widget = Ext.widget('addSharedCommunicationSchedule', {mRID: me.deviceMRID, store: availableScheduleStore});
        me.deviceMRID = mRID;
        availableScheduleStore.getProxy().setExtraParam('filter', Ext.encode([
            {property: 'mrid', value: mRID},
            {property: 'available', value: true}
        ]));
        availableScheduleStore.load({
            callback: function () {
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#addSharedScheduleButtonForm').setVisible(availableScheduleStore.getCount() !== 0);
                widget.down('uni-form-info-message').setVisible(availableScheduleStore.getCount() !== 0);
            }
        });

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget.device = device;
                me.getApplication().fireEvent('loadDevice', device);
            }
        });
    },

    navigateToRemoveScheduleView: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/communicationschedules/remove').forward();
    },

    showRemoveSharedSchedule: function (mRID) {
        var me = this,
            usedSchedulesStore = me.getUsedCommunicationSchedulesForDeviceStore(),
            widget = Ext.widget('removeSharedCommunicationSchedule', {mRID: mRID, store: usedSchedulesStore});

        usedSchedulesStore.getProxy().setExtraParam('filter', Ext.encode([
            {property: 'mrid', value: mRID},
        ]));
        usedSchedulesStore.load({
            callback: function (records, operation, success) {
                widget.down('#removeSharedScheduleButtonForm').setVisible(usedSchedulesStore.getCount() !== 0);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget.device = device;
                me.getApplication().fireEvent('loadDevice', device);
            }
        });
    },

    previewCommunicationSchedule: function(grid, record) {
        var preview = this.getSharedCommunicationSchedulePreview(),
            previewForm = this.getSharedCommunicationSchedulePreviewForm(),
            taskList = '';

        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        previewForm.loadRecord(record);
        previewForm.down('#comTaskPreviewContainer').removeAll();
        Ext.each(record.comTaskUsages().data.items, function (comTaskUsage) {
            taskList += Ext.String.htmlEncode(comTaskUsage.get('name')) + '<br/>'
        });
        previewForm.down('#comTaskPreviewContainer').add({
            xtype: 'displayfield',
            value: taskList,
            htmlEncode: false
        });
        preview.show();
    },

    navigateToSchedulesOverview: function () {
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

        if (this.checkValidSelection(communicationSchedules, true)) {
            Ext.each(communicationSchedules, function (communicationSchedule) {
                scheduleIds.push(communicationSchedule.get('id'));
            });

            Ext.Ajax.request({
                url: '/api/ddr/devices/' + encodeURIComponent(mRID) + '/sharedschedules',
                method: 'PUT',
                params: '',
                jsonData: {
                    device: _.pick(device.getRecordData(), 'mRID', 'version', 'parent'),
                    scheduleIds: scheduleIds
                },
                timeout: 180000,
                success: function () {
                    route.forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationSchedule.addSharedSchedulesSucceeded', 'MDC', 'Shared communication schedule(s) added'));
                }
            });
        }
    },

    removeSharedSchedules: function () {
        var me = this,
            communicationSchedules = this.getAddSharedCommunicationScheduleGrid().getSelectionModel().getSelection(),
            scheduleIds = [],
            device = me.getRemoveSharedCommunicationSchedulePage().device,
            mRID = device.get('mRID'),
            route = me.getController('Uni.controller.history.Router').getRoute('devices/device/communicationschedules');

        if (this.checkValidSelection(communicationSchedules, false)) {
            Ext.each(communicationSchedules, function (communicationSchedule) {
                scheduleIds.push(communicationSchedule.get('id'));
            });

            Ext.Ajax.request({
                url: '/api/ddr/devices/' + encodeURIComponent(mRID) + '/sharedschedules',
                method: 'DELETE',
                params: '',
                jsonData: {
                    device: _.pick(device.getRecordData(), 'mRID', 'version', 'parent'),
                    scheduleIds: scheduleIds
                },
                timeout: 180000,
                success: function () {
                    route.forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationSchedule.removeSharedSchedulesSucceeded', 'MDC', 'Shared communication schedule(s) removed'));
                }
            });
        }
    },

    checkValidSelection: function (communicationSchedules, isAdd) {
        var me = this;
        me.getUniFormErrorMessage().hide();
        me.getWarningMessage().setVisible(false);
        if (communicationSchedules.length === 1) {
            return true;
        } else if (communicationSchedules.length === 0) {
            me.getUniFormErrorMessage().show();
            me.getWarningMessage().update('<span style="color:red">' + Uni.I18n.translate('deviceCommunicationSchedule.noScheduleSelected', 'MDC', 'Select at least one shared communication schedule') + '</span>');
            me.getWarningMessage().setVisible(true);
            return false;
        } else if (communicationSchedules.length > 1 && isAdd) {
            return me.checkOverlap(communicationSchedules);

        } else {
            return true;
        }
    },

    onAddSharedComScheduleSelectionChange: function (grid, selection) {
        var me = this,
            communicationSchedules = me.getAddSharedCommunicationScheduleGrid().getSelectionModel().getSelection();
        me.getWarningMessage().setVisible(false);
        me.getUniFormErrorMessage().hide();
        if (communicationSchedules.length > 1) {
            me.checkOverlap(communicationSchedules);
        }
        if(selection.length > 1) {
            this.getSharedCommunicationSchedulePreview().show();
        }
    },

    onRemovedSharedComScheduleSelectionChange: function(grid, selection) {
        if(selection.length > 1) {
            this.getSharedCommunicationSchedulePreview().show();
        }
    },

    checkOverlap: function (communicationSchedules) {
        var me = this;
        var valuesToCheck = [];
        Ext.each(communicationSchedules, function (item) {
            valuesToCheck.push.apply(valuesToCheck, item.get('comTaskUsages'));
        });
        if (_.uniq(valuesToCheck, function (item) {
                return item.id;
            }).length === valuesToCheck.length) {
            me.getUniFormErrorMessage().hide();
            me.getWarningMessage().setVisible(false);
            return true;
        } else {
            me.getUniFormErrorMessage().show();
            me.getWarningMessage().update('<span style="color:#EB5642">' + Uni.I18n.translate('deviceCommunicationSchedule.ComTaskOverlap', 'MDC', "Shared communication schedules can't contain the same communication task.") + '</span>');
            me.getWarningMessage().setVisible(true);
            return false;
        }
    }
});