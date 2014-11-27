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
        },
        {
            ref: 'sortingToolbar',
            selector: 'data-purge-history-overview #data-purge-history-sorting-toolbar'
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
        me.getSortingToolbar().getContainer().add({
            xtype: 'button',
            ui: 'tag',
            text: Uni.I18n.translate('datapurge.history.startedon', 'SAM', 'Started on'),
            iconCls: 'x-btn-sort-item-asc'
        });
    },

    showDetails: function (selectionModel, record) {
        var categoriesStore = this.getStore('Sam.store.DataPurgeHistoryCategories');

        this.getDetailsView().setTitle(Uni.I18n.formatDate('datapurge.history.startedon.dateFormat', record.get('startDate'), 'SAM', 'D d M Y \\a\\t h:i A'));
        categoriesStore.getProxy().setUrl(record.getId());
        categoriesStore.load();
    }
});