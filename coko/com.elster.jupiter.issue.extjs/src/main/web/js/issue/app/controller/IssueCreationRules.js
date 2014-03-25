Ext.define('Isu.controller.IssueCreationRules', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.CreationRules'
    ],
    views: [
        'administration.datacollection.issuecreationrules.Overview',
        'ext.button.GridAction',
        'administration.datacollection.issuecreationrules.ActionMenu'
    ],

    mixins: {
        isuGrid: 'Isu.util.IsuGrid'
    },

    init: function () {
        this.control({
            'issue-creation-rules-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'issue-creation-rules-overview issues-creation-rules-list gridview': {
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
    },

    showOverview: function () {
        var widget = Ext.widget('issue-creation-rules-overview');
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
                text: 'Issue creation rules',
                href: 'issuecreationrules'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    onGridRefresh: function (grid) {
        this.setAssigneeTypeIconTooltip(grid);
        this.setDescriptionTooltip(grid);
    }
});
