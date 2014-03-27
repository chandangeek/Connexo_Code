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
                afterrender: this.loadFormModel
            },
            'issues-side-filter filter-form combobox[name=assignee]': {
                focus: this.onFocusCombo,
                blur: this.onBlurCombo,
                beforequery: this.onBeforeQueryCombo
            },
            'issues-side-filter filter-form combobox[name=reason]': {
                focus: this.onFocusCombo,
                blur: this.onBlurCombo
            }
        });
        this.listen({
            store: {
                '#Issues': {
                    updateProxyFilter: this.filterUpdate
                }
            }
        });
        this.getStore('Isu.store.Assignee').on('load', this.assigneeLoad);
    },

    assigneeLoad: function (store, records, success) {
        var combo = Ext.ComponentQuery.query('issues-side-filter filter-form combobox[name=assignee]')[0];
        if (combo.getValue && records.length > 0) {
            var types = {};
            Ext.Array.each(records, function (item) {
                types[item.get('type')] = true
            })
            if (!types.ROLE) {
                store.add({
                    name: 'No matches',
                    type: 'ROLE',
                    id: 'empty'
                })
            }
            if (!types.USER) {
                store.add({
                    name: 'No matches',
                    type: 'USER',
                    id: 'empty'
                })
            }
            if (!types.GROUP) {
                store.add({
                    name: 'No matches',
                    type: 'GROUP',
                    id: 'empty'
                })
            }
            console.log(store)
        }
    },

    onBeforeQueryCombo: function (queryPlan) {
        var store = queryPlan.combo.store;
        if (queryPlan.query) {
            store.group('type');
        } else {
            store.clearGrouping();
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
            grstore = this.getStore('Isu.store.IssuesGroups')
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
        var grstore = this.getStore('Isu.store.IssuesGroups'),
            reason = filter.get('reason'),
            status = filter.statusStore;

        if (reason) {
            grstore.proxy.extraParams.id = reason.get('id');
            grstore.loadPage(1);
        } else {
            delete grstore.proxy.extraParams.id;
            grstore.loadPage(1);
        }
        if (status) {
            var stat = [];
            status.each(function (item) {
                stat.push(item.get('id'))
            });
            grstore.proxy.extraParams.status = stat;
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