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
            'administration-licensing-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'administration-licensing-overview licensing-list actioncolumn': {
                click: this.showItemAction
            },
            'licensing-action-menu': {
                beforehide: this.hideItemAction,
                click: this.chooseAction
            },
            'administration-licensing-overview licensing-list gridview': {
                itemclick: this.loadGridItemDetail,
                refresh: this.showDefaults
            }
        });
        this.getApplication().on('addlicense', this.showDefault);
        this.actionMenuXtype = 'licensing-action-menu';
        this.gridItemModel = this.getModel('Isu.model.Licensing');
    },

    showDefault: function(last) {
        var self = this.getController('Isu.controller.Licensing');
        self.lastItem = last;
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

    showDefaults: function (grid) {
        var itemPanel = this.getItemPanel(),
            index = 0,
            store = grid.getStore(),
            record;
        if (this.lastItem) {
            record = store.getById(this.lastItem);
        } else {
            record = store.getAt(index);
        }
        grid.selModel.doSelect(record);
        this.gridItemModel.load(record.data.id, {
            success: function (rec) {
                itemPanel.fireEvent('change', itemPanel, rec);
            }
        });

    },

    chooseAction: function (menu, item) {
        var action = item.action;
        switch (action) {
            case 'upgrade':
                window.location.href = '#/issue-administration/datacollection/licensing/upgradelicense/' + menu.issueId;
                break;
        }
    }
});
