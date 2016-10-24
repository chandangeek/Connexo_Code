Ext.define('Mdc.timeofuse.controller.TimeOfUse', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.timeofuse.view.Setup',
        'Mdc.timeofuse.view.AvailableCalendarsSetup',
        'Mdc.timeofuse.view.SpecificationsForm',
        'Mdc.timeofuse.view.EditSpecificationsSetup',
        'Mdc.timeofuse.view.ViewCalendarSetup',
        'Mdc.timeofuse.view.EditSpecificationsForm',
        'Uni.view.error.NotFound'
    ],

    stores: [
        'Mdc.timeofuse.store.UsedCalendars',
        'Mdc.timeofuse.store.UnusedCalendars'
    ],

    models: [
        'Mdc.model.DeviceType',
        'Mdc.timeofuse.model.AllowedCalendar',
        'Uni.model.timeofuse.Calendar',
        'Mdc.timeofuse.model.TimeOfUseOptions',
        'Mdc.timeofuse.model.TimeOfUseOption'
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
        },
        {
            ref: 'editTOUSpecsView',
            selector: 'tou-devicetype-edit-specs-setup'
        },
        {
            ref: 'editForm',
            selector: '#tou-devicetype-edit-specs-form'
        },
        {
            ref: 'breadCrumbs',
            selector: 'breadcrumbTrail'
        }

    ],

    deviceTypeId: null,
    tab2Activate: undefined,
    calendarCount: 0,

    init: function () {
        var me = this;

        me.control({
                'device-type-tou-setup #add-tou-calendars-btn': {
                    click: me.goToAddCalendars
                },
                'device-type-tou-setup #tou-no-cal-add-btn': {
                    click: me.goToAddCalendars
                },
                'device-type-tou-setup #tou-no-cal-activate-btn': {
                    click: me.goToEditPage
                },
                'tou-available-cal-setup #btn-add-tou-calendars': {
                    click: me.addAvailableCalendar
                },
                'tou-available-cal-setup #btn-cancel-add-tou-calendars': {
                    click: me.goBackToCalendars
                },
                'tou-spec-action-menu': {
                    click: me.chooseSpecificationsAction
                },
                'tou-devicetype-action-menu': {
                    click: me.chooseAction
                },
                'device-type-tou-setup tou-calendars-grid': {
                    select: this.showPreview
                },
                'tou-devicetype-edit-specs-form #tou-save-specs-button': {
                    click: this.saveTOUSettings
                },
                'tou-devicetype-edit-specs-form #tou-edit-cancel-link': {
                    click: this.goBackToCalendars
                },
                '#tou-allowed-radio-field': {
                    change: this.disableEnableCheckboxes
                },
                '#device-type-tou-tab-panel': {
                    tabChange: this.updateCounter
                }
            }
        )
    },

    showTimeOfUseOverview: function (deviceTypeId) {
        var me = this,
            view,
            store = me.getStore('Mdc.timeofuse.store.UsedCalendars'),
            optionsModel = Ext.ModelManager.getModel('Mdc.timeofuse.model.TimeOfUseOptions');

        optionsModel.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                optionsModel.load(1, {
                    success: function (options) {
                        view = Ext.widget('device-type-tou-setup', {
                            deviceTypeId: deviceTypeId,
                            timeOfUseAllowed: options.get('isAllowed'),
                            timeOfUseSupported: options.supportedOptions().count() > 0,
                            tab2Activate: me.tab2Activate
                        });
                        view.down('tou-devicetype-specifications-form').fillOptions(options);
                        me.deviceTypeId = deviceTypeId;
                        me.getApplication().fireEvent('changecontentevent', view);
                        me.tab2Activate = undefined;
                        store.getProxy().setUrl(deviceTypeId);
                        store.load({
                            callback: function (records, operation, success) {
                                if (success === true) {
                                    me.calendarCount = store.getCount();
                                    me.updateCounter();
                                }
                            }
                        });
                        view.setLoading(true);
                        view.suspendLayouts();
                        me.reconfigureMenu(deviceType, view);
                    }

                });
            }
        })
    },

    updateCounter: function () {
        var me = this;

        me.getCalendarGrid().down('pagingtoolbartop #displayItem').setText(
            Uni.I18n.translatePlural('general.calendarCount', me.calendarCount, 'MDC', 'No time of use calendars', '{0} time of use calendar', '{0} time of use calendars')
        );
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
            view,
            optionsModel = Ext.ModelManager.getModel('Mdc.timeofuse.model.TimeOfUseOptions');

        optionsModel.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                optionsModel.load(1, {
                    success: function (options) {
                        if (options.get('isAllowed')) {
                            view = Ext.widget('tou-available-cal-setup', {
                                deviceTypeId: deviceTypeId
                            });
                            store.getProxy().setUrl(deviceTypeId);
                            store.load({
                                callback: function (records, operation, success) {
                                    if (records === null || records.length === 0) {
                                        view.down('#btn-add-tou-calendars').hide();
                                    }
                                }
                            });

                            me.deviceTypeId = deviceTypeId;
                            me.reconfigureMenu(deviceType, view);
                        } else {
                            view = Ext.widget('errorNotFound');
                            me.removeBreadcrumbs();
                        }
                        me.getApplication().fireEvent('changecontentevent', view);
                    }
                });
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
            view,
            optionsModel = Ext.ModelManager.getModel('Mdc.timeofuse.model.TimeOfUseOptions');

        optionsModel.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                optionsModel.load(deviceTypeId, {
                    success: function (options) {
                        if (options.supportedOptions().count() > 0) {
                            view = Ext.widget('tou-devicetype-edit-specs-setup', {
                                deviceTypeId: deviceTypeId,
                                timeOfUseAllowed: options.get('isAllowed')
                            });
                            view.down('#tou-devicetype-edit-specs-form').fillOptions(options);

                            me.deviceTypeId = deviceTypeId;
                            view.setLoading(true);
                            view.suspendLayouts();
                            me.reconfigureMenu(deviceType, view);
                        } else {
                            view = Ext.widget('errorNotFound');
                            me.removeBreadcrumbs();
                        }
                        me.getApplication().fireEvent('changecontentevent', view);
                    }
                });
            }
        });

    },

    removeBreadcrumbs: function () {
        var me = this;
        me.getBreadCrumbs().hide();
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
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                view = Ext.widget('tou-devicetype-view-calendar-setup', {
                    url: '/api/dtc/devicetypes/' + deviceTypeId + '/timeofuse',
                    calendarId: calendarId,
                    deviceTypeId: deviceTypeId,
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
            }
        });

    },


    chooseAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'viewpreview':
                me.forwardToCalendarView(menu.record.getCalendar().get('id'));
                break;
            case 'remove':
                me.showRemovalPopup(menu.record);
                break;
        }
    },

    showRemovalPopup: function (calendarRecord) {
        var me = this,
            store = me.getStore('Mdc.timeofuse.store.UsedCalendars');
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            cancelText: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
        }).show({
            msg: Uni.I18n.translate('timeofuse.removeMsg', 'MDC', 'You will no longer be able to send this time of use calendar to devices of this device type..'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", calendarRecord.get('name')),
            fn: function (btn) {
                if (btn === 'confirm') {
                    calendarRecord.getProxy().setUrl(me.deviceTypeId);
                    calendarRecord.destroy(
                        {
                            callback: function(record, operation, success) {
                                me.calendarCount = store.getCount();
                                me.updateCounter();
                            }
                        }
                    );
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
            previewForm = preview.down('devicetype-tou-preview-form'),
            model = Ext.ModelManager.getModel('Uni.model.timeofuse.Calendar');
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        if (record.get('ghost') !== true) {
            model.getProxy().setUrl('/api/dtc/devicetypes/' + me.deviceTypeId + '/timeofuse');
            previewForm.setLoading(true);
            model.load(record.getCalendar().get('id'), {
                success: function (calendar) {
                    previewForm.fillFieldContainers(calendar);
                    previewForm.setLoading(false);
                },
                failure: function () {
                    previewForm.setLoading(false);
                }
            });

        } else {
            previewForm.showEmptyMessage();
        }
        if (preview.down('tou-devicetype-action-menu')) {
            preview.down('tou-devicetype-action-menu').record = record;
        }
    },

    saveTOUSettings: function () {
        var me = this,
            form = me.getEditForm(),
            formErrorsPanel = form.down('#form-errors'),
            record,
            id;

        me.tab2Activate = 0;
        form.updateRecord();
        record = form.getRecord();
        record.allowedOptions().removeAll();
        formErrorsPanel.hide();
        form.down('#no-checkboxes-time-of-use-selected').hide();

        Ext.each(form.down('#tou-specs-options-form').getChecked(), function (checkedBox) {
            var me = this,
                record = me.getEditForm().getRecord(),
                option;
            id = checkedBox.inputValue;
            option = record.supportedOptions().findRecord('id', id, 0, false, true, true);
            record.allowedOptions().add(option);
        }, me);

        if (record.allowedOptions().count() <= 0) {
            formErrorsPanel.show();
            form.down('#no-checkboxes-time-of-use-selected').update(Uni.I18n.translate('general.fieldRequired', 'MDC', 'This field is required'));
            form.down('#no-checkboxes-time-of-use-selected').show();
        } else {
            record.save({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('tou.specs.save.success', 'MDC', 'Time of use specifications saved'));
                    me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/timeofuse', {deviceTypeId: me.deviceTypeId}).forward();
                },
                failure: function (record, operation) {
                    formErrorsPanel.show();
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                    }
                }
            });
        }
    },

    goBackToCalendars: function () {
        var me = this;
        me.tab2Activate = 0;
        me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/timeofuse', {deviceTypeId: me.deviceTypeId}).forward();
    },

    disableEnableCheckboxes: function (radioField, newValue) {
        var me = this,
            form = me.getEditForm();

        form.down('#tou-specs-options-form').setDisabled(!newValue);
    }
});