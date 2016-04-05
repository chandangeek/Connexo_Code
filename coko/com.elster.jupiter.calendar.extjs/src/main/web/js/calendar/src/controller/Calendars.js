Ext.define('Cal.controller.Calendars', {
    extend: 'Ext.app.Controller',

    views: [
        'Cal.view.Setup',
        'Uni.view.window.Confirmation'
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
            }
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

    updateCalendarsCounter: function () {
        var me = this;
        me.getTimeOfUseGrid().down('pagingtoolbartop #displayItem').setText(
            Uni.I18n.translatePlural('general.timeOfUseCalendarCount', me.getTimeOfUseGrid().getStore().getCount(), 'CAL', 'No time of use caldendars', '{0} time of use calendar', '{0} time of use calendars')
        );
    }
});