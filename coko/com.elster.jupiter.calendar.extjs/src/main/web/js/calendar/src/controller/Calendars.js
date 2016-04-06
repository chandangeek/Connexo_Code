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
            },
        });
    },

    showTimeOfUseOverview: function () {
        var me = this,
            view,
            store = me.getStore('Cal.store.TimeOfUseCalendars');

        store.load();

        view = Ext.widget('tou-setup');
        me.getApplication().fireEvent('changecontentevent', view);


    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            previewForm = preview.down('tou-preview-form');

        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        previewForm.fillFieldContainers(record);
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
            model = me.getModel('Uni.model.timeofuse.Calendar'),
            view;

        model.setProxy({
            type: 'rest',
            url: '/api/cal/calendars/timeofusecalendars',
            timeout: 120000,
            reader: {
                type: 'json'
            }
        });
        model.load(id, {
            success: function (calendar) {
                view = Ext.widget('timeOfUseCalendar', {
                    record: calendar,
                    model: model
                });
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });

    }

    //updateCalendarsCounter: function () {
    //    var me = this;
    //    me.getTimeOfUseGrid().down('pagingtoolbartop #displayItem').setText(
    //        Uni.I18n.translatePlural('general.timeOfUseCalendarCount', me.getTimeOfUseGrid().getStore().getCount(), 'CAL', 'No time of use caldendars', '{0} time of use calendar', '{0} time of use calendars')
    //    );
    //}
});