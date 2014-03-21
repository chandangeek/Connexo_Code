Ext.define('Isu.controller.Issues', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Isu.model.IssueSort'
    ],

    stores: [
        'Issues',
        'Isu.store.IssuesGroups',
        'Isu.store.Assignee'
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
            selector: 'issue-no-group'
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
            'panel[name=sortitemspanel]': {
                afterrender: this.setDefaults
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
                '#Issues': {
                    updateProxyFilter: this.filterUpdate,
                    updateProxySort: this.sortUpdate
                }
            }
        });

        this.groupStore = this.getStore('Isu.store.IssuesGroups');
        this.store = this.getStore('Issues');
        this.groupParams = {};
        this.sortParams = {};
        this.actionMenuXtype = 'issue-action-menu';
        this.gridItemModel = this.getModel('Isu.model.Issues');
    },

    /**
     * After "updateProxyFilter" event from the Issue store, method will redraw button tags on the filter panel
     *
     * todo: I18n
     * @param filter Isu.component.filter.model.Filter
     */
    filterUpdate: function (filter) {
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
    },

    sortUpdate: function (sortModel) {
        var filterElm = this.getFilter().down('[name="sortitemspanel"]');

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
        var widget = Ext.widget('issues-overview');
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
                Isu.getApplication().getIssueDetailController().showOverview(menu.issueId, true);
                break;
        }
    },

    setDefaults: function (sortPanel) {
        var defaultSort = new Isu.model.IssueSort();
        defaultSort.addSortParam('dueDate');
        this.store.setProxySort(defaultSort);
    },

    updateIssueList: function () {
        var extraParams = {};
        if (this.groupParams != undefined) {
            Ext.merge(extraParams, this.groupParams);
        }
        this.store.proxy.extraParams = extraParams;

        this.store.load(extraParams);
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

    clearSort: function (btn) {
        this.store.setProxySort(new Isu.model.IssueSort());
    },

    setGroupFields: function (view) {
        var model = Ext.ModelManager.getModel('Isu.model.Issues'),
            reason = model.getFields()[1],
            data = [
                { Value: '(none)', display: 'None'}
            ],
            rec = { Value: reason.name,
                display: reason.displayValue };
        data.push(rec);
        view.store = Ext.create('Ext.data.Store', {
            fields: ['Value', 'display'],
            data: data
        });
    },

    setGroup: function (view, newValue) {
        var grid = Ext.ComponentQuery.query('grid[name=groupgrid]')[0],
            issuesFor = Ext.ComponentQuery.query('panel[name=issuesforlabel]')[0],
            lineLabel = Ext.ComponentQuery.query('label[name=forissuesline]')[0],
            groupItemsShown = grid.down('panel[name=groupitemsshown]'),
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

        this.groupStore.loadPage(1);
        this.showDefaultItems();

        if (newValue != '(none)') {
            this.groupStore.proxy.extraParams = {field: newValue};
            this.groupStore.load();
            this.group = newValue;
            grid.show();
            this.groupStore.on('load', XtoYof);
        } else {
            this.groupStore.removeAll();
            grid.hide();
            this.groupParams = {};
            this.updateIssueList();
            groupItemsShown.hide();
            this.groupStore.un('load', XtoYof);
            this.getIssueNoGroup().hide();
            fullList.show();
/*            this.store.getProxyFilter().set('reason', undefined);
            this.store.updateProxyFilter();*/
            this.store.setGroup(undefined);
            this.store.load();
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
//        this.groupParams[this.group] = record.data.id;

//        var model = new Isu.model.IssueReason({
//            id: record.getId(),
//            name: record.get('reason')
//        });
//        console.log(this.store.getProxyFilter());
//        this.store.getProxyFilter().set('reason', model);
//        this.store.updateProxyFilter();
        this.store.setGroup(record);
        this.store.load();

//        this.updateIssueList();
        this.showDefaultItems();

    },


    showDefaultItems: function () {
        var issueItemView = this.getItemPanel();
        if (issueItemView) {
            issueItemView.removeAll();
            issueItemView.add({
                html: '<h3>No issue selected</h3><p>Select an issue to view its detail.</p>',
                bodyPadding: 10,
                border: false
            });
        }
    }
    // ====================================  END IssueListFilter controls  ====================================
});