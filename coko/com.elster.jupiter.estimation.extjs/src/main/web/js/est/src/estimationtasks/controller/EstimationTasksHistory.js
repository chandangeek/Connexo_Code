Ext.define('Est.estimationtasks.controller.EstimationTasksHistory', {
    extend: 'Ext.app.Controller',

    requires: [],

    stores: [
        'Est.estimationtasks.store.EstimationTasksHistory'
    ],

    models: [
        'Est.estimationtasks.model.HistoryFilter'
    ],

    views: [
        'Est.estimationtasks.view.History'
    ],

    refs: [
        {ref: 'actionMenu', selector: 'estimationtasks-history-action-menu'},
        {ref: 'preview', selector: 'estimationtasks-history-preview'},
        {ref: 'previewForm', selector: 'estimationtasks-history-preview-form'},
        {ref: 'overviewLink', selector: '#estimationtasks-overview-link'},
        {ref: 'sideFilterForm', selector: '#side-filter #filter-form'},
        {ref: 'filterTopPanel', selector: '#estimationtasks-history-filter-top-panel'},
        {ref: 'filterTopSeparator', selector: '#estimationtasks-history-filter-top-separator'},
        {ref: 'history', selector: 'estimationtasks-history'}
    ],

    init: function () {
        this.control({
            'estimationtasks-history-grid': {
                select: this.showPreview
            },
            'estimationtasks-history-filter-form  button[action=applyfilter]': {
                click: this.applyHistoryFilter
            },
            'estimationtasks-history-filter-form  button[action=clearfilter]': {
                click: this.clearHistoryFilter
            },
            '#estimationtasks-history-filter-top-panel': {
                removeFilter: this.removeHistoryFilter,
                clearAllFilters: this.clearHistoryFilter
            }
        });
    },

    showEstimationTaskHistory: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Est.estimationtasks.model.EstimationTask'),
            store = me.getStore('Est.estimationtasks.store.EstimationTasksHistory'),
            widget,
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
        store.getProxy().setUrl(router.arguments);
        widget = Ext.widget('estimationtasks-history', {router: router, taskId: currentTaskId});

        me.initFilter();
        pageMainContent.setLoading(true);

        taskModel.load(currentTaskId, {
            success: function (record) {
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(record.get('name'));
                me.getApplication().fireEvent('estimationTaskLoaded', record);
            },
            callback: function(){
                pageMainContent.setLoading(false);
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getHistory(),
            preview = page.down('estimationtasks-history-preview'),
            previewForm = page.down('estimationtasks-history-preview-form');

        if (record) {
            Ext.suspendLayouts();
            preview.setTitle(record.get('startedOn_formatted'));
            previewForm.down('displayfield[name=startedOn_formatted]').setVisible(true);
            previewForm.down('displayfield[name=finishedOn_formatted]').setVisible(true);
            previewForm.loadRecord(record);
            preview.down('estimationtasks-history-action-menu').record = record;

            previewForm.loadRecord(record);
            Ext.resumeLayouts(true);
        }
    },

    initFilter: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            filter = router.filter,
            date;

        me.getSideFilterForm().loadRecord(filter);
        for (var f in filter.getData()) {
            var name = '', estimationPeriod;
            switch (f) {
                case 'startedOnFrom':
                    name = Uni.I18n.translate('estimationtasks.general.startedFrom', 'EST', 'Started from');
                    break;
                case 'startedOnTo':
                    name = Uni.I18n.translate('estimationtasks.general.startedTo', 'EST', 'Started to');
                    break;
                case 'finishedOnFrom':
                    name = Uni.I18n.translate('estimationtasks.general.finishedFrom', 'EST', 'Finished from');
                    name = 'Finished from';
                    break;
                case 'finishedOnTo':
                    name = Uni.I18n.translate('estimationtasks.general.finishedTo', 'EST', 'Finished to');
                    break;
            }
            if (!Ext.isEmpty(filter.get(f))) {
                date = new Date(filter.get(f));
                me.getFilterTopPanel().setFilter(f, name, estimationPeriod
                    ? Uni.DateTime.formatDateLong(date)
                    : Uni.DateTime.formatDateLong(date)
                + ' ' + Uni.I18n.translate('estimationtasks.general.at', 'EST', 'At').toLowerCase() + ' '
                + Uni.DateTime.formatTimeShort(date));
            }
        }
        me.getFilterTopPanel().setVisible(true);
    },

    applyHistoryFilter: function () {

        this.getSideFilterForm().updateRecord();
        this.getSideFilterForm().getRecord().save();
    },

    clearHistoryFilter: function () {

        this.getSideFilterForm().getForm().reset();
        this.getFilterTopPanel().setVisible(false);
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeHistoryFilter: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;
        if (record) {
            delete record.data[key];
            record.save();
        }
    }
});
