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
        'administration.datacollection.licensing.Details'
    ],

    mixins: {
        isuGrid: 'Isu.util.IsuGrid'
    },

    refs: [
        {
            ref: 'detailsPanel',
            selector: 'licensing-details'
        },
        {
            ref: 'listPanel',
            selector: 'licensing-list'
        }
    ],

    init: function () {
        this.control({
            'administration-licensing-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'administration-licensing-overview licensing-list grid': {
                viewready: this.showDefaults
            },
            'administration-licensing-overview licensing-list actioncolumn': {
                click: this.showItemAction
            },
            'licensing-action-menu': {
                beforehide: this.hideItemAction
            },
            'administration-licensing-overview licensing-list gridview': {
                itemclick: this.loadDetail
            }
        });

        this.actionMenuXtype = 'licensing-action-menu';
    },

    showOverview: function () {
        var widget = Ext.widget('administration-licensing-overview');
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
                text: 'Licensing',
                href: 'licensing'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    loadDetail: function (grid, record) {
        var model = this.getModel('Isu.model.Licensing'),
            detailsPanel = this.getDetailsPanel();
        model.load(record.data.id, {
            success: function (rec) {
                detailsPanel.fireEvent('change', detailsPanel, rec);
            }
        });
    },

    showDefaults: function () {
        var grid = this.getListPanel().down('grid'),
            detailsPanel = this.getDetailsPanel(),
            store = grid.getStore();
        grid.selModel.doSelect(store.data.items[0]);
        detailsPanel.fireEvent('change', detailsPanel, store.data.items[0]);
    }
});
