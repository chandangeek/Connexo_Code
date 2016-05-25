Ext.define('Cal.controller.Calendars', {
    extend: 'Ext.app.Controller',

    views: [
        'Cal.view.Setup',
        'Uni.view.window.Confirmation',
        'Uni.view.calendar.TimeOfUseCalendar'
    ],
    stores: [
        'Cal.store.TimeOfUseCalendars'
    ],
    models: [
        'Uni.model.timeofuse.Calendar'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'tou-preview'
        },
        {
            ref: 'timeOfUseGrid',
            selector: 'tou-grid'
        }
    ],

    init: function () {
        this.control({
            'tou-setup tou-grid': {
                select: this.showPreview
            },
            'tou-action-menu': {
                click: this.chooseAction
            }
        });
    },

    showTimeOfUseOverview: function () {
        var me = this,
            view,
            store = me.getStore('Cal.store.TimeOfUseCalendars');

        store.load({
            callback: function (records, operation, success) {
                if(success === true) {
                    me.getTimeOfUseGrid().down('pagingtoolbartop #displayItem').setText(
                        Uni.I18n.translatePlural('general.calendarCount', store.getCount(), 'CAL', 'No time of use calendars', '{0} time of use calendar', '{0} time of use calendars')
                    );
                }
            }
        });

        view = Ext.widget('tou-setup');
        me.getApplication().fireEvent('changecontentevent', view);


    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            previewForm = preview.down('tou-preview-form'),
            model = Ext.ModelManager.getModel('Uni.model.timeofuse.Calendar');

        model.getProxy().setUrl('/api/cal/calendars/timeofusecalendars');
        previewForm.setLoading(true);
        Ext.ModelManager.getModel('Uni.model.timeofuse.Calendar').load(record.get('id'), {
           success: function (calendar) {
               previewForm.fillFieldContainers(calendar);
               previewForm.setLoading(false);
           },
            failure: function() {
                previewForm.setLoading(false);
            }
        });

        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        preview.down('tou-action-menu').record = record;
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'viewpreview':
                me.loadPreview(menu.record.get('id'));
        }
    },

    loadPreview: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('administration/timeofusecalendars/preview');
        route.forward({id: id});
    },

    viewPreviewOfCalendar: function (id) {
        var me = this,
            view;


        view = Ext.widget('timeOfUseCalendar', {
            url: '/api/cal/calendars/timeofusecalendars',
            calendarId: id
        });
        view.on('timeofusecalendarloaded', function (newRecord) {
            me.getApplication().fireEvent('timeofusecalendarloaded', newRecord.get('name'));
            return true;
        }, {single: true});
        me.getApplication().fireEvent('changecontentevent', view);
    }

    //updateCalendarsCounter: function () {
    //    var me = this;
    //    me.getTimeOfUseGrid().down('pagingtoolbartop #displayItem').setText(
    //        Uni.I18n.translatePlural('general.timeOfUseCalendarCount', me.getTimeOfUseGrid().getStore().getCount(), 'CAL', 'No time of use caldendars', '{0} time of use calendar', '{0} time of use calendars')
    //    );
    //}
});