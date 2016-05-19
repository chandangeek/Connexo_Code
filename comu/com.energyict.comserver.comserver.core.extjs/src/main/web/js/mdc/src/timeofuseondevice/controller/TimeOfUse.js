Ext.define('Mdc.timeofuseondevice.controller.TimeOfUse', {
    extend: 'Ext.app.Controller',

    stores: [],

    views: [
        'Mdc.timeofuseondevice.view.Setup',
        'Mdc.timeofuseondevice.view.ViewCalendarSetup'
    ],

    models: [
        'Mdc.model.Device',
        'Uni.model.timeofuse.Calendar',
        'Mdc.timeofuseondevice.model.CalendarOnDevice',
        'Mdc.timeofuseondevice.model.NextPassiveCalendar'
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
            reader = Ext.create('Ext.data.reader.Json', {
                model: 'Mdc.timeofuseondevice.model.CalendarOnDevice'
            }),
            result,
            resultSet,
            view;

        deviceModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                view = Ext.widget('device-tou-setup', {
                    device: record
                });
                me.getApplication().fireEvent('changecontentevent', view);
                if (view.down('#wrappingPanel')) {
                    view.down('#wrappingPanel').setLoading(true);
                }
                Ext.Ajax.request(
                    {
                        url: '/api/ddr/devices/' + mRID + '/timeofuse',
                        method: 'GET',
                        success: function (response, opt) {
                            result = JSON.parse(response.responseText);
                            resultSet = reader.read(result);

                            if (view.down('device-tou-preview-form')) {
                                if (!(Object.keys(result).length === 0 && result.constructor === Object)) {
                                    view.down('device-tou-preview-form').fillFieldContainers(resultSet.records[0]);
                                    view.down('tou-device-action-menu').record = resultSet.records[0].getActiveCalendar();

                                }
                                view.down('#wrappingPanel').setLoading(false);
                            }


                            if(view.down('device-tou-planned-on-form')) {
                                if(resultSet.records[0].getNextPassiveCalendar() === null) {
                                    view.down('device-tou-planned-on-form').hide();
                                } else {
                                    view.down('device-tou-planned-on-form').show();
                                    view.down('device-tou-planned-on-form').down('form').loadRecord(resultSet.records[0].getNextPassiveCalendar());
                                }

                            }
                        }
                        ,
                        failure: function () {
                            if (view.down('#wrappingPanel')) {
                                view.down('#wrappingPanel').setLoading(false);
                            }
                        }
                    }
                );
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
                debugger;
                me.redirectToPreview(menu.record.get('id'));
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
                    url: '/api/ddr/devices/device/timeofuse',
                    calendarId: calendarId,
                    device: record
                });
                view.on('timeofusecalendarloaded', function (newRecord) {
                    me.getApplication().fireEvent('timeofusecalendarloaded', newRecord.get('name'));
                    return true;
                }, {single: true});
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    }
});




