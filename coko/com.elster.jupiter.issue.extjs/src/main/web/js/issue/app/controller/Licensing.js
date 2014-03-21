Ext.define('Isu.controller.Licensing', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.Licensing'
    ],

    views: [
        'administration.datacollection.licensing.Overview',
        'ext.button.GridAction',
        'administration.datacollection.licensing.ActionMenu',
        'administration.datacollection.licensing.SideFilter'
    ],

    mixins: {
        isuGrid: 'Isu.util.IsuGrid'
    },

    init: function () {
        this.control({
            'administration-licensing-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'administration-licensing-overview licensing-list actioncolumn': {
                click: this.showItemAction
            },
            'licensing-action-menu': {
                beforehide: this.hideItemAction
            }
        });

        this.actionMenuXtype = 'licensing-action-menu';
    },

    showOverview: function () {
        var widget = Ext.widget('administration-licensing-overview');
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
                text: 'Licensing',
                href: 'licensing'
            });

        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    }
});
