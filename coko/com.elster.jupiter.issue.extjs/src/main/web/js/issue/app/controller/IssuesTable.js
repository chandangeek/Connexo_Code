Ext.define('ViewDataCollectionIssues.controller.IssuesTable', {
    extend: 'Ext.app.Controller',

    init: function () {
        var issuesStore = this.getStore('ViewDataCollectionIssues.store.DataCollectionIssuesList');

        this.control({
            'view-data-collection-issues gridview': {
                itemclick: this.isuuesGridOnItemclick
            },
            /*'view-data-collection-issues paginationtoolbar': {
                change: this.onChangePage
            }*/
        });

        issuesStore.addListener('load', this.issuesStoreOnLoad);
    },

    isuuesGridOnItemclick : function (grid, record, item, index, e, eOpts) {
        var self = this,
            issuesItem = Ext.ComponentQuery.query('view-data-collection-issues-item')[0],
            emptyText = '';

        Ext.Ajax.request({
            url: '/api/isu/issue/' + record.data.id,
            success: function (response) {
                var issue = Ext.JSON.decode(response.responseText);

                record.set('location',        issue.location);
                record.set('customer',        issue.customer);
                record.set('usagePoint',      issue.usagePoint);
                record.set('creationDate',    issue.creationDate);
                record.set('serviceCategory', issue.serviceCategory);
                record.commit();

                issuesItem.fireEvent('change', issuesItem, record.data);
                Ext.resumeLayouts(true);
            }
        });
    },

    issuesStoreOnLoad : function (store, records, successful) {
        if (!store.getCount() && store.currentPage == 1) {
            Ext.ComponentQuery.query('view-data-collection-issues')[0].hide();
            Ext.ComponentQuery.query('view-data-collection-issues-blank')[0].show();
        } else {
            Ext.ComponentQuery.query('view-data-collection-issues')[0].show();
            Ext.ComponentQuery.query('view-data-collection-issues-blank')[0].hide();
        }
    },

    /*onChangePage: function (pgtb, pageData) {
        var issuesInfo = Ext.ComponentQuery.query('view-data-collection-issues toolbar container')[0],
            infoText = pageData.fromRecord + ' - ' + pageData.toRecord + ' of ' + pageData.total + ' issues';

        issuesInfo.removeAll();
        issuesInfo.add({
            xtype: 'container',
            html: infoText
        });
    }*/
});
