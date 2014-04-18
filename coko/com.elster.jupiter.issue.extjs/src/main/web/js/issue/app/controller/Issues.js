Ext.define('Isu.controller.Issues', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Isu.model.IssueSort'
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssuesGroups',
        'Isu.store.Assignee',
        'Isu.store.IssueStatus',
        'Isu.store.IssueReason'
    ],

    views: [
        'workspace.issues.Overview',
        'workspace.issues.Filter',
        'workspace.issues.List',
        'workspace.issues.Item',
        'workspace.issues.IssueNoGroup',
        'ext.button.GridAction',
        'ext.button.SortItemButton',
        'workspace.issues.component.TagButton'
    ],

    mixins: [
        'Isu.util.IsuGrid'
    ],

    refs: [
        {
            ref: 'itemPanel',
            selector: 'issues-item'
        },
        {
            ref: 'issuesList',
            selector: 'issues-list'
        },
        {
            ref: 'issueNoGroup',
            selector: 'issues-browse issue-no-group'
        },
        {
            ref: 'issuesOverview',
            selector: 'issues-overview'
        },
        {
            ref: 'filter',
            selector: 'issues-filter'
        }
    ],

    init: function () {
        this.control({
            'issues-overview issues-list gridview': {
                itemclick: this.loadGridItemDetail,
                refresh: this.onIssuesListGridViewRefreshEvent
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
            // ====================================  IssueListFilter controls  ====================================

            'button[name=addsortbtn]': {
                click: this.setAddSortMenu
            },
            'menu[name=addsortitemmenu]': {
                click: this.addSortItem
            },
            'combobox[name=groupnames]': {
                afterrender: this.setGroupFields,
                change: this.setGroup
            },
            'grid[name=groupgrid]': {
                itemclick: this.getIssuesForGroup
            },
            'button[name=clearsortbtn]': {
                click: this.clearSort
            },
            'issues-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'button[name=sortitembtn]': {
                click: this.changeSortDirection,
                arrowclick: this.removeSortItem
            },

            // ====================================  END IssueListFilter controls  ================================
            'issues-filter [name="filter"] button-tag': {
                arrowclick: this.removeFilter
            }
        });

        this.listen({
            store: {
                '#Isu.store.Issues': {
                    load: this.issueStoreLoad,
                    updateProxyFilter: this.filterUpdate,
                    updateProxySort: this.sortUpdate
                }
            }
        });

        this.groupStore = this.getStore('Isu.store.IssuesGroups');
        this.store = this.getStore('Isu.store.Issues');
        this.sortParams = {};
        this.actionMenuXtype = 'issue-action-menu';
        this.gridItemModel = this.getModel('Isu.model.Issues');
    },

    issueStoreLoad: function (store, records) {
        var issueNoGroup = this.getIssueNoGroup(),
            issueList = this.getIssuesList();

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

        if (filter.get('assignee')) {
            var button = Ext.create('Isu.view.workspace.issues.component.TagButton', {
                text: 'Assignee: ' + filter.get('assignee').get('name'),
                target: 'assignee'
            });

            buttons.push(button);
        }

        if (filter.get('reason')) {
            var button = Ext.create('Isu.view.workspace.issues.component.TagButton', {
                text: 'Reason: ' + filter.get('reason').get('name'),
                target: 'reason'
            });

            buttons.push(button);
        }

        if (filter.get('meter')) {
            var button = Ext.create('Isu.view.workspace.issues.component.TagButton', {
                text: 'Meter: ' + filter.get('meter').get('name'),
                target: 'meter'
            });

            buttons.push(button);
        }

        if (filter.status().count()) {
            filter.status().each(function (status) {
                var button = Ext.create('Isu.view.workspace.issues.component.TagButton', {
                    text: 'Status: ' + status.get('name'),
                    target: 'status',
                    targetId: status.getId()
                });

                buttons.push(button);
            });
        }

        filterElm.removeAll();

        if (buttons.length) {
            emptyText.hide();
            clearFilterBtn.setDisabled(false);

            Ext.Array.each(buttons, function (button) {
                filterElm.add(button);
            });
        } else {
            emptyText.show();
            clearFilterBtn.setDisabled(true);
        }
    },

    bulkChangeButtonDisable: function (grid) {
        var bulkBtn = grid.up().down('button[action=bulkchangesissues]');
        if (grid.store.getCount() < 1) {
            bulkBtn.setDisabled(true);
        } else {
            bulkBtn.setDisabled(false);
        }
    },

    onIssuesListGridViewRefreshEvent: function (gridView) {
        this.setAssigneeTypeIconTooltip(gridView);
        this.bulkChangeButtonDisable(gridView);
        this.selectFirstGridRow(gridView);
    },

    sortUpdate: function (sortModel) {
        if (!this.getFilter()) {
            return;
        }
        var filterElm = this.getFilter().down('[name="sortitemspanel"]'),
            isuList = this.getIssuesList();

        if (isuList) {
            isuList.getSelectionModel().deselectAll()
        }
        this.showDefaultItems();

        filterElm.removeAll();

        sortModel.fields.each(function (field) {
            if (sortModel.get(field.name)) {
                var cls = sortModel.get(field.name) == Isu.model.IssueSort.ASC
                    ? 'isu-icon-up-big'
                    : 'isu-icon-down-big';

                var sortItem = {
                    xtype: 'sort-item-btn',
                    sortValue: field.name,
                    text: field.displayValue,
                    iconCls: 'isu-icon-white ' + cls
                };

                filterElm.add(sortItem);
            }
        });

        this.getFilter().down('[name="clearsortbtn"]').setDisabled(!filterElm.items.length);

    },

    removeFilter: function (elm) {
        this.store.getProxyFilter().removeFilterParam(elm.target, elm.targetId);
        this.store.updateProxyFilter();
    },

    showOverview: function () {
        var widget,
            issuesStore = this.getStore('Isu.store.Issues');

        delete issuesStore.proxyFilter;
        delete issuesStore.proxySort;

        widget = Ext.widget('issues-overview');

        this.getApplication().fireEvent('changecontentevent', widget);
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
        var iString = '<h3>Issues for ' + this.group + ': ' + record.data.reason.name + '</h3>',
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
        var filterController = this.getController('Isu.controller.IssueFilter'),
            filterForm = filterController.getIssueFilter().down('filter-form'),
            allCheckboxes = filterForm.query('checkboxfield');
            checkbox = filterForm.down('[name='+ filterType +'] checkboxfield[inputValue=' + filterValue + ']');

        Ext.Array.each(allCheckboxes, function (item) {
            item.setValue(false);
        });

        checkbox.setValue(true);
        filterController.filter();
    },

    setComboFilter: function (filterType, filterValue, visualValue) {
        var filterController = this.getController('Isu.controller.IssueFilter'),
            filterForm = filterController.getIssueFilter().down('filter-form'),
            combo = filterForm.down('[name=' + filterType + ']'),
            comboStore = combo.getStore(),
            storeProxy = comboStore.getProxy();

        storeProxy.setExtraParam(combo.queryParam, visualValue);

        comboStore.load(function () {
            combo.setValue(filterValue);
            filterController.filter();
        });
    }
    // ====================================  END IssueListFilter controls  ====================================
});