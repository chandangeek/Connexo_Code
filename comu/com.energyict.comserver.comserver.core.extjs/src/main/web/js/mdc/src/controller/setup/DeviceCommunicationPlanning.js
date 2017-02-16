/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceCommunicationPlanning', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicecommunicationschedule.DeviceCommunicationPlanning',
        'Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedule',
        'Mdc.view.setup.devicecommunicationschedule.ScheduleAdd',
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
        {ref: 'deviceCommunicationPlanningGrid', selector: 'DeviceCommunicationPlanningGrid grid'},
        {ref: 'scheduleField', selector: 'device-schedule-add #device-schedule-add-scheduleField'},
        {ref: 'addScheduleView', selector: 'device-schedule-add'},
        {ref: 'removeSharedCommunicationSchedulePage', selector: 'removeSharedCommunicationSchedule'},
        {ref: 'uniFormErrorMessage', selector: '#form-errors-shared-schedules'},
        {ref: 'warningMessage', selector: '#warningMessageSchedules'},
        {ref: 'sharedCommunicationScheduleSelectionGrid', selector: '#sharedCommunicationScheduleSelectionGrid'},
        {ref: 'sharedCommunicationSchedulePreview', selector: '#sharedCommunicationSchedulePreview'},
        {ref: 'sharedCommunicationSchedulePreviewForm', selector: '#sharedCommunicationSchedulePreviewForm'},
        {ref: 'deviceCommunicationPlanning', selector: 'deviceCommunicationPlanning'}
    ],

    REQUEST_TIMEOUT: 180000,
    deviceName: undefined,

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
            '#mdc-device-communication-planning-add-schedule': {
                click: this.onAddSchedule
            },
            '#mdc-device-communication-planning-remove-schedule': {
                click: this.onRemoveSchedule
            },
            '#mdc-device-communication-planning-change-schedule': {
                click: this.onEditSchedule
            },
            '#mdc-device-communication-planning-runDeviceComTask': {
                click: this.onRunSchedule
            },
            '#mdc-device-communication-planning-runDeviceComTaskNow': {
                click: this.onRunNowSchedule
            },
            '#mdc-device-communication-planning-activate-task': {
                click: this.activateComTask
            },
            '#mdc-device-communication-planning-deactivate-task': {
                click: this.deactivateComTask
            },
            '#device-schedule-add-addButton': {
                click: this.addOrEditSchedule
            },
            '#device-schedule-add-cancelLink': {
                click: this.onCancelAddSchedule
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

    showDeviceCommunicationPlanning: function (deviceName) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.deviceName = deviceName;

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceName, {
            success: function (device) {
                var scheduleStore = me.getDeviceSchedulesStore();
                scheduleStore.getProxy().setExtraParam('deviceName', deviceName);
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

    showAddSharedSchedule: function (deviceName) {
        var me = this,
            widget,
            availableScheduleStore = this.getAvailableCommunicationSchedulesForDeviceStore();

        me.deviceName = deviceName;
        widget = Ext.widget('addSharedCommunicationSchedule', {deviceName: me.deviceName, store: availableScheduleStore});
        availableScheduleStore.getProxy().setExtraParam('filter', Ext.encode([
            {property: 'deviceName', value: deviceName},
            {property: 'available', value: true}
        ]));
        availableScheduleStore.load({
            callback: function () {
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#addSharedScheduleButtonForm').setVisible(availableScheduleStore.getCount() !== 0);
                widget.down('uni-form-info-message').setVisible(availableScheduleStore.getCount() !== 0);
            }
        });

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceName, {
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

    showRemoveSharedSchedule: function (deviceName) {
        var me = this,
            usedSchedulesStore = me.getUsedCommunicationSchedulesForDeviceStore(),
            widget;

        me.deviceName = deviceName;
        widget = Ext.widget('removeSharedCommunicationSchedule', {deviceName: deviceName, store: usedSchedulesStore});
        usedSchedulesStore.getProxy().setExtraParam('filter', Ext.encode([
            {property: 'deviceId', value: deviceName}
        ]));
        usedSchedulesStore.load({
            callback: function (records, operation, success) {
                widget.down('#removeSharedScheduleButtonForm').setVisible(usedSchedulesStore.getCount() !== 0);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceName, {
            success: function (device) {
                widget.device = device;
                me.getApplication().fireEvent('loadDevice', device);
            }
        });
    },

    previewCommunicationSchedule: function (grid, record) {
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
            deviceName = device.get('name'),
            route = me.getController('Uni.controller.history.Router').getRoute('devices/device/communicationschedules');

        if (this.checkValidSelection(communicationSchedules, true)) {
            Ext.each(communicationSchedules, function (communicationSchedule) {
                scheduleIds.push(communicationSchedule.get('id'));
            });

            Ext.Ajax.request({
                url: '/api/ddr/devices/' + encodeURIComponent(deviceName) + '/sharedschedules',
                method: 'PUT',
                params: '',
                jsonData: {
                    device: _.pick(device.getRecordData(), 'name', 'version', 'parent'),
                    scheduleIds: scheduleIds
                },
                timeout: me.REQUEST_TIMEOUT,
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
            deviceName = device.get('name'),
            route = me.getController('Uni.controller.history.Router').getRoute('devices/device/communicationschedules');

        if (this.checkValidSelection(communicationSchedules, false)) {
            Ext.each(communicationSchedules, function (communicationSchedule) {
                scheduleIds.push(communicationSchedule.get('id'));
            });

            Ext.Ajax.request({
                url: '/api/ddr/devices/' + encodeURIComponent(deviceName) + '/sharedschedules',
                method: 'DELETE',
                params: '',
                jsonData: {
                    device: _.pick(device.getRecordData(), 'name', 'version', 'parent'),
                    scheduleIds: scheduleIds
                },
                timeout: me.REQUEST_TIMEOUT,
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
        if (selection.length > 1) {
            this.getSharedCommunicationSchedulePreview().show();
        }
    },

    onRemovedSharedComScheduleSelectionChange: function (grid, selection) {
        if (selection.length > 1) {
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
    },

    onAddSchedule: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            gridRecord = me.getDeviceCommunicationPlanningGrid().getSelectionModel().getSelection()[0];

        router.getRoute('devices/device/communicationschedules/addSchedule').forward({comTaskId: gridRecord.get('comTask').id});
    },

    showAddSchedule: function (deviceName, comTaskId) {
        var me = this,
            comTaskModel = Ext.ModelManager.getModel('Mdc.model.DeviceSchedule'),
            comTaskName = undefined;

        comTaskModel.getProxy().setUrl(deviceName);
        comTaskModel.load(comTaskId, {
            success: function (comTask) {
                comTaskName = comTask.get('comTask').name;

                Ext.ModelManager.getModel('Mdc.model.Device').load(deviceName, {
                    success: function (device) {
                        me.getApplication().fireEvent('loadDevice', device);

                        widget = Ext.widget('device-schedule-add', {
                            device: device,
                            comTaskId: comTaskId,
                            title: Uni.I18n.translate('deviceCommunicationPlanning.addScheduleToX', 'MDC', "Add schedule to '{0}'", comTaskName)
                        });
                        me.getApplication().fireEvent('changecontentevent', widget);
                    }
                });
            }
        });
    },

    addOrEditSchedule: function (button) {
        var me = this,
            scheduleField = me.getScheduleField(),
            addScheduleView = me.getAddScheduleView(),
            deviceName = addScheduleView.device.get('name'),
            jsonData,
            request = {};

        if (button.action === 'addScheduleAction') {
            request.id = addScheduleView.comTaskId;
            request.schedule = scheduleField.getValue();
            jsonData = Ext.encode(request);
            Ext.Ajax.request({
                url: '/api/ddr/devices/' + encodeURIComponent(deviceName) + '/schedules',
                method: 'POST',
                params: '',
                jsonData: jsonData,
                timeout: me.REQUEST_TIMEOUT,
                success: function (response) {
                    me.showDeviceCommunicationPlanning(deviceName);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationPlanning.addScheduleSucceeded', 'MDC', 'Schedule added.'));
                    me.navigateToCommunicationPlanning();
                }
            });
        } else {
            request.id = addScheduleView.comTask.get('id');
            request.version = addScheduleView.comTask.get('version');
            request.parent = addScheduleView.comTask.get('parent');
            request.schedule = scheduleField.getValue();
            jsonData = Ext.encode(request);
            Ext.Ajax.request({
                url: '/api/ddr/devices/' + encodeURIComponent(deviceName) + '/schedules',
                isNotEdit: true,
                method: 'PUT',
                params: '',
                jsonData: jsonData,
                timeout: me.REQUEST_TIMEOUT,
                success: function (response) {
                    me.showDeviceCommunicationPlanning(deviceName);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationPlanning.editScheduleSucceeded', 'MDC', 'Schedule saved.'));
                }
            });
        }
    },

    onEditSchedule: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            gridRecord = me.getDeviceCommunicationPlanningGrid().getSelectionModel().getSelection()[0];

        router.getRoute('devices/device/communicationschedules/editSchedule').forward({comTaskId: gridRecord.get('comTask').id});
    },

    showEditSchedule: function (deviceName, comTaskId) {
        var me = this,
            comTaskModel = Ext.ModelManager.getModel('Mdc.model.DeviceSchedule'),
            comTaskName = undefined;

        comTaskModel.getProxy().setUrl(deviceName);
        comTaskModel.load(comTaskId, {
            success: function (comTask) {
                comTaskName = comTask.get('comTask').name;

                Ext.ModelManager.getModel('Mdc.model.Device').load(deviceName, {
                    success: function (device) {
                        me.getApplication().fireEvent('loadDevice', device);

                        widget = Ext.widget('device-schedule-add', {
                            device: device,
                            comTaskId: comTaskId,
                            title: Uni.I18n.translate('deviceCommunicationPlanning.editScheduleOfX', 'MDC', "Edit schedule of '{0}'", comTaskName),
                            editMode: true,
                            comTask: comTask
                        });
                        widget.down('#device-command-add-form').loadRecord(comTask);
                        me.getApplication().fireEvent('changecontentevent', widget);
                    }
                });
            }
        });
    },

    navigateToCommunicationPlanning: function () {
        this.getController('Uni.controller.history.Router').getRoute('devices/device/communicationschedules').forward();
    },

    onCancelAddSchedule: function () {
        this.navigateToCommunicationPlanning();
    },

    onRemoveSchedule: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceName = router.arguments.deviceId,
            gridRecord = me.getDeviceCommunicationPlanningGrid().getSelectionModel().getSelection()[0];

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceCommunicationPlanning.removeScheduleConfirmation.msg', 'MDC', 'This communication task will no longer be executed according to this schedule.'),
            title: Ext.String.format(Uni.I18n.translate('deviceCommunicationPlanning.removeScheduleConfirmation.title', 'MDC', "Remove schedule from '{0}'?"), gridRecord.get('comTask').name),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.removeCommunicationSchedule(gridRecord, deviceName);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    removeCommunicationSchedule: function (record, deviceName) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/ddr/devices/' + deviceName + '/schedules',
            isNotEdit: true,
            method: 'PUT',
            params: '',
            jsonData: _.pick(record.getRecordData(), 'id', 'version', 'parent', 'name'),
            timeout: me.REQUEST_TIMEOUT,
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationPlanning.removeScheduleSucceeded', 'MDC', 'Schedule removed.'));
                me.navigateToCommunicationPlanning();
            }
        });
    },

    onRunSchedule: function () {
        this.applyActionOnComTask('run');
    },

    onRunNowSchedule: function () {
        this.applyActionOnComTask('runnow');
    },

    activateComTask: function () {
        this.applyActionOnComTask('activate');
    },

    deactivateComTask: function () {
        this.applyActionOnComTask('deactivate');
    },

    applyActionOnComTask: function (action) {
        var me = this,
            gridRecord = me.getDeviceCommunicationPlanningGrid().getSelectionModel().getSelection()[0],
            ackMessage = undefined;

        switch (action) {
            case 'run':
                ackMessage = Uni.I18n.translate('deviceCommunicationPlanning.runSuccess', 'MDC', 'Run succeeded.');
                break;
            case 'runnow':
                ackMessage = Uni.I18n.translate('deviceCommunicationPlanning.runNowSuccess', 'MDC', 'Run now succeeded.');
                break;
            case 'activate':
                ackMessage = Uni.I18n.translate('deviceCommunicationPlanning.activated', 'MDC', 'Communication task activated');
                break;
            case 'deactivate':
                ackMessage = Uni.I18n.translate('deviceCommunicationPlanning.deactivated', 'MDC', 'Communication task deactivated');
                break;
        }

        me.executePut('/api/ddr/devices/' + encodeURIComponent(gridRecord.get('parent').id) + '/comtasks/' + gridRecord.get('comTask').id + '/' + action, ackMessage);
    },

    executePut: function (actionUrl, actionMsg) {
        var me = this,
            request = {},
            device = me.getDeviceCommunicationPlanning().device,
            router = me.getController('Uni.controller.history.Router');

        Ext.Ajax.request({
            url: actionUrl,
            method: 'PUT',
            params: '',
            isNotEdit: true,
            jsonData: Ext.merge(request, {device: _.pick(device.getRecordData(), 'name', 'version', 'parent')}),
            timeout: me.REQUEST_TIMEOUT,
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', actionMsg);
                router.getRoute().forward();
            }
        });
    }

});