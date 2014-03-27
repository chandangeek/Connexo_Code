Ext.define('Isu.controller.IssueAssignmentRules', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.AssignmentRules'
    ],
    views: [
        'administration.datacollection.issueassignmentrules.Overview',
        'ext.button.GridAction',
        'administration.datacollection.issueassignmentrules.ActionMenu'
    ],

    mixins: {
            isuGrid: 'Isu.util.IsuGrid'
    },

    init: function () {
        this.control({
            'issue-assignment-rules-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'issue-assignment-rules-overview issues-assignment-rules-list gridview': {
                refresh: this.onGridRefresh
            },
            'issue-assignment-rules-overview issues-assignment-rules-list actioncolumn': {
                click: this.showItemAction
            },
            'rule-action-menu': {
                beforehide: this.hideItemAction
            }
        });

        this.actionMenuXtype = 'rule-action-menu';
    },

    showOverview: function () {
        var widget = Ext.widget('issue-assignment-rules-overview');
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
                text: 'Issue assignment rules',
                href: 'issueassignmentrules'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    onGridRefresh: function (grid) {
        this.setAssigneeTypeIconTooltip(grid);
        this.setDescriptionTooltip(grid);
    }
});
