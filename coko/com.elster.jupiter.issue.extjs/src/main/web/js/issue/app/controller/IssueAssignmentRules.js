Ext.define('Mtr.controller.IssueAssignmentRules', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mtr.store.AssignmentRules'
    ],
    views: [
        'workspace.datacollection.issueassignmentrules.Overview'
    ],

    refs: [],

    init: function () {
        this.control({
            'issue-assignment-rules-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
        });

    },

    showOverview: function () {
        var widget = Ext.widget('issue-assignment-rules-overview');
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
                text: 'Issue assignment rules',
                href: 'assignmentrules'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    }
});
