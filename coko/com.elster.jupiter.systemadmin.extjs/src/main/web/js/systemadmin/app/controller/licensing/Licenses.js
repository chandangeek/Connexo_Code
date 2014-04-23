Ext.define('Sam.controller.licensing.Licenses', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Sam.store.Licensing'
    ],

    views: [
        'licensing.Overview',
        'licensing.Details'
    ],

    refs: [
        {
            ref: 'itemPanel',
            selector: 'licensing-details'
        },
        {
            ref: 'listPanel',
            selector: 'licensing-list'
        }
    ],

    init: function () {
        this.control({
            'licensing-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'licensing-overview licensing-list gridview': {
                itemclick: this.loadGridItemDetail,
                refresh: this.showDefaults
            }
        });
        this.gridItemModel = this.getModel('Sam.model.Licensing');
    },

    showOverview: function () {
        var widget = Ext.widget('licensing-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    setBreadcrumb: function (breadcrumbs) {
        var me = this;
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'System administration',
                href: me.getController('Sam.controller.history.Administration').tokenizeShowOverview()
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Licensing',
                href: 'licensing'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Licenses',
                href: 'licenses'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    loadGridItemDetail: function (grid, record) {
        var itemPanel = this.getItemPanel(),
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: itemPanel
            });
        if (this.displayedItemId != record.id) {
            grid.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = record.id;
        this.gridItemModel.load(record.data.id, {
            success: function (rec) {
                itemPanel.fireEvent('change', itemPanel, rec);
                preloader.destroy();
            },
            failure: function (rec) {
                preloader.destroy();
            }
        });
    },

    showDefaults: function (grid) {
        var itemPanel = this.getItemPanel(),
            index = 0,
            store = grid.getStore(),
            record = store.getAt(index);

        grid.selModel.doSelect(record);
        this.gridItemModel.load(record.data.id, {
            success: function (rec) {
                itemPanel.fireEvent('change', itemPanel, rec);
            }
        });
    }
});

