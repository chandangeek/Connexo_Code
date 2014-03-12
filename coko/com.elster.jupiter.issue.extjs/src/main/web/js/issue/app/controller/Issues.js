Ext.define('Isu.controller.Issues', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssuesGroups'
    ],

    views: [
        'workspace.issues.Overview',
        'workspace.issues.Filter',
        'workspace.issues.List',
        'workspace.issues.Item',
        'workspace.issues.IssueNoGroup',
        'ext.button.GridAction',
        'ext.button.SortItemButton'
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
        }
    ],

    init: function () {
        this.control({
            'issues-overview issues-list gridview': {
                itemclick: this.loadGridItemDetail,
                itemmouseenter: this.onUserTypeIconHover
            },
            'issues-overview issues-list actioncolumn': {
                click: this.showItemAction
            },
            'issue-action-menu': {
                beforehide: this.hideItemAction,
                click: this.chooseIssuesAction
            },
            'issues-overview issues-list button[name=bulk-change-issues]': {
                click: this.bulkChangeIssues
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
            }

            // ====================================  END IssueListFilter controls  ================================
        });

        this.groupStore = this.getStore('Isu.store.IssuesGroups');
        this.store = this.getStore('Isu.store.Issues');
        this.groupParams = {};
        this.sortParams = {};
        this.actionMenuXtype = 'issue-action-menu';
        this.gridItemModel = this.getModel('Isu.model.Issues');
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
        var widget;
        if (item.text == 'Assign') {
            widget = Ext.widget('issues-assign', {
                record: menu.record
            });
        } else {
            widget = Ext.widget('issues-close', {
                record: menu.record
            });
        }
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    bulkChangeIssues: function () {
        var widget = Ext.widget('bulk-browse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    // ====================================  IssueListFilter controls  ====================================

    setDefaults: function (sortPanel) {
        this.groupParams = {};
        var fakeItem = {
                text: 'Due date',
                value: 'dueDate'
            },
            fakeMenu = {
                up: function () {
                    return {
                        up: function () {
                            return sortPanel;
                        }
                    }
                }
            };
        this.addSortItem(fakeMenu, fakeItem);

    },

    updateIssueList: function () {
        var extraParams = {};
        if (this.sortParams != undefined || this.groupParams != undefined) {
            Ext.merge(extraParams, this.groupParams);
            Ext.merge(extraParams, this.sortParams);
        }
        this.store.proxy.extraParams = extraParams;
        this.store.loadPage(1);
    },

    applySort: function (view) {
        var me = this;
        me.sortParams = {};
        view.items.each(function (item) {
            if (item.name == 'sortitembtn') {
                me.sortParams.sort = item.sortValue;
                me.sortParams.order = item.sortOrder;
            }
        });
        me.updateIssueList();
        this.showDefaultItems();
    },

    changeSortDirection: function (btn) {
        if (btn.sortOrder == 'asc') {
            btn.setIconCls('isu-icon-down-big isu-icon-white');
            btn.sortOrder = 'desc';
        } else {
            btn.setIconCls('isu-icon-up-big isu-icon-white');
            btn.sortOrder = 'asc'
        }
        this.applySort(btn.up('panel'))
    },

    removeSortItem: function (btn) {
        var panel = btn.up('panel');
        panel.remove(btn);
        this.applySort(panel)
    },

    addSortItem: function (menu, item) {
        var btn = menu.up('button'),
            panel = btn.up('panel'),
            index = panel.items.getCount() - 1;

        var sortItem = {
            xtype: 'sort-item-btn',
            sortValue: item.value,
            text: item.text
        };

        panel.insert(index, sortItem);

        this.applySort(panel);
    },

    setAddSortMenu: function (btn) {
        if (btn.menu.items.getCount() < 1) {
            var menu = [],
                item = {};
            item.text = 'Due date';
            item.value = 'dueDate';
            item.name = 'addsortmenuitem';
            menu.push(item);
            btn.menu.add(menu);
            btn.showMenu();
        }
    },

    clearSort: function (btn) {
        var pan = btn.up('panel').up('panel').down('panel[name=sortitemspanel]');
        pan.items.each(function (item) {
            if (item.name == 'sortitembtn') {
                pan.remove(item, true);
            }
        });
        this.applySort(pan);
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
            this.groupStore.proxy.extraParams = {reason: newValue};
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
        }
    },

    getIssuesForGroup: function (grid, record) {

        var iString = 'Issues for ' + this.group + ': ' + record.data.reason,
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
        this.groupParams[this.group] = record.data.reason;
        this.updateIssueList();
        this.showDefaultItems();
        this.store.loadPage(1);
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