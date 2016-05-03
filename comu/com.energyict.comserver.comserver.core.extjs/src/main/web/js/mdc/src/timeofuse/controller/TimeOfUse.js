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
        },
        {
            ref: 'calendarGrid',
            selector: 'tou-calendars-grid'
        },
        {
            ref: 'preview',
            selector: 'device-type-tou-setup tou-preview-panel'
        }
    ],

    deviceTypeId: null,
    init: function () {
        var me = this;

        me.control({
            'device-type-tou-setup #add-tou-calendars-btn': {
                click: me.goToAddCalendars
            },
            'device-type-tou-setup #tou-no-cal-add-btn': {
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
            },
            'device-type-tou-setup tou-calendars-grid': {
                select: this.showPreview
            }
        })
    },

    showTimeOfUseOverview: function (deviceTypeId) {
        var me = this,
            view,
            store = me.getStore('Mdc.timeofuse.store.UsedCalendars');

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                view = Ext.widget('device-type-tou-setup', {
                    deviceTypeId: deviceTypeId,
                    timeOfUseAllowed: deviceType.get('timeOfUseAllowed')
                });
                view.down('tou-devicetype-specifications-form').loadRecord(deviceType);
                me.deviceTypeId = deviceTypeId;
                me.getApplication().fireEvent('changecontentevent', view);
                store.getProxy().setUrl(deviceTypeId);
                store.load({
                    callback: function (records, operation, success) {
                        if(success === true) {
                            me.getCalendarGrid().down('pagingtoolbartop #displayItem').setText(
                                Uni.I18n.translatePlural('general.calendarCount', store.getCount(), 'MDC', 'No time of use calendars', '{0} time of use calendar', '{0} time of use calendars')
                            );
                        }
                    }
                });
                view.setLoading(true);
                view.suspendLayouts();
                me.reconfigureMenu(deviceType, view);
            },
            failure: function () {
                view.setLoading(false);
                view.resumeLayouts();
            }
        });
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
            view;

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                view = Ext.widget('tou-available-cal-setup', {
                    deviceTypeId: deviceTypeId,
                    timeOfUseAllowed: deviceType.get('timeOfUseAllowed')
                });
                store.getProxy().setUrl(me.deviceTypeId);
                store.load();
                me.getApplication().fireEvent('changecontentevent', view);
                me.deviceTypeId = deviceTypeId;
                view.setLoading(true);
                view.suspendLayouts();
                me.reconfigureMenu(deviceType, view);
            },
            failure: function () {
                view.setLoading(false);
                view.resumeLayouts();
            }
        });
    },

    addAvailableCalendar: function () {
        var me = this,
            grid = this.getUnusedCalendarGrid(),
            array = [];
        Ext.each(grid.getSelectionModel().getSelection(), function (calendarToAdd) {
            array.push(calendarToAdd.raw);
        });
        me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/timeofuse', {deviceTypeId: me.deviceTypeId}).forward();

        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/unusedcalendars',
            method: 'PUT',
            jsonData: Ext.encode(array)
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
            view;

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                view = Ext.widget('tou-devicetype-edit-specs-setup', {
                    deviceTypeId: deviceTypeId,
                    timeOfUseAllowed: deviceType.get('timeOfUseAllowed')
                });
                view.down('tou-devicetype-edit-specs-form').loadRecord(deviceType);
                me.getApplication().fireEvent('changecontentevent', view);
                me.deviceTypeId = deviceTypeId;
                view.setLoading(true);
                view.suspendLayouts();
                me.reconfigureMenu(deviceType, view);
            },
            failure: function () {
                view.setLoading(false);
                view.resumeLayouts();
            }
        });

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

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                view = Ext.widget('tou-devicetype-view-calendar-setup', {
                    url: '/api/dtc/devicetypes/' + deviceTypeId + '/timeofuse',
                    calendarId: calendarId,
                    deviceTypeId: deviceTypeId,
                    timeOfUseAllowed: deviceType.get('timeOfUseAllowed')
                });
                view.on('timeofusecalendarloaded', function (newRecord) {
                    me.getApplication().fireEvent('timeofusecalendarloaded', newRecord.get('name'))
                    return true;
                }, {single: true});
                me.deviceTypeId = deviceTypeId;
                me.getApplication().fireEvent('changecontentevent', view);
                view.setLoading(true);
                view.suspendLayouts();
                me.reconfigureMenu(deviceType, view);
            },
            failure: function () {
                view.setLoading(false);
                view.resumeLayouts();
            }
        });

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
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", calendarRecord.get('name')),
            fn: function (btn) {
                if (btn === 'confirm') {
                    calendarRecord.getProxy().setUrl(me.deviceTypeId);
                    calendarRecord.destroy();
                }
            }
        });

    },

    reconfigureMenu: function (deviceType, view) {
        var me = this;
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

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            previewForm = preview.down('devicetype-tou-preview-form');
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        if (record.get('ghost') !== true) {
            previewForm.fillFieldContainers(record.getCalendar());
        } else {
            previewForm.showEmptyMessage();
        }
        preview.down('tou-devicetype-action-menu').record = record;
    }


});