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
                afterrender: this.loadFormModel
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
        var listValues = combo.picker;

        if (newValue == null) {
            combo.reset();
            listValues && listValues.hide();
        } else {
            listValues && listValues.show();
        }
    },

    loadFormModel: function (form) {
        var store = this.getStore('IssueStatus'),
            me = this;

        store.filter('name', 'Open'); //todo: hardcoded value! remove after proper REST API is implemented.

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
            store = this.getStore('IssueStatus'),
            me = this;

        store.each(function (item) {
            defaultFilter.status().add(item);
        });

        form.loadRecord(defaultFilter);
        me.getStore('Issues').setProxyFilter(defaultFilter);
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

        this.getStore('Issues').setProxyFilter(filter);
    }
});