Ext.define('Isu.controller.IssueAutoCreationRules', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.AutoCreationRules'
    ],
    views: [
        'administration.datacollection.issueautomaticcreationrules.Overview',
        'ext.button.GridAction',
        'administration.datacollection.issueautomaticcreationrules.ActionMenu'
    ],

    mixins: {
        isuGrid: 'Isu.util.IsuGrid'
    },

    init: function () {
        this.control({
            'issue-autocreation-rules-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'issue-autocreation-rules-overview issues-autocreation-rules-list gridview': {
                refresh: this.onGridRefresh
            },
            'issue-autocreation-rules-overview issues-autocreation-rules-list actioncolumn': {
                click: this.showItemAction
            },
            'rule-action-menu': {
                beforehide: this.hideItemAction
            }
        });

        this.actionMenuXtype = 'auto-rule-action-menu';
    },

    showOverview: function () {
        var widget = Ext.widget('issue-autocreation-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    setBreadcrumb: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Administration',
                href: '#/administration'
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Data collection',
                href: 'datacollection'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issue automatic creation rules',
                href: 'issueautomaticcreationrules'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    onGridRefresh: function (grid) {
        this.setAssigneeTypeIconTooltip(grid);
        this.setDescriptionTooltip(grid);
    }
});
