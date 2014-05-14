Ext.define('Mdc.controller.setup.SearchItems', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Ext.ux.window.Notification'
    ],

    views: [
        'setup.searchitems.SearchItems',
        'setup.searchitems.ContentFilter',
        'setup.searchitems.ContentLayout',
        'setup.searchitems.SideFilter'
    ],

    stores: [
        'Mdc.store.Devices'
    ],

    refs: [
    ],

    init: function () {
        this.control({
            '#searchItems breadcrumbTrail': {
                afterrender: this.showBreadCrumb
            },
            'items-sort-menu': {
                click: this.chooseSort
            },
            'button[name=clearitemssortbtn]': {
                click: this.clearSort
            },
            'button[action=clear]': {
                click: this.clearCriteria
            },
            'button[name=sortbymridbtn]': {
                click: this.switchSort
            },
            'button[name=sortbysnbtn]': {
                click: this.switchSort
            },
            '#clearAllItems[action=clearfilter]': {
                click: this.clearAllItems
            },
            '#searchAllItems[action=applyfilter]': {
                click: this.searchAllItems
            },
            'button[action=customizeFilter]': {
                closeclick: this.closeclick
            }
        });
    },

    showSearchItems : function () {
        var widget = Ext.widget('searchItems');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },

    showBreadCrumb: function (breadcrumbs) {
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('searchItems.searchItems', 'MDC', 'Search items'),
            href: 'searchitems'
        });

        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        breadcrumbParent.setChild(breadcrumbChild);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    chooseSort: function (menu, item) {

    },

    clearSort: function (btn) {
        var sortBtnsPanel = btn.up('panel[name=sortpanel]').down('panel[name=sortitemsbtns]');
        sortBtnsPanel.items.each(function (btns) {
            btns.destroy();
        });
        btn.setDisabled(true);
    },

    clearCriteria: function (btn) {
        var searchItems = Ext.getCmp('search-items-id'),
            criteriaContainer = searchItems.down('container[name=filter]').getContainer();
        criteriaContainer.items.each(function (btns) {
            btns.destroy();
        });
        btn.setDisabled(true);
    },

    switchSort: function (btn) {
        console.log('switch items sorting'); // todo: switch sort
    },

    clearAllItems: function(btn) {
        var searchItems = Ext.getCmp('search-items-id');
        searchItems.down('#mrid').setValue("");
        searchItems.down('#sn').setValue("");
        this.clearSort(searchItems.down('button[name=clearitemssortbtn]'));
        this.clearCriteria(searchItems.down('button[name=clearitemsfilterbtn]'));
    },

    searchAllItems: function(btn) {
        var searchItems = Ext.getCmp('search-items-id'),
            criteriaContainer = searchItems.down('container[name=filter]').getContainer(),
            store = this.getStore('Mdc.store.Devices');

        if(searchItems.down('#mrid').getValue() != "") {
            var button = searchItems.down('button[name=mRIDBtn]');
            button = this.createCriteriaButton(button, 'mRIDBtn', Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID')+': '+searchItems.down('#mrid').getValue());
            criteriaContainer.add(button);
            store.getProxy().setExtraParam('mRID', searchItems.down('#mrid').getValue());
        } else {
            delete store.getProxy().extraParams.mRID;
        }
        if(searchItems.down('#sn').getValue() != "") {
            var button = searchItems.down('button[name=serialNumberBtn]');
            button = this.createCriteriaButton(button, 'serialNumberBtn', Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number')+': '+searchItems.down('#sn').getValue());
            criteriaContainer.add(button);
            store.getProxy().setExtraParam('serialNumber', searchItems.down('#sn').getValue());
        } else {
            delete store.getProxy().extraParams.serialNumber;
        }

        store.load({
            callback: function (devices) {
                // TODO: display results
                searchItems.down('#resultsPanel').removeAll()
                searchItems.down('#resultsPanel').add(Ext.create('Mdc.view.setup.searchitems.SearchResults'));
                searchItems.down('#contentLayout').getLayout().setActiveItem(1);
                searchItems.down('#searchResults').reconfigure(store);

                searchItems.down('#searchItemsToolbarBottom').reconfigure(store);
                searchItems.down('#searchItemsToolbarTop').reconfigure(store);
            }
        });
        searchItems.down('#contentLayout').getLayout().setActiveItem(2);
    },

    createCriteriaButton: function(button, name, text) {
        if (Ext.isEmpty(button)) {
            button = new Skyline.button.TagButton({
                text: text,
                name: name,
                action: 'customizeFilter'
            });
        } else {
            button.setText(text);
        }
        return button;
    },

    closeclick: function(btn) {
        var searchItems = Ext.getCmp('search-items-id');
        switch (btn.name) {
            case 'mRIDBtn':
                searchItems.down('#mrid').setValue("");
                break;
            case 'serialNumberBtn':
                searchItems.down('#sn').setValue("");
                break;
        }
        this.searchAllItems(btn);
    }
});