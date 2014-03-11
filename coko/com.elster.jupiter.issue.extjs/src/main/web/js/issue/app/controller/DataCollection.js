Ext.define('Isu.controller.DataCollection', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    views: [
        'workspace.datacollection.Overview'
    ],

    init: function () {
        this.control({
            'datacollection-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('datacollection-overview');
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
            });
        breadcrumbParent.setChild(breadcrumbChild1);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    }
});