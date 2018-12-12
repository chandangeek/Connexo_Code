/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

       // store.load();

        view = Ext.widget('tou-setup');
        me.getApplication().fireEvent('changecontentevent', view);


    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            previewForm = preview.down('tou-preview-form'),
            model = Ext.ModelManager.getModel('Uni.model.timeofuse.Calendar');

        model.getProxy().setUrl('/api/cal/calendars');
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
                break;
            case 'remove':
                me.removeCalendar(menu.record);
                break;
            case 'activateDeactivate':
                me.activateDeactivateCalendar(menu.record);
                break;
        }
    },

    loadPreview: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('administration/calendars/preview');
        route.forward({id: id});
    },

    viewPreviewOfCalendar: function (id) {
        var me = this,
            view;


        view = Ext.widget('timeOfUseCalendar', {
            url: '/api/cal/calendars',
            calendarId: id
        });
        view.on('timeofusecalendarloaded', function (newRecord) {
            me.getApplication().fireEvent('timeofusecalendarloaded', newRecord.get('name'));
            return true;
        }, {single: true});
        me.getApplication().fireEvent('changecontentevent', view);
    },

    removeCalendar: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            store = me.getStore('Cal.store.TimeOfUseCalendars');

        record.getProxy().setUrl('/api/cal/calendars');
        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('calendar.remove.msg', 'CAL', 'This calendar will no longer be available.'),
                title: Uni.I18n.translate('general.removeX', 'CAL', "Remove '{0}'?", [record.data.name]),
                fn: function (state) {
                    if (state === 'confirm') {
                        record.destroy({
                            success: function () {
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('calendar.remove.success.msg', 'CAL', 'Calendar removed'));
                                var grid = me.getTimeOfUseGrid();
                                grid.down('pagingtoolbartop').totalCount = 0;
                                
                                grid.getStore().load();
                            }
                        });
                    }
                }
            });
    },

    activateDeactivateCalendar: function(calendar){
        var me=this,
            store = me.getStore('Cal.store.TimeOfUseCalendars');
        calendar.set('status',{id:calendar.get('status').id==='INACTIVE'?'ACTIVE':'INACTIVE'});
        calendar.save({
            success: function(){
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('calendar.save.success.msg', 'CAL', 'Calendar saved'));
                store.load();
            },
            failure: function(){
                store.load();
            }
        });
    }
});