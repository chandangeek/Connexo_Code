Ext.define('Isu.controller.IssueCreationRules', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'CreationRules'
    ],
    views: [
        'administration.datacollection.issuecreationrules.Overview',
        'ext.button.GridAction',
        'administration.datacollection.issuecreationrules.ActionMenu'
    ],

    mixins: {
        isuGrid: 'Isu.util.IsuGrid'
    },

    refs: [
        {
            ref: 'itemPanel',
            selector: 'issue-creation-rules-overview issue-creation-rules-item'
        }
    ],

    init: function () {
        this.control({
            'issue-creation-rules-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'issue-creation-rules-overview issues-creation-rules-list gridview': {
                itemclick: this.loadGridItemDetail,
                refresh: this.onGridRefresh
            },
            'issue-creation-rules-overview issues-creation-rules-list actioncolumn': {
                click: this.showItemAction
            },
            'creation-rule-action-menu': {
                beforehide: this.hideItemAction
            }
        });

        this.actionMenuXtype = 'creation-rule-action-menu';
        this.gridItemModel = this.getModel('CreationRules');
    },

    showOverview: function () {
        var widget = Ext.widget('issue-creation-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    setBreadcrumb: function (breadcrumbs) {
        var me = this;
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Administration',
                href: me.getController('Isu.controller.history.Administration').tokenizeShowOverview()
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Data collection',
                href: 'datacollection'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issue creation rules',
                href: 'issuecreationrules'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    onGridRefresh: function (grid) {
        this.setAssigneeTypeIconTooltip(grid);
        this.setDescriptionTooltip(grid);
        this.selectFirstRule(grid);
    },

    selectFirstRule: function (grid) {
        var index = 0,
            item = grid.getNode(index),
            record = grid.getRecord(item);

        grid.fireEvent('itemclick', grid, record, item, index);
    }
});
