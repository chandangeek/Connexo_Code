Ext.define('Isu.controller.Issues', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Isu.model.ExtraParams'
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssuesGroups',
        'Isu.store.Assignee',
        'Isu.store.IssueStatus',
        'Isu.store.IssueReason',
        'Isu.store.IssueMeter',
        'Isu.store.UserGroupList',
        'Isu.store.IssueGrouping'
    ],

    views: [
        'workspace.issues.Overview',
        'workspace.issues.Filter',
        'workspace.issues.List',
        'workspace.issues.Item',
        'workspace.issues.IssueNoGroup',
        'ext.button.GridAction',
        'ext.button.SortItemButton',
        'Skyline.button.TagButton'
    ],

    mixins: [
        'Isu.util.IsuGrid',
        'Isu.util.IsuComboTooltip'
    ],

    refs: [
        {
            ref: 'issuesOverview',
            selector: 'issues-overview'
        },
        {
            ref: 'itemPanel',
            selector: 'issues-item'
        },
        {
            ref: 'issuesList',
            selector: 'issues-list'
        },
        {
            ref: 'noIssues',
            selector: 'issues-overview [name=noIssues]'
        },
        {
            ref: 'filteringToolbar',
            selector: 'issues-overview filtering-toolbar'
        },
        {
            ref: 'sortingToolbar',
            selector: 'issues-overview sorting-toolbar'
        },
        {
            ref: 'groupingToolbar',
            selector: 'issues-overview isu-grouping-toolbar'
        },
        {
            ref: 'issueFilter',
            selector: 'issues-side-filter'
        }
    ],

    init: function () {
        this.control({
            'issues-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'issues-overview issues-list gridview': {
                itemclick: this.loadGridItemDetail,
                refresh: this.onIssuesGridRefresh
            },
            'issues-overview issues-list actioncolumn': {
                click: this.showItemAction
            },
            'issue-action-menu': {
                beforehide: this.hideItemAction,
                click: this.chooseIssuesAction
            },
            'issues-overview issues-item': {
                afterChange: this.setFilterIconsActions
            },

            'issues-overview sorting-toolbar [name=actions-toolbar] button': {
                click: this.changeSortDirection,
                arrowclick: this.removeSortItem
            },
            'issues-overview sorting-toolbar issue-sort-menu': {
                click: this.addSortParam
            },
            'issues-overview sorting-toolbar button[action=clearSort]': {
                click: this.clearSortParams
            },

            'issues-overview filtering-toolbar [name="filter"] button-tag': {
                arrowclick: this.removeFilterItem
            },
            'button[name=clearsortbtn]': {
                click: this.clearSort
            },
            'issues-side-filter button[action="reset"]': {
                click: this.resetFilter
            },

            // ====================================  END IssueListFilter controls  ================================
            'issues-filter [name="filter"] button-tag': {
                arrowclick: this.removeFilter
            }
        });

        this.actionMenuXtype = 'issue-action-menu';
        this.gridItemModel = this.getModel('Isu.model.Issues');
    },

    showOverview: function () {
        var self = this,
            widget,
            issuesStore = this.getStore('Isu.store.Issues'),
            extraParamsModel = new Isu.model.ExtraParams(),
            extraParams = issuesStore.proxy.extraParams;

        if (records && records.length < 1) {

            issueNoGroup.removeAll();
            issueNoGroup.add({
                html: '<h3>No issues found</h3><p>The filter is too narrow</p>',
                bodyPadding: 10,
                border: false
            });
            issueList.hide();
            issueNoGroup && issueNoGroup.show();
        } else {
            issueList.show();
            issueNoGroup && issueNoGroup.hide();
        }
    },

    /**
     * After "updateProxyFilter" event from the Issue store, method will redraw button tags on the filter panel
     *
     * todo: I18n
     * @param filter Uni.component.filter.model.Filter
     */
    filterUpdate: function (filter) {
        if (!this.getFilter()) {
            return;
        }
        var filterElm = this.getFilter().down('[name="filter"]'),
            emptyText = this.getFilter().down('[name="empty-text"]'),
            clearFilterBtn = this.getFilter().down('button[action="clearfilter"]'),
            buttons = [];

            widget = Ext.widget('issues-overview');
            self.getApplication().fireEvent('changecontentevent', widget);

            self.extraParamsModel = extraParamsModel;

            self.getFilteringToolbar().addFilterButtons(extraParamsModel.get('filter'));
            self.setFilterForm();
            self.getSortingToolbar().addSortButtons(extraParamsModel.get('sort'));
            self.setGrouping();
        });
    },

    setBreadcrumb: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Workspace',
                href: '#/workspace'
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Data collection',
                href: 'datacollection'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issues',
                href: 'issues'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    setFilterForm: function () {
        var self = this,
            filterModel = this.extraParamsModel.get('filter'),
            form = self.getIssueFilter().down('filter-form'),
            filterCheckboxGroup = form.down('filter-checkboxgroup');

        if (filterModel.get('assignee')) {
            form.down('combobox[name=assignee]').getStore().add(filterModel.get('assignee'));
        }

        if (filterModel.get('reason')) {
            form.down('combobox[name=reason]').getStore().add(filterModel.get('reason'));
        }

        if (filterModel.get('meter')) {
            form.down('combobox[name=meter]').getStore().add(filterModel.get('meter'));
        }

        if (!filterCheckboxGroup.store.getCount()) {
            filterCheckboxGroup.store.load(function () {
                form.loadRecord(filterModel);
            });
        } else {
            form.loadRecord(filterModel);
        }
    },

    removeFilterItem: function (button) {
        this.extraParamsModel.get('filter').removeFilterParam(button.target, button.targetId);
        window.location.href = this.extraParamsModel.getQueryStringFromValues();
    },

    applyFilter: function () {
        var form = this.getIssueFilter().down('filter-form'),
            filter = form.getRecord();

        form.updateRecord(filter);
        this.extraParamsModel.set('filter', filter);
        window.location.href = this.extraParamsModel.getQueryStringFromValues();
    },

    resetFilter: function () {
        var filter = new Isu.model.IssueFilter();

        this.extraParamsModel.set('filter', filter);
        window.location.href = this.extraParamsModel.getQueryStringFromValues();
    },

    addSortParam: function (menu, item) {
        var sorting = this.extraParamsModel.get('sort');

        if (!sorting.data[item.action]) {
            sorting.addSortParam(item.action);
            window.location.href = this.extraParamsModel.getQueryStringFromValues();
        }
    },

    changeSortDirection: function (button) {
        var sorting = this.extraParamsModel.get('sort');

        sorting.toggleSortParam(button.sortName);
        window.location.href = this.extraParamsModel.getQueryStringFromValues();
    },

    removeSortItem: function (button) {
        var sorting = this.extraParamsModel.get('sort');

        sorting.removeSortParam(button.sortName);
        window.location.href = this.extraParamsModel.getQueryStringFromValues();
    },

    clearSortParams: function () {
        var sorting = this.extraParamsModel.get('sort');

        sorting.data = {};
        window.location.href = this.extraParamsModel.getQueryStringFromValues();
    },

    setGrouping: function () {
        var grouping = this.extraParamsModel.get('group').get('value'),
            groupingTollbar = this.getGroupingToolbar(),
            groupingCombo = groupingTollbar.down('[name=groupingcombo]'),
            groupingGrid = groupingTollbar.down('[name=groupinggrid]'),
            groupingInformation = groupingTollbar.down('[name=groupinginformation]'),
            selectionModel = groupingGrid.getSelectionModel(),
            groupingField;

        groupingCombo.setValue(grouping);
        groupingCombo.on('change', this.changeGrouping, this, {single: true});

        if (grouping == 'none') {
            groupingGrid.hide();
        } else {
            groupingField = this.extraParamsModel.get('filter').get(grouping);
            if (groupingField) {
                selectionModel.select(groupingField);
                groupingInformation.down('[name=informationtext]').update('<h3>Issues for reason: ' + groupingField.get('name') + '</h3>');
                groupingInformation.show();
            } else {
                this.getIssuesList().hide();
                this.getNoIssues().update('<h3>No group selected</h3><p>Select a group of issues.</p>');
                this.getNoIssues().show();
                this.getItemPanel().fireEvent('clear');
            }
            groupingGrid.on('itemclick', this.changeGroup, this, {single: true});
            groupingGrid.show();
        }
    },

    changeGroup: function (grid, record) {
        var grouping = this.extraParamsModel.get('group').get('value');

        this.extraParamsModel.get('filter').set(grouping, record);
        window.location.href = this.extraParamsModel.getQueryStringFromValues();
    },

    changeGrouping: function (combo, newValue) {
        var store = combo.getStore();

        this.extraParamsModel.set('group', store.getById(newValue));
        window.location.href = this.extraParamsModel.getQueryStringFromValues();
    },

    setParamsForIssueGroups: function (filterModel, field) {
        var groupStore = this.getStore('Isu.store.IssuesGroups'),
            groupStoreProxy = groupStore.getProxy(),
            status = filterModel.statusStore,
            statusValues = [],
            reason = filterModel.get('reason'),
            assignee = filterModel.get('assignee'),
            meter = filterModel.get('meter');

        if (field != 'none') {
            groupStoreProxy.setExtraParam('field', field);
        }

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
    },

    chooseIssuesAction: function (menu, item) {
        var action = item.action;

        switch (action) {
            case 'assign':
                window.location.href = '#/workspace/datacollection/issues/' + menu.issueId + '/assign';
                break;
            case 'close':
                window.location.href = '#/workspace/datacollection/issues/' + menu.issueId + '/close';
                break;
            case 'addcomment':
                window.location.href = '#/workspace/datacollection/issues/' + menu.issueId + '/addcomment';
                break;
        }
    },

    changeSortDirection: function (btn) {
        this.store.getProxySort().toggleSortParam(btn.sortValue);
        this.store.updateProxySort();
    },

    removeSortItem: function (btn) {
        this.store.getProxySort().removeSortParam(btn.sortValue);
        this.store.updateProxySort();
    },

    addSortItem: function (menu, item) {
        this.store.getProxySort().addSortParam(item.value);
        this.store.updateProxySort();
    },

    setAddSortMenu: function (btn) {
        if (btn.menu.items.getCount() < 1) {
            this.store.getProxySort().fields.each(function (item) {
                if (item.displayValue) {
                    btn.menu.add({
                        text: item.displayValue,
                        value: item.name
                    });
                }
            });
            btn.showMenu();
        }
    },

    clearSort: function () {
        this.store.setProxySort(new Isu.model.IssueSort());

    },

    setGroupFields: function (view) {
        var model = Ext.ModelManager.getModel('Isu.model.Issues'),
            reason = model.getFields()[1];

        view.store = Ext.create('Ext.data.Store', {
            fields: ['value', 'display'],
            data: [
                { value: 0, display: 'None'},
                { value: 'reason', display: reason.displayValue }
            ]
        });
    },

    setGroup: function (view, newValue) {
        var grid = Ext.ComponentQuery.query('grid[name=groupgrid]')[0],
            issuesFor = Ext.ComponentQuery.query('panel[name=issuesforlabel]')[0],
            lineLabel = Ext.ComponentQuery.query('label[name=forissuesline]')[0],
            groupItemsShown = grid.down('panel[name=groupitemsshown]'),
            issueNoGroup = this.getIssueNoGroup(),
            issuesList = this.getIssuesList(),
            fullList = this.getIssuesList(),

            XtoYof = function (store, records) {
                if (records.length > 0) {
                    var X = (store.currentPage - 1) * store.pageSize + 1,
                        Y = records.length + X - 1,
                        of = store.getTotalCount()
                        ;
                    groupItemsShown.removeAll();
                    groupItemsShown.add({
                        html: X + ' - ' + Y + ' of ' + of + ' ' + view.getRawValue() + 's',
                        border: false,
                        margin: '5'
                    });
                    groupItemsShown.show();
                } else {
                    groupItemsShown.hide();
                }
            };

        issuesFor.hide();
        lineLabel.hide();
        this.getIssueNoGroup().show();
        fullList.getSelectionModel().deselectAll();
        fullList.hide();

        this.showDefaultItems();

        // now grouping is active
        if (newValue) {
            this.groupStore.proxy.extraParams.field = newValue;
            this.groupStore.loadPage(1);
            this.group = newValue;
            issueNoGroup.removeAll();
            issueNoGroup.add({
                html: '<h3>No group selected</h3><p>Select a group of issues.</p>',
                bodyPadding: 10,
                border: false
            });
            issueNoGroup.show();
            issuesList.hide();
            grid.show();
            this.groupStore.on('load', XtoYof);
        } else {
            // remove the grouping

            this.groupStore.removeAll();
            grid.hide();
            groupItemsShown.hide();
            this.groupStore.un('load', XtoYof);
            this.getIssueNoGroup().hide();
            fullList.show();
            this.store.setGroup(undefined);
            this.store.loadPage(1);
        }
    },

    getIssuesForGroup: function (grid, record) {
        var iString = '<h3>Issues for ' + this.group + ': ' + record.data.reason + '</h3>',
            issuesFor = Ext.ComponentQuery.query('panel[name=issuesforlabel]')[0],
            lineLabel = Ext.ComponentQuery.query('label[name=forissuesline]')[0]
            ;
        issuesFor.removeAll();
        issuesFor.add({html: iString});
        issuesFor.show();
        lineLabel.show();
        this.getIssuesList().getSelectionModel().deselectAll();
        this.getIssuesList().show();
        this.getIssueNoGroup().hide();
        this.store.setGroup(record);
        this.store.loadPage(1);
        this.showDefaultItems();
    },


    showDefaultItems: function () {
        var issueItemView = this.getItemPanel();

        issueItemView && issueItemView.fireEvent('clear');
    },

    setFilterIconsActions: function (itemPanel) {
        var self = this,
            icons = Ext.get(itemPanel.getEl()).select('.isu-apply-filter');

        icons.on('click', self.addFilterIconAction, self);
        itemPanel.on('change', function () {
            icons.un('click', self.addFilterIconAction, self);
        });
        itemPanel.on('clear', function () {
            icons.un('click', self.addFilterIconAction, self);
        });
    },

    addFilterIconAction: function (event, icon) {
        var filterType = icon.getAttribute('data-filterType'),
            filterValue = icon.getAttribute('data-filterValue'),
            visualValue = Ext.get(icon).prev().getHTML();

        if (!filterType || !filterValue) {
            return;
        }

        switch (filterType) {
            case 'status':
                this.setChecboxFilter(filterType, filterValue);
                break;
            case 'assignee':
                this.setComboFilter(filterType, filterValue, visualValue);
                break;
            case 'reason':
                this.setComboFilter(filterType, parseInt(filterValue), visualValue);
                break;
            case 'meter':
                this.setComboFilter(filterType, parseInt(filterValue), filterValue);
                break;
        }
    },

    setChecboxFilter: function (filterType, filterValue) {
        var self = this,
            filterForm = this.getIssueFilter().down('filter-form'),
            allCheckboxes = filterForm.query('checkboxfield'),
            checkbox = filterForm.down('[name=' + filterType + '] checkboxfield[inputValue=' + filterValue + ']');

        Ext.Array.each(allCheckboxes, function (item) {
            item.setValue(false);
        });

        checkbox.setValue(true);
        self.applyFilter();
    },

    setComboFilter: function (filterType, filterValue, visualValue) {
        var self = this,
            filterForm = this.getIssueFilter().down('filter-form'),
            combo = filterForm.down('[name=' + filterType + ']'),
            comboStore = combo.getStore(),
            storeProxy = comboStore.getProxy();

        storeProxy.setExtraParam(combo.queryParam, visualValue);

        comboStore.load(function () {
            combo.setValue(filterValue);
            self.applyFilter();
        });
    },

    onIssuesGridRefresh: function (grid) {
        var store = grid.getStore(),
            grouping = this.extraParamsModel.get('group').get('value'),
            groupingField = this.extraParamsModel.get('filter').get(grouping),
            extraParams = store.getProxy().extraParams,
            emptyText;

        if (!store.getCount()) {
            if (extraParams.status || extraParams.assigneeId || extraParams.reason || extraParams.meter) {
                emptyText = '<h3>No issues found</h3><p>The filter is too narrow</p>';
            } else {
                emptyText = '<h3>No issue found</h3><p>No data collection issues have been created yet.</p>';
            }

            this.getIssuesList().hide();
            this.getNoIssues().update(emptyText);
            this.getNoIssues().show();
            this.getItemPanel().fireEvent('clear');
        }

        this.setAssigneeTypeIconTooltip(grid);

        if (!(grouping && grouping != 'none' && !groupingField)) {
            this.selectFirstGridRow(grid);
        }
    }
});