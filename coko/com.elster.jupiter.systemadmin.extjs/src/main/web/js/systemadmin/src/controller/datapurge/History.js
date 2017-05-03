/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.controller.datapurge.History', {
    extend: 'Ext.app.Controller',

    models: [
        'Uni.component.sort.model.Sort'
    ],

    stores: [
        'Sam.store.DataPurgeHistory',
        'Sam.store.DataPurgeHistoryCategories'
    ],

    views: [
        'Sam.view.datapurge.HistoryOverview'
    ],

    refs: [
        {
            ref: 'detailsView',
            selector: 'data-purge-history-overview #data-purge-history-details'
        }
    ],

    init: function () {
        this.control({
            'data-purge-history-overview #data-purge-history-grid': {
                select: this.showDetails
            }
        });
    },

    showOverview: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        me.getApplication().fireEvent('changecontentevent', Ext.widget('data-purge-history-overview', {
            router: router
        }));
    },

    showDetails: function (selectionModel, record) {
        var categoriesStore = this.getStore('Sam.store.DataPurgeHistoryCategories'),
            date = record.get('startDate');

        this.getDetailsView().setTitle(
            Uni.DateTime.formatDateTimeLong(date)
        );
        categoriesStore.getProxy().setUrl(record.getId());
        categoriesStore.load();
    }
});