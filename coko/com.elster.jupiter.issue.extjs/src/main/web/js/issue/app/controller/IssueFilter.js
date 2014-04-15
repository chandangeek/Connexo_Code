Ext.define('Isu.controller.IssueFilter', {
    extend: 'Ext.app.Controller',

    requires: [
        'Isu.model.IssueFilter'
    ],
    stores: [
        'Isu.store.Assignee',
        'Isu.store.IssueStatus',
        'Isu.store.IssueReason',
        'Isu.store.UserGroupList',
        'Isu.store.IssueMeter',
        'Isu.store.Issues',
        'Isu.store.IssuesGroups'
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
                render: this.setComboTooltip
            },
            'issues-side-filter filter-form combobox[name=meter]': {
                render: this.setComboTooltip,
                expand: this.limitNotification
            }
        });

        this.listen({
            store: {
                '#Isu.store.Issues': {
                    updateProxyFilter: this.filterUpdate
                }
            }
        });
        this.groupStore = this.getStore('Isu.store.IssuesGroups');
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
        if (!this.getIssueFilter()) {
            return;
        }
        var form = this.getIssueFilter().down('filter-form'),
            chkbx = form.down('filter-checkboxgroup'),
            loadRecord = function () {
                form.loadRecord(filter);
                chkbx.store.un('load', loadRecord);
            };

        if (!chkbx.store.getCount()){
            chkbx.store.on('load', loadRecord);
        } else if (!chkbx.child()) {
            chkbx.on('afterRender', loadRecord);
        } else {
            loadRecord();
        }

        this.setParamsForIssueGroups(filter);
    },

    setParamsForIssueGroups: function (filter) {
        var groupStore = this.getStore('Isu.store.IssuesGroups'),
            groupStoreProxy = groupStore.getProxy(),
            status = filter.statusStore,
            statusValues = [],
            reason = filter.get('reason'),
            assignee = filter.get('assignee'),
            meter = filter.get('meter');

        if (status) {
            status.each(function (item) {
                statusValues.push(item.get('id'));
            });
            groupStoreProxy.setExtraParam('status', statusValues);
        }
        if (assignee) {
            groupStoreProxy.setExtraParam('assigneeId', assignee.get('id'));
            groupStoreProxy.setExtraParam('assigneeType', assignee.get('type'));
        } else {
            groupStoreProxy.setExtraParam('assigneeId', []);
            groupStoreProxy.setExtraParam('assigneeType', []);
        }
        if (reason) {
            groupStoreProxy.setExtraParam('id', reason.get('id'));
        } else {
            groupStoreProxy.setExtraParam('id', []);
        }
        if (meter) {
            groupStoreProxy.setExtraParam('meter', meter.get('id'));
        } else {
            groupStoreProxy.setExtraParam('meter', []);
        }
        groupStore.loadPage(1);
    },

    filter: function () {
        if (!this.getIssueFilter()) {
            return;
        }
        var form = this.getIssueFilter().down('filter-form'),
            filter = form.getRecord(),
            groupCombobox = Ext.ComponentQuery.query('issues-filter panel combobox[name=groupnames]')[0];

        form.updateRecord(filter);

        var reason = filter.data.reason;

        this.getStore('Isu.store.Issues').setProxyFilter(filter);

        if ( !Ext.isEmpty(groupCombobox.getValue()) && groupCombobox.getValue() != 0 && !Ext.isEmpty(reason) ) {
            var combobox = form.down('combobox[name=reason]'),
                grid = Ext.ComponentQuery.query('issues-filter panel gridpanel[name=groupgrid]')[0],
                gridview = Ext.ComponentQuery.query('issues-overview issues-list gridview')[0];
            reason.data.reason = reason.data.name;

            grid.fireEvent('itemclick', gridview, reason);
            grid.getSelectionModel().select([reason]);
        }
    }
});