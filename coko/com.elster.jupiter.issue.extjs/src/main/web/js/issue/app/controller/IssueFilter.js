Ext.define('Isu.controller.IssueFilter', {
    extend: 'Ext.app.Controller',
    requires: [
        'Isu.model.IssueFilter'
    ],
    stores: [
        'Isu.store.Assignee',
        'Isu.store.IssueStatus',
        'Isu.store.IssueReason',
        'Issues'
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
            },
            'issues-side-filter button[action="reset"]': {
                click: this.reset
            },
            'issues-filter button[action="clearfilter"]': {
                click: this.reset
            },
            'issues-side-filter filter-form': {
                render: this.loadFormModel
            }
        });
        this.listen({
            store: {
                '#Issues': {
                    updateProxyFilter: this.filterUpdate
                }
            }
        });
    },

    loadFormModel: function (form) {
        form.loadRecord(new Isu.model.IssueFilter());
    },

    reset: function() {
        var filter = new Isu.model.IssueFilter();
        this.getIssueFilter().down('filter-form').loadRecord(filter);
        this.getStore('Issues').setProxyFilter(filter);
    },

    /**
     * @param filter
     */
    filterUpdate: function (filter) {
        this.getIssueFilter().down('filter-form').loadRecord(filter);
    },

    filter: function() {
        var form = this.getIssueFilter().down('filter-form');
        var filter = form.getRecord();
        form.updateRecord(filter);

        this.getStore('Issues').setProxyFilter(filter);
    }
});