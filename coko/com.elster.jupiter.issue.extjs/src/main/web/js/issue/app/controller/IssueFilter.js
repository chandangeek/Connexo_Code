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

    mixins: [
        'Isu.util.IsuCombo'
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
                change: this.clearCombo,
                focus: this.onFocusCombo,
                blur: this.onBlurCombo
            },
            'issues-side-filter filter-form combobox[name=reason]': {
                change: this.clearCombo,
                focus: this.onFocusCombo,
                blur: this.onBlurCombo
            }
        });

        this.listen({
            store: {
                '#Isu.store.Issues': {
                    updateProxyFilter: this.filterUpdate
                }
            }
        });
    },

    loadFormModel: function () {
        var store = this.getStore('Isu.store.IssueStatus'),
            me = this;

        if (!store.count()) {
            store.on('load', function () {
                me.loadDefaults();
            });
        } else {
            me.loadDefaults();
        }
    },

    loadDefaults: function () {
        var form = this.getIssueFilter().down('filter-form'),
            defaultFilter = new Isu.model.IssueFilter(),
            store = this.getStore('Isu.store.IssueStatus'),
            me = this;

        var item = store.findRecord('name', 'Open'); //todo: hardcoded value! remove after proper REST API is implemented.
        defaultFilter.status().add(item);

        form.loadRecord(defaultFilter);
        me.getStore('Isu.store.Issues').setProxyFilter(defaultFilter);
    },

    reset: function () {
        var filter = new Isu.model.IssueFilter();

        this.getIssueFilter().down('filter-form').loadRecord(filter);
        this.getStore('Isu.store.Issues').setProxyFilter(filter);
    },

    /**
     * @param filter
     */
    filterUpdate: function (filter) {
        var grstore = this.getStore('Isu.store.IssuesGroups');
        reason = filter.get('reason');
        if (reason) {
            grstore.proxy.extraParams.id = reason.get('id');
            grstore.loadPage(1);
        } else {
            delete grstore.proxy.extraParams.id;
            grstore.loadPage(1);
        }

        this.getIssueFilter().down('filter-form').loadRecord(filter);
    },

    filter: function () {
        var form = this.getIssueFilter().down('filter-form'),
            filter = form.getRecord();

        form.updateRecord(filter);

        this.getStore('Isu.store.Issues').setProxyFilter(filter);
    }
});