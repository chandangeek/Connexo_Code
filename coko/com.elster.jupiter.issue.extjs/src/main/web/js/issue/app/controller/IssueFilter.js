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
        'Isu.util.IsuComboTooltip'
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
            'issues-side-filter filter-form combobox[name=reason]': {
                focus: this.onFocusComboTooltip,
                blur: this.onBlurComboTooltip,
                change: this.clearComboTooltip
            }
        });

        this.listen({
            store: {
                '#Isu.store.Issues': {
                    updateProxyFilter: this.filterUpdate
                }
            }
        });
     //   this.getStore('Isu.store.Assignee').on('load', this.assigneeLoad);
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
        }
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
        var form = this.getIssueFilter().down('filter-form');
        form.loadRecord(filter);

        var grstore = this.getStore('Isu.store.IssuesGroups'),
            reason = filter.get('reason'),
            status = filter.statusStore;

        if (reason) {
            grstore.proxy.extraParams.id = reason.get('id');

        } else {
            delete grstore.proxy.extraParams.id;

        }
        if (status) {
            var stat = [];
            status.each(function (item) {
                stat.push(item.get('id'))
            });
            grstore.proxy.extraParams.status = stat;

        }
        grstore.loadPage(1);
    },

    filter: function () {
        var form = this.getIssueFilter().down('filter-form'),
            filter = form.getRecord();

        form.updateRecord(filter);

        this.getStore('Isu.store.Issues').setProxyFilter(filter);
    }
});