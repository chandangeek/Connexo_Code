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
        issuesStoreProxy.setExtraParam('status', 'status.open');
        issuesStoreProxy.setExtraParam('sort', 'dueDate');

        widget = Ext.widget('bulk-browse');
        grid = widget.down('bulk-step1').down('issues-selection-grid');
        grid.reconfigure(issuesStore);

        me.getApplication().fireEvent('changecontentevent', widget);
        issuesStore.data.clear();
        issuesStore.loadPage(1, {
            callback: function() {
                grid.onSelectDefaultGroupType();
            }
        });
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