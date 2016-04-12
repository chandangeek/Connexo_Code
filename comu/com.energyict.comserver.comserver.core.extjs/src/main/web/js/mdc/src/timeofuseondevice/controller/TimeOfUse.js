Ext.define('Mdc.timeofuseondevice.controller.TimeOfUse', {
    extend: 'Ext.app.Controller',

    stores: [],

    views: [
        'Mdc.timeofuseondevice.view.Setup',
        'Mdc.timeofuseondevice.view.ViewCalendarSetup'
    ],

    models: [
        'Mdc.model.Device',
        'Uni.model.timeofuse.Calendar'
    ],

    init: function () {
        var me = this;
        this.control({
            'tou-device-action-menu': {
                click: me.chooseAction
            }
        });
    },

    showTimeOfUseOverview: function (mRID) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            view;

        deviceModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                view = Ext.widget('device-tou-setup', {
                    device: record
                });
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'activatecalendar':
                break;
            case 'cleartariff':
                break;
            case 'sendcalendar':
                break;
            case 'verifycalendars':
                break;
            case 'viewpreview':
                me.redirectToPreview(1);
                break;
        }
    },

    redirectToPreview: function (calendarId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('devices/device/timeofuse/viewpreview');
        route.forward(Ext.merge(router.arguments, {calendarId: calendarId}));
    },

    showPreviewCalendarView: function (mRID, calendarId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            view;

        deviceModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                view = Ext.widget('tou-device-view-calendar-setup', {
                    url: '/api/cal/calendars/timeofusecalendars',
                    calendarId: calendarId,
                    device: record
                });
                view.on('timeofusecalendarloaded', function (newRecord) {
                    me.getApplication().fireEvent('timeofusecalendarloaded', newRecord.get('name'))
                    return true;
                }, {single: true});
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    }
});




