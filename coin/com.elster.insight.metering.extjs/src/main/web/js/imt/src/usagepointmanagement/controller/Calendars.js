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
        'Imt.usagepointmanagement.view.calendars.ActionMenu'

    ],

    refs: [
        {
            ref: 'calendarCombo',
            selector: '#calendar-combo'
        },
        {
            ref: 'form',
            selector: '#frm-add-user-directory'
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
            }
            //    '#usage-point-attributes #usage-point-attributes-actions-menu': {
            //        click: me.chooseAction
            //    }
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
                    // itemId: 'usage-point-metrology-configuration-details',
                    router: router,
                    usagePoint: usagePoint,
                    // meterRolesAvailable: usagePoint.get('metrologyConfiguration_meterRoles')3269-UP
                }));


            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    },


    addCalendar: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        usagePointsController.loadUsagePoint(mRID, {
            success: function (types, usagePoint) {
                me.usagePoint = usagePoint;
                me.getApplication().fireEvent('changecontentevent', Ext.widget('usage-point-calendar-add', {
                    // itemId: 'usage-point-metrology-configuration-details',
                    router: router,
                    usagePoint: usagePoint
                    // meterRolesAvailable: usagePoint.get('metrologyConfiguration_meterRoles')
                }));
                //   me.getStore('Imt.usagepointmanagement.store.CalendarCategories').load();

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
            values = this.getForm().getValues()
        Ext.Ajax.request({
            url: '../../api/udr/usagepoints/' + encodeURIComponent(btn.mRID) + '/calendars',
            method: 'POST',
            jsonData: {
                calendar: {
                    id: values.calendar
                },
                fromTime: values.activateCalendar === 'immediate-activation' ? new Date().getTime() : this.getForm().down('#activation-date-values').down('#activation-on').getValue().getTime()
            },
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePoint.acknowledge.updateSuccess', 'IMT', 'Usage point saved'));
                me.getController('Uni.controller.history.Router').getRoute('usagepoints/view/calendars').forward({mRID: btn.mRID});
            },
            failure: function (record, response) {
                if (!!response.response) {
                    var responseText = Ext.decode(response.response.responseText, true);

                    if (responseText && Ext.isArray(responseText.errors)) {
                        me.getForm().markInvalid(responseText.errors);
                    }
                }
            }
        });
    }


});