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
        {ref: 'scheduleField', selector: '#scheduleField'}
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
        var me = this;
        this.mrid = mrid;


        var scheduleStore = me.getDeviceSchedulesStore();
        scheduleStore.getProxy().setUrl(mrid);
        scheduleStore.load({
            callback: function () {
                var widget = Ext.widget('deviceCommunicationScheduleSetup', {mrid: mrid});
                var shared = [];
                var individual = [];
                var adHocComTasks = [];
                debugger;
                scheduleStore.each(function (schedule) {
                    if (schedule.get('type') === 'INDIVIDUAL') {
                        individual.push(schedule);

                    } else if (schedule.get('type') === 'SCHEDULED') {
//                        shared.push({
//                            xtype: 'sharedCommunicationScheduleGrid',
//                            title: schedule.get('name'),
//                            schedule: schedule
//                        })
                        shared.push(schedule);
                    } else {
                        adHocComTasks.push(schedule);
                    }
                });

                // add shared component
                // widget.down('#sharedDeviceCommunicationScheduleSetupPanel').getCenterContainer().add(shared);
                if (shared.length > 0) {
                    var sharedGrid = {
                        xtype: 'sharedCommunicationScheduleGrid',
                        shared: shared
                    };
                    widget.down('#sharedDeviceCommunicationScheduleSetupPanel').getCenterContainer().add(sharedGrid);
                }

                //add individual component
                if (individual.length > 0) {
                    var individualGrid = {
                        xtype: 'individualCommunicationScheduleGrid',
                        individual: individual
                    };
                    widget.down('#individualDeviceCommunicationScheduleSetupPanel').getCenterContainer().add(individualGrid);
                }

                //add on request component
                if (adHocComTasks.length > 0) {
                    var onRequestGrid = {
                        xtype: 'onRequestCommunicationScheduleGrid',
                        onRequest: adHocComTasks
                    };
                    widget.down('#onRequestDeviceCommunicationScheduleSetupPanel').getCenterContainer().add(onRequestGrid);
                }

                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    addSharedCommunicationScheduleHistory: function () {
        location.href = '#/devices/' + this.mrid + '/communicationschedules/add';
    },

    cancelSharedScheduleHistory: function () {
        location.href = '#/devices/' + this.mrid + '/communicationschedules';
    },

    addSharedCommunicationSchedule: function (mrid) {
        var me = this;
        this.mrid = mrid;
        var widget = Ext.widget('addSharedCommunicationSchedule');

        var availableScheduleStore = this.getAvailableCommunicationSchedulesForDeviceStore();
        widget.down('#addSharedCommunicationScheduleGrid').reconfigure(availableScheduleStore);
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
                if(availableScheduleStore.getCount()===0){
                    widget.down('#addSharedScheduleButtonForm').setVisible(false);
                } else {
                    widget.down('#addSharedScheduleButtonForm').setVisible(true);
                }
            }
        });
    },

    previewDeviceCommunicationSchedule: function () {
        var communicationSchedules = this.getAddSharedCommunicationScheduleGrid().getSelectionModel().getSelection();
        var me = this;
        if (communicationSchedules.length == 1) {
            this.getAddSharedCommunicationSchedulePreview().setTitle(communicationSchedules[0].get('name'));
            this.getAddSharedCommunicationSchedulePreview().setVisible(true);
            this.getAddSharedCommunicationSchedulePreviewForm().loadRecord(communicationSchedules[0]);
            this.getAddSharedCommunicationSchedulePreviewForm().down('#comTaskPreviewContainer').removeAll();
            if (communicationSchedules[0].comTaskUsages().data.items.length === 0) {
                me.getAddSharedCommunicationSchedulePreviewForm().down('#comTaskPreviewContainer').add({
                    xtype: 'displayfield'
                });
            } else {
                Ext.each(communicationSchedules[0].comTaskUsages().data.items, function (comTaskUsage) {
                    me.getAddSharedCommunicationSchedulePreviewForm().down('#comTaskPreviewContainer').add({
                        xtype: 'displayfield',
                        value: '<li>' + comTaskUsage.get('name') + '</li>'
                    })
                });
            }

        }
    },

    saveSharedSchedule: function () {
        var me = this;
        var communicationSchedules = this.getAddSharedCommunicationScheduleGrid().getSelectionModel().getSelection();
        var scheduleIds = [];
        var jsonData;
        var request = {};
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
                location.href = '#/devices/' + me.mrid + '/communicationschedules';
            }
        });
    },

    removeSharedCommunicationScheduleConfirmation: function(){
        var me = this;
        var record = this.getSharedCommunicationScheduleGrid().getSelectionModel().getSelection()[0];
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceCommunicationSchedule.deleteConfirmationSharedSchedule.msg', 'MDC', 'This communication schedule will no longer be available on this device.'),
            title: Ext.String.format(Uni.I18n.translate('deviceCommunicationSchedule.deleteConfirmationSharedSchedule.title', 'MDC', 'Remove \'{0}\'?'), record.get('name')),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        this.close();
                        me.removeSharedCommunicationSchedule(record);
                        break;
                    case 'cancel':
                        this.close();
                        break;
                }
            }
        });
    },

    removeSharedCommunicationSchedule: function (record) {
        var me = this;
        // var scheduleIds = [ btn.up('panel').schedule.get('masterScheduleId')];
        var jsonData;
        var request = {};
        request.deviceMRIDs = [this.mrid];
        request.scheduleIds = [record.get('masterScheduleId')];
        jsonData = Ext.encode(request);
        Ext.Ajax.request({
            url: '/api/ddr/devices/schedules',
            method: 'DELETE',
            params: '',
            jsonData: jsonData,
            timeout: 180000,
            success: function (response) {
                me.showDeviceCommunicationScheduleView(me.mrid);
            }
        });
    },

    addCommunicationSchedule: function () {
        var me = this;
        var widget = Ext.widget('addSchedulePopUp', {action: 'addIndividualScheduleAction'});
        var comTask = this.getOnRequestCommunicationScheduleGrid().getSelectionModel().getSelection()[0];
        widget.setTitle(Uni.I18n.translate('deviceCommunicationSchedule.addSchedule', 'MDC', 'Add schedule to communication task') + "'" + comTask.get('comTaskInfos')[0].name + "'");

        widget.show();

    },

    changeCommunicationSchedule: function () {
        var me = this;
        var widget = Ext.widget('addSchedulePopUp', {action: 'changeIndividualScheduleAction'});
        var comTask = this.getIndividualCommunicationScheduleGrid().getSelectionModel().getSelection()[0];
        widget.setTitle(Uni.I18n.translate('deviceCommunicationSchedule.addSchedule', 'MDC', 'Add schedule to communication task') + "'" + comTask.get('comTaskInfos')[0].name + "'");
        widget.down('#addScheduleForm').loadRecord(comTask);
        widget.show();
    },

    removeCommunicationScheduleConfirmation: function(){
        var me = this;
        var record = this.getIndividualCommunicationScheduleGrid().getSelectionModel().getSelection()[0];
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceCommunicationSchedule.deleteConfirmation.msg', 'MDC', 'This communication schedule will no longer be available.'),
            title: Ext.String.format(Uni.I18n.translate('deviceCommunicationSchedule.deleteConfirmation.title', 'MDC', 'Remove schedule from \'{0}\'?'), record.get('comTaskInfos')[0].name),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        this.close();
                        me.removeCommunicationSchedule(record);
                        break;
                    case 'cancel':
                        this.close();
                        break;
                }
            }
        });
    },

    removeCommunicationSchedule: function(record){
        var me = this;
        var jsonData;
        var request = {};
        request.id = this.getIndividualCommunicationScheduleGrid().getSelectionModel().getSelection()[0].get('id');
        jsonData = Ext.encode(request);
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + me.mrid + '/schedules',
            method: 'PUT',
            method: 'PUT',
            params: '',
            jsonData: jsonData,
            timeout: 180000,
            success: function (response) {
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
            url: '/api/ddr/devices/' + me.mrid + '/schedules',
            method: 'POST',
            params: '',
            jsonData: jsonData,
            timeout: 180000,
            success: function (response) {
                me.showDeviceCommunicationScheduleView(me.mrid);
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
            url: '/api/ddr/devices/' + me.mrid + '/schedules',
            method: 'POST',
            params: '',
            jsonData: jsonData,
            timeout: 180000,
            success: function (response) {
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
            url: '/api/ddr/devices/' + me.mrid + '/schedules',
            method: 'PUT',
            params: '',
            jsonData: jsonData,
            timeout: 180000,
            success: function (response) {
                me.showDeviceCommunicationScheduleView(me.mrid);
            }
        });
        button.up('.window').close();
    }

});
