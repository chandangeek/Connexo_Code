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
            },
            'issues-side-filter filter-form combobox[name=assignee]': {
                change: this.clearCombo
            },
            'issues-side-filter filter-form combobox[name=reason]': {
                change: this.clearCombo
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

    clearCombo: function (combo, newValue) {
        if (newValue == '') {
            combo.reset();
        }
    },

    loadFormModel: function (form) {
        var defaultFilter = new Isu.model.IssueFilter(),
            store = this.getStore('IssueStatus'),
            me = this;

        store.filter('name', 'Open'); //todo: hardcoded value! remove after proper REST API is implemented.
        store.on('load', function(){
            store.each(function(item) {
                defaultFilter.status().add(item);
            });

            form.loadRecord(defaultFilter);
            me.getStore('Issues').setProxyFilter(defaultFilter);
        });
    },

    reset: function () {
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

    filter: function () {
        var form = this.getIssueFilter().down('filter-form'),
            filter = form.getRecord();

        form.updateRecord(filter);

        this.getStore('Issues').setProxyFilter(filter);
    }
});