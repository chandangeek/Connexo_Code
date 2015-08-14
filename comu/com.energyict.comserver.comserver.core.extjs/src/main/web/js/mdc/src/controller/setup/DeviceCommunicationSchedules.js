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
        'Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedule',
        'Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationScheduleGrid',
        'Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationSchedulePreview',
        'Mdc.view.setup.devicecommunicationschedule.AddSchedulePopUp'
    ],

    stores: [
        'DeviceSchedules',
        'CommunicationTaskConfigsOfDeviceConfiguration',
        'AvailableCommunicationSchedulesForDevice'
    ],

    refs: [
        {ref: 'addSharedCommunicationScheduleGrid', selector: '#addSharedCommunicationScheduleGrid'},
        {ref: 'addSharedCommunicationSchedulePreview', selector: '#addSharedCommunicationSchedulePreview'},
        {ref: 'addSharedCommunicationSchedulePreviewForm', selector: '#addSharedCommunicationSchedulePreviewForm'},
        {ref: 'sharedCommunicationScheduleGrid', selector: '#sharedCommunicationScheduleGrid'},
        {ref: 'onRequestCommunicationScheduleGrid', selector: '#onRequestComtaskGrid'},
        {ref: 'individualCommunicationScheduleGrid', selector: '#individualComtaskGrid'},
        {ref: 'scheduleField', selector: '#scheduleField'},
        {ref: 'warningMessage', selector: '#warningMessage'},
        {ref: 'addButton', selector: '#addButton'},
        {ref: 'uniFormErrorMessage', selector: '#form-errors'}

    ],

    init: function () {
        this.control({
            '#deviceCommunicationScheduleSetup button[action=addSharedCommunicationSchedule]': {
                click: this.addSharedCommunicationScheduleHistory
            },
            'menuitem[action=addCommunicationSchedule]': {
                click: this.addCommunicationSchedule
            },
            'menuitem[action=runCommunicationSchedule]': {
                click: this.runCommunicationSchedule
            },
            'menuitem[action=removeSharedCommunicationSchedule]': {
                click: this.removeSharedCommunicationScheduleConfirmation
            }, 'menuitem[action=changeCommunicationSchedule]': {
                click: this.changeCommunicationSchedule
            },
            'menuitem[action=removeCommunicationSchedule]': {
                click: this.removeCommunicationScheduleConfirmation
            },
            '#addSharedCommunicationScheduleGrid': {
                selectionchange: this.previewDeviceCommunicationSchedule
            },
            '#addSharedScheduleButtonForm button[action=addAction]': {
                click: this.saveSharedSchedule
            }, '#addSharedScheduleButtonForm button[action=cancelAction]': {
                click: this.cancelSharedScheduleHistory
            },
            'button[action=addIndividualScheduleAction]': {
                click: this.saveIndividualSchedule
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

    addSharedCommunicationScheduleHistory: function () {
        location.href = '#/devices/' + encodeURIComponent(this.mrid) + '/communicationplanning/add';
    },

    cancelSharedScheduleHistory: function () {
        location.href = '#/devices/' + encodeURIComponent(this.mrid) + '/communicationplanning';
    },

    addSharedCommunicationSchedule: function (mrid) {
        var me = this;
        this.mrid = mrid;
        var widget = Ext.widget('addSharedCommunicationSchedule', {mRID: this.mrid});

        var availableScheduleStore = this.getAvailableCommunicationSchedulesForDeviceStore();
        widget.down('#addSharedCommunicationScheduleGrid').reconfigure(availableScheduleStore);
        availableScheduleStore.getProxy().pageParam = false;
        availableScheduleStore.getProxy().limitParam = false;
        availableScheduleStore.getProxy().startParam = false;
        availableScheduleStore.getProxy().setExtraParam('filter', Ext.encode([
            {
                property: 'mrid',
                value: mrid
            },
            {
                property: 'available',
                value: true
            }
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

        Ext.ModelManager.getModel('Mdc.model.Device').load(mrid, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
            }
        });
    },

    previewDeviceCommunicationSchedule: function () {
        var communicationSchedules = this.getAddSharedCommunicationScheduleGrid().getSelectionModel().getSelection();
        var me = this;
        me.getWarningMessage().setVisible(false);
        me.getUniFormErrorMessage().hide();
        if (communicationSchedules.length === 1) {
            this.getAddSharedCommunicationSchedulePreview().setTitle(communicationSchedules[0].get('name'));
            this.getAddSharedCommunicationSchedulePreview().setVisible(true);
            this.getAddSharedCommunicationSchedulePreviewForm().loadRecord(communicationSchedules[0]);
            this.getAddSharedCommunicationSchedulePreviewForm().down('#comTaskPreviewContainer').removeAll();
            if (communicationSchedules[0].comTaskUsages().data.items.length === 0) {
                me.getAddSharedCommunicationSchedulePreviewForm().down('#comTaskPreviewContainer').add({
                    xtype: 'displayfield',
                    htmlEncode: false
                });
            } else {
                Ext.each(communicationSchedules[0].comTaskUsages().data.items, function (comTaskUsage) {
                    me.getAddSharedCommunicationSchedulePreviewForm().down('#comTaskPreviewContainer').add({
                        xtype: 'displayfield',
                        value: '<li>' + comTaskUsage.get('name') + '</li>',
                        htmlEncode: false
                    })
                });
            }

        }
        else {
            var valuesToCheck = [];
            Ext.each(communicationSchedules, function (item) {
                valuesToCheck.push.apply(valuesToCheck, item.get('comTaskUsages'));
            });
            if (_.uniq(valuesToCheck,function (item) {
                return item.id;
            }).length === valuesToCheck.length) {
                me.getUniFormErrorMessage().hide();
                me.getWarningMessage().setVisible(false);
            } else {
                me.getUniFormErrorMessage().show();
                me.getWarningMessage().update('<span style="color:red">' + Uni.I18n.translate('deviceCommunicationSchedule.ComTaskOverlap', 'MDC', 'The current selection has overlapping communication tasks.') + '</span>');
                me.getWarningMessage().setVisible(true);
            }
        }
    },

    saveSharedSchedule: function () {
        var me = this;
        var communicationSchedules = this.getAddSharedCommunicationScheduleGrid().getSelectionModel().getSelection();
        var scheduleIds = [];
        var jsonData;
        var request = {};

        if (this.checkValidSelection(communicationSchedules)) {
            Ext.each(communicationSchedules, function (communicationSchedule) {
                scheduleIds.push(communicationSchedule.get('id'));
            });
            request.deviceMRIDs = [this.mrid];
            request.scheduleIds = scheduleIds;
            jsonData = Ext.encode(request);
            Ext.Ajax.request({
                url: '/api/ddr/devices/schedules',
                method: 'PUT',
                params: '',
                jsonData: jsonData,
                timeout: 180000,
                success: function (response) {
                    location.href = '#/devices/' + encodeURIComponent(me.mrid) + '/communicationplanning';
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationSchedule.addSharedScheduleSucceeded', 'MDC', 'Add shared communication schedule succeeded'));
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
    },

    removeSharedCommunicationScheduleConfirmation: function () {
        var me = this;
        var record = this.getSharedCommunicationScheduleGrid().getSelectionModel().getSelection()[0];
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceCommunicationSchedule.deleteConfirmationSharedSchedule.msg', 'MDC', 'This shared communication schedule will no longer be available on this device.'),
            title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', [record.get('name')]),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.removeSharedCommunicationSchedule(record);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    removeSharedCommunicationSchedule: function (record) {
        var me = this;
        var jsonData;
        var request = {};
        jsonData = Ext.encode(request);
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(this.mrid) + '/schedules/' + record.getId(),
            method: 'DELETE',
            params: '',
            jsonData: jsonData,
            timeout: 180000,
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('communicationSchedule.removed', 'MDC', 'Shared communication schedule removed'));
                me.showDeviceCommunicationScheduleView(me.mrid);
            }
        });
    },

    addCommunicationSchedule: function () {
        var me = this;
        var widget = Ext.widget('addSchedulePopUp', {action: 'addIndividualScheduleAction'});
        var comTask = this.getOnRequestCommunicationScheduleGrid().getSelectionModel().getSelection()[0];
        widget.setTitle(Uni.I18n.translate('deviceCommunicationSchedule.addFrequency', 'MDC', 'Add frequency to communication task ') + "'" + comTask.get('comTaskInfos')[0].name + "'");
        widget.show();

    },

    changeCommunicationSchedule: function () {
        var me = this;
        var widget = Ext.widget('addSchedulePopUp', {action: 'changeIndividualScheduleAction'});
        var comTask = this.getIndividualCommunicationScheduleGrid().getSelectionModel().getSelection()[0];
        widget.setTitle(Uni.I18n.translate('deviceCommunicationSchedule.changeFrequency', 'MDC', 'Change frequency of communication task ') + "'" + comTask.get('comTaskInfos')[0].name + "'");
        widget.down('#addScheduleForm').loadRecord(comTask);
        widget.show();
    },

    removeCommunicationScheduleConfirmation: function () {
        var me = this;
        var record = this.getIndividualCommunicationScheduleGrid().getSelectionModel().getSelection()[0];
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceCommunicationSchedule.deleteIndividualConfirmation.msg', 'MDC', 'This individual communication schedule will no longer be available.'),
            title: Ext.String.format(Uni.I18n.translate('deviceCommunicationSchedule.deleteConfirmation.title', 'MDC', 'Remove schedule from \'{0}\'?'), record.get('comTaskInfos')[0].name),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.removeCommunicationSchedule(record);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    removeCommunicationSchedule: function (record) {
        var me = this;
        var jsonData;
        var request = {};
        request.id = this.getIndividualCommunicationScheduleGrid().getSelectionModel().getSelection()[0].get('id');
        jsonData = Ext.encode(request);
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(me.mrid) + '/schedules',
            method: 'PUT',
            params: '',
            jsonData: jsonData,
            timeout: 180000,
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationSchedule.removeIndividualScheduleSucceeded', 'MDC', 'Individual communication schedule removed'))
                me.showDeviceCommunicationScheduleView(me.mrid);
            }
        });
    },

    saveIndividualSchedule: function (button) {
        var me = this;
        var scheduleField = this.getScheduleField();
        var jsonData;
        var request = {};
        if (this.getOnRequestCommunicationScheduleGrid().getSelectionModel().getSelection()[0] !== undefined) {
            request.id = this.getOnRequestCommunicationScheduleGrid().getSelectionModel().getSelection()[0].get('comTaskInfos')[0].id;
        }
        request.schedule = scheduleField.getValue();
        jsonData = Ext.encode(request);
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(me.mrid) + '/schedules',
            method: 'POST',
            params: '',
            jsonData: jsonData,
            timeout: 180000,
            success: function (response) {
                me.showDeviceCommunicationScheduleView(me.mrid);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationSchedule.addIndividualScheduleSucceeded', 'MDC', 'Individual communication schedule added'));
            }
        });
        button.up('.window').close();
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
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationSchedule.runScheduleSucceeded', 'MDC', 'Run shared communication schedule succeeded'));
                me.showDeviceCommunicationScheduleView(me.mrid);
            }
        });
    },

    updateIndividualSchedule: function (button) {
        var me = this;
        var scheduleField = this.getScheduleField();
        var jsonData;
        var request = {};
        request.id = this.getIndividualCommunicationScheduleGrid().getSelectionModel().getSelection()[0].get('id');
        request.schedule = scheduleField.getValue();
        jsonData = Ext.encode(request);
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(me.mrid) + '/schedules',
            method: 'PUT',
            params: '',
            jsonData: jsonData,
            timeout: 180000,
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommunicationSchedule.editIndividualScheduleSucceeded', 'MDC', 'Individual communication schedule saved'));
                me.showDeviceCommunicationScheduleView(me.mrid);
            }
        });
        button.up('.window').close();
    }

});
