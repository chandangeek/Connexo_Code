Ext.define('Mtr.controller.BulkChangeIssues', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mtr.store.Issues',
        'Mtr.store.IssuesGroups'
    ],

    views: [
        'workspace.issues.Filter',
        'workspace.issues.List',
        'workspace.issues.IssueNoGroup',
        'workspace.issues.bulk.Browse',
        'ext.button.IssuesGridAction',
        'ext.button.SortItemButton'
    ],

    refs: [

    ],

    init: function () {
        this.control({
            'bulk-browse breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            }
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
            }),
            breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Bulk action',
                href: 'bulkaction'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2).setChild(breadcrumbChild3);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    }
});