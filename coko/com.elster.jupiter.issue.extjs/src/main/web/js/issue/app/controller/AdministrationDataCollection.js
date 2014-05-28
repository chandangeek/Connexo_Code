Ext.define('Isu.controller.AdministrationDataCollection', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    views: [
    ],

    init: function () {
        this.control({
            'administration-datacollection-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            }
        });
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
            });
        breadcrumbParent.setChild(breadcrumbChild1);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    }
});