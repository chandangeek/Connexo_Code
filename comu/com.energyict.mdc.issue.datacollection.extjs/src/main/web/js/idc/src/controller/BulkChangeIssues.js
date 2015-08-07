Ext.define('Idc.controller.BulkChangeIssues', {
    extend: 'Isu.controller.BulkChangeIssues',

    stores: [
        'Idc.store.IssuesBuffered',
        'Idc.store.BulkChangeIssues'
    ],

    showOverview: function () {
        var me = this,
            issuesStore = this.getStore('Idc.store.IssuesBuffered'),
            issuesStoreProxy = issuesStore.getProxy(),
            widget, grid;

        issuesStoreProxy.extraParams = {};
        issuesStoreProxy.setExtraParam('sort', ['dueDate', 'modTime']);

        widget = Ext.widget('bulk-browse');
        grid = widget.down('bulk-step1').down('issues-selection-grid');
        grid.reconfigure(issuesStore);

        me.getApplication().fireEvent('changecontentevent', widget);
        issuesStore.data.clear();
        issuesStore.clearFilter(true);
        issuesStore.filter([{property: 'status', value: ['status.open']}]);
        issuesStore.on('load', function () {
            grid.onSelectDefaultGroupType();
        }, me, {single: true});
    },

    onWizardCancelledEvent: function (wizard) {
        this.getController('Uni.controller.history.Router').getRoute('workspace/datacollectionissues').forward();
    },

    getBulkRecord: function () {
        var bulkStore = Ext.getStore('Idc.store.BulkChangeIssues'),
            bulkRecord = bulkStore.getAt(0);

        if (!bulkRecord) {
            bulkStore.add({
                operation: 'assign'
            });
        }

        return bulkStore.getAt(0);
    }
});