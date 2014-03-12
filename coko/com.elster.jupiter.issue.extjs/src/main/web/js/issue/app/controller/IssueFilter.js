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
        this.getIssueFilter().down('form').loadRecord(new Isu.model.IssueFilter());
    },

    filter: function() {
        this.getIssueFilter().down('form').updateRecord();

        //todo: make a model from values!
        var values = this.getIssueFilter().down('form').getValues();
        this.getStore('Issues').setProxyFilter(values);
    }
});