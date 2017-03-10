/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.controller.Calendars', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],

    stores: [
        'Imt.usagepointmanagement.store.ActiveCalendars',
        'Imt.usagepointmanagement.store.CalendarCategories',
        'Imt.usagepointmanagement.store.CalendarsForCategory'
    ],

    models: [],

    views: [
        'Imt.usagepointmanagement.view.calendars.Details',
        'Imt.usagepointmanagement.view.calendars.Add',
        'Imt.usagepointmanagement.view.calendars.ActionMenu',
        'Imt.usagepointmanagement.view.calendars.PreviewCalendar'

    ],

    refs: [
        {
            ref: 'calendarCombo',
            selector: '#calendar-combo'
        },
        {
            ref: 'form',
            selector: '#frm-add-user-directory'
        },
        {
            ref: 'calendarGrid',
            selector: 'active-calendars-grid'
        }
    ],

    init: function () {
        var me = this;
        me.control({
            '#category-name': {
                select: me.categorySelected
            },
            'usage-point-calendar-add #add-button': {
                click: me.saveCalendar
            },
            'calendarActionMenu': {
                click: me.chooseAction
            }
        });
    },

    showCalendars: function (mRID) {
        var me = this,
            resultSet,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');
        usagePointsController.loadUsagePoint(mRID, {
            success: function (types, usagePoint) {
                me.usagePoint = usagePoint;
                var calendars = me.getStore('Imt.usagepointmanagement.store.ActiveCalendars');
                calendars.setMrid(mRID);
                calendars.load();
                me.getApplication().fireEvent('changecontentevent', Ext.widget('usage-point-calendar-configuration-details', {
                    router: router,
                    usagePoint: usagePoint,
                }));


            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    },


    addCalendar: function (usagePointname) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        usagePointsController.loadUsagePoint(usagePointname, {
            success: function (types, usagePoint) {
                me.usagePoint = usagePoint;
                me.getApplication().fireEvent('changecontentevent', Ext.widget('usage-point-calendar-add', {
                    router: router,
                    usagePoint: usagePoint
                }));
            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    },

    categorySelected: function (value) {
        var calendarStore = Ext.getStore('Imt.usagepointmanagement.store.CalendarsForCategory');
        calendarStore.filter([
            {
                property: 'status',
                value: 'ACTIVE'
            },
            {
                property: 'category',
                value: value.getValue()
            }
        ]);
        this.getCalendarCombo().enable();
    },

    saveCalendar: function (btn) {
        var me = this,
            values = this.getForm().getValues();

        Ext.Ajax.request({
            url: '../../api/udr/usagepoints/' + encodeURIComponent(btn.usagePointname) + '/calendars',
            method: 'POST',
            jsonData: {
                calendar: {
                    id: values.calendar
                },
                immediately: values.activateCalendar === 'immediate-activation',
                fromTime: values.activateCalendar === 'immediate-activation' ? new Date().getTime() : this.getForm().down('#activation-date-values').down('#activation-on').getValue().getTime()
            },
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePoint.acknowledge.calendarAdded', 'IMT', 'Calendar added'));
                me.getController('Uni.controller.history.Router').getRoute('usagepoints/view/calendars').forward({mRID: btn.usagePointname});
            },
            failure: function (response) {
                var responseText = Ext.decode(response.responseText, true);
                if (responseText && Ext.isArray(responseText.errors)) {
                    me.getForm().form.markInvalid(responseText.errors);
                    me.getForm().down('#form-errors').show();
                    var fromTimeError = Ext.Array.findBy(responseText.errors, function (item) { return item.id == 'fromTime';});
                    if(fromTimeError) {
                        me.getForm().down('#error-label').setText(fromTimeError.msg);
                        me.getForm().down('#error-label').show();
                    }
                }
            }
        });
    },

    chooseAction: function (menu, item) {
        var record = this.getCalendarGrid().getSelectionModel().getLastSelected();
        switch (item.action) {
            case 'viewPreview':
                this.getController('Uni.controller.history.Router').getRoute('usagepoints/view/calendars/preview').forward({
                    mRID: this.usagePoint.get('name'),
                    calendarId: record.getCalendar().get('id')
                })
                break;
            case 'viewTimeline':
                this.getController('Uni.controller.history.Router').getRoute('usagepoints/view/history').forward({mRID: this.usagePoint.get('name')});
                break;
        }
    },

    previewCalendar: function (usagePointname, calendarId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');
        usagePointsController.loadUsagePoint(usagePointname, {
            success: function (types, usagePoint) {
                me.usagePoint = usagePoint;
                var calendars = me.getStore('Imt.usagepointmanagement.store.ActiveCalendars');
                calendars.setMrid(usagePointname);
                calendars.load();
                me.getApplication().fireEvent('changecontentevent', Ext.widget('usagepoint-view-calendar-setup', {
                    url: '/api/udr/usagepoints/' + usagePointname + '/calendars/',
                    calendarId: calendarId,
                    router: router,
                    usagePoint: usagePoint
                }));


            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    }


});