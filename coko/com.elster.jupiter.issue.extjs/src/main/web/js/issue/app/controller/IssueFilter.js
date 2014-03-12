Ext.define('Isu.controller.IssueFilter', {
    extend: 'Ext.app.Controller',
    requires: [
        'Isu.model.IssueFilter'
    ],
    stores: [
        'Isu.store.Assignee',
        'Isu.store.IssueStatus',
        'Isu.store.IssueReason'
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
        var record = this.getIssueFilter().down('form').getRecord();
        console.log(record);
        console.log(this.getIssueFilter().down('form').getValues());
    }
});