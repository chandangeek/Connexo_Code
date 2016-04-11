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
        'Uni.model.timeofuse.Calendar'
    ],

    refs: [],

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
            });

        me.deviceTypeId = deviceTypeId;
        me.getApplication().fireEvent('changecontentevent', view);
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
            view = Ext.widget('tou-available-cal-setup', {
                deviceTypeId: deviceTypeId
            });

        me.getApplication().fireEvent('changecontentevent', view);
        me.deviceTypeId = deviceTypeId;
        view.setLoading(true);
        view.suspendLayouts();
        me.reconfigureMenu(deviceTypeId, view);
    },

    addAvailableCalendar: function () {
        //todo: switch from stores
    },

    cancelAddCalendar: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/timeofuse', {deviceTypeId: me.deviceTypeId}).forward();
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
        }
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