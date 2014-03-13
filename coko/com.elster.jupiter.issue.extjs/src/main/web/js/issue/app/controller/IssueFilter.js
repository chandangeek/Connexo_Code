Ext.define('Isu.controller.IssueFilter', {
    extend: 'Ext.app.Controller',
    requires: [
        'Isu.model.IssueFilter'
    ],
    stores: [
        'Isu.store.Assignee',
        'Isu.store.IssueStatus',
        'Isu.store.IssueReason',
        'Isu.store.Issues'
    ],
    views: [
        'workspace.issues.SideFilter'
    ],

    refs: [
        {
            ref: 'issueFilter',
            selector: 'issues-side-filter'
        }
    ],

    init: function () {
        this.control({
            'issues-side-filter button[action="filter"]': {
                click: this.filter
            }
        });
    },

    onLaunch: function () {
        this.getIssueFilter().down('filter-form').loadRecord(new Isu.model.IssueFilter());
    },

    filter: function() {
        var form = this.getIssueFilter().down('filter-form');
        var filter = form.getRecord();
        form.updateRecord(filter);

        this.getStore('Issues').setProxyFilter(filter);
    }
});