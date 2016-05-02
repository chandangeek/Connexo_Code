Ext.define('Mdc.timeofuse.controller.TimeOfUse', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.timeofuse.view.Setup',
        'Mdc.timeofuse.view.AvailableCalendarsSetup',
        'Mdc.timeofuse.view.SpecificationsForm',
        'Mdc.timeofuse.view.EditSpecificationsSetup',
        'Mdc.timeofuse.view.ViewCalendarSetup'
    ],

    stores: [
        'Mdc.timeofuse.store.UsedCalendars',
        'Mdc.timeofuse.store.UnusedCalendars'
    ],

    models: [
        'Mdc.model.DeviceType',
        'Mdc.timeofuse.model.AllowedCalendar',
        'Uni.model.timeofuse.Calendar'
    ],

    refs: [
        {
            ref: 'unusedCalendarGrid',
            selector: 'tou-available-cal-grd'
        }
    ],

    deviceTypeId: null,
    init: function () {
        var me = this;

        me.control({
            'device-type-tou-setup #add-tou-calendars-btn': {
                click: me.goToAddCalendars
            },
            'tou-available-cal-setup #btn-add-tou-calendars': {
                click: me.addAvailableCalendar
            },
            'tou-available-cal-setup #btn-cancel-add-tou-calendars': {
                click: me.cancelAddCalendar
            },
            'tou-spec-action-menu': {
                click: me.chooseSpecificationsAction
            },
            'tou-devicetype-action-menu': {
                click: me.chooseAction
            }
        })
    },

    showTimeOfUseOverview: function (deviceTypeId) {
        var me = this,
            view = Ext.widget('device-type-tou-setup', {
                deviceTypeId: deviceTypeId
            }),
            store = me.getStore('Mdc.timeofuse.store.UsedCalendars');

        me.deviceTypeId = deviceTypeId;
        me.getApplication().fireEvent('changecontentevent', view);
        store.getProxy().setUrl(deviceTypeId);
        store.load();
        view.setLoading(true);
        view.suspendLayouts();
        me.reconfigureMenu(deviceTypeId, view);
    },

    goToAddCalendars: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),

            route;

        route = router.getRoute('administration/devicetypes/view/timeofuse/add', {deviceTypeId: me.deviceTypeId});

        route.forward();
    },

    showAddCalendarsView: function (deviceTypeId) {
        var me = this,
            store = me.getStore('Mdc.timeofuse.store.UnusedCalendars'),
            view = Ext.widget('tou-available-cal-setup', {
                deviceTypeId: deviceTypeId
            });

        store.getProxy().setUrl(me.deviceTypeId);
        store.load();
        me.getApplication().fireEvent('changecontentevent', view);
        me.deviceTypeId = deviceTypeId;
        view.setLoading(true);
        view.suspendLayouts();
        me.reconfigureMenu(deviceTypeId, view);
    },

    addAvailableCalendar: function () {
        var me = this,
            grid = this.getUnusedCalendarGrid(),
            store = me.getStore('Mdc.timeofuse.store.CalendarsToAdd'),
            array = [];
        store.removeAll(true);
        Ext.each(grid.getSelectionModel().getSelection(),function(calendarToAdd){
            grid.getStore().remove(calendarToAdd);
            store.add(calendarToAdd);
            calendarToAdd.phantom = true;
            array.push(calendarToAdd.raw);
        });
        store.getProxy().setUrl(me.deviceTypeId);
       // store.save();
        me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/timeofuse', {deviceTypeId: me.deviceTypeId}).forward();

        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/unusedcalendars',
            method: 'PUT',
            jsonData: Ext.encode(array),
            success: function () {
                //var messageText = suspended
                //    ? Uni.I18n.translate('appServers.deactivateSuccessMsg', 'APR', 'Application server deactivated')
                //    : Uni.I18n.translate('appServers.activateSuccessMsg', 'APR', 'Application server activated');
                //me.getApplication().fireEvent('acknowledge', messageText);
                //router.getState().forward(); // navigate to the previously stored url
            },
            failure: function (response, request) {
                //if (response.status == 400) {
                //    var errorText = Uni.I18n.translate('appServers.error.unknown', 'APR', 'Unknown error occurred');
                //    if (!Ext.isEmpty(response.statusText)) {
                //        errorText = response.statusText;
                //    }
                //    if (!Ext.isEmpty(response.responseText)) {
                //        var json = Ext.decode(response.responseText, true);
                //        if (json && json.error) {
                //            errorText = json.error;
                //        }
                //    }
                //    var titleText = suspended
                //        ? Uni.I18n.translate('appServers.deactivate.operation.failed', 'APR', 'Deactivate operation failed')
                //        : Uni.I18n.translate('appServers.activate.operation.failed', 'APR', 'Activate operation failed');
                //
                //    me.getApplication().getController('Uni.controller.Error').showError(titleText, errorText);
                //}
            }
        });
    },

    cancelAddCalendar: function () {
        var me = this;
        me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/timeofuse', {deviceTypeId: me.deviceTypeId}).forward();
    },

    chooseSpecificationsAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'editspecifications':
                me.goToEditPage();
                break;
        }
    },

    goToEditPage: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('administration/devicetypes/view/timeofuse/edit', {deviceTypeId: me.deviceTypeId});
        route.forward();
    },

    showEditSpecificationsScreen: function (deviceTypeId) {
        var me = this,
            view = Ext.widget('tou-devicetype-edit-specs-setup', {
                deviceTypeId: deviceTypeId
            });
        me.getApplication().fireEvent('changecontentevent', view);
        me.deviceTypeId = deviceTypeId;
        view.setLoading(true);
        view.suspendLayouts();
        me.reconfigureMenu(deviceTypeId, view);

    },

    forwardToCalendarView: function (calendarId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('administration/devicetypes/view/timeofuse/viewpreview');
        route.forward(Ext.merge(router.arguments, {calendarId: calendarId}));
    },

    showPreviewCalendarView: function (deviceTypeId, calendarId) {
        var me = this,
            view;
        view = Ext.widget('tou-devicetype-view-calendar-setup', {
            url: '/api/cal/calendars/timeofusecalendars',
            calendarId: calendarId,
            deviceTypeId: deviceTypeId
        });

        view.on('timeofusecalendarloaded', function (newRecord) {
            me.getApplication().fireEvent('timeofusecalendarloaded', newRecord.get('name'))
            return true;
        }, {single: true});
        me.deviceTypeId = deviceTypeId;
        me.getApplication().fireEvent('changecontentevent', view);
        view.setLoading(true);
        view.suspendLayouts();
        me.reconfigureMenu(deviceTypeId, view);

    },


    chooseAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'viewpreview':
                me.forwardToCalendarView(1);
                break;
            case 'remove':
                me.showRemovalPopup(menu.record);
                break;
        }
    },

    showRemovalPopup: function (calendarRecord) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            cancelText: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
        }).show({
            msg: Uni.I18n.translate('timeofuse.removeMsg', 'MDC', 'You will no longer be able to send this time of use calendar to devices of this device type..'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", 'timeOfUseCalendar'),
            fn: function (btn) {
                if (btn === 'confirm') {
                    calendarRecord.getProxy().setUrl(me.deviceTypeId);
                    calendarRecord.destroy();
                }
            }
        });

    },

    reconfigureMenu: function (deviceTypeId, view) {
        var me = this;
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                if (view.down('deviceTypeSideMenu')) {
                    view.down('deviceTypeSideMenu').setDeviceTypeLink(deviceType.get('name'));
                    view.down('deviceTypeSideMenu #conflictingMappingLink').setText(
                        Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', deviceType.get('deviceConflictsCount'))
                    );
                }

                view.setLoading(false);
                view.resumeLayouts();
            },
            failure: function () {
                view.setLoading(false);
                view.resumeLayouts();
            }
        });
    }


});