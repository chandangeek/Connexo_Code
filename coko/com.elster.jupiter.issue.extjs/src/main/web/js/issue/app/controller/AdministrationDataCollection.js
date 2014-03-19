Ext.define('Isu.controller.AdministrationDataCollection', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    views: [
        'administration.datacollection.Overview'
    ],

    init: function () {
        this.control({
            'administration-datacollection-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('administration-datacollection-overview');
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
            });
        breadcrumbParent.setChild(breadcrumbChild1);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    }
});