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
        {ref: 'searchItems', selector: '#searchItems'}
    ],

    init: function () {
        this.control({
            '#searchItems breadcrumbTrail': {
                afterrender: this.showBreadCrumb
            },
            'items-sort-menu': {
                click: this.chooseSort
            },
            '#filteritemid button[action=clear]': {
                click: this.clearCriteria
            },
            '#sortitemid button[action=clear]': {
                click: this.clearSort
            },
            '#clearAllItems[action=clearfilter]': {
                click: this.clearAllItems
            },
            '#searchAllItems[action=applyfilter]': {
                click: this.searchClick
            },
            '#filteritemid button[action=customizeFilter]': {
                closeclick: this.filterCloseclick
            },
            '#sortitemid button': {
                closeclick: this.sortCloseclick
            },
            '#sortitemid #itemsContainer button': {
                click: this.switchSort
            },
            '#cancelSearching': {
                click: this.cancelSearching
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
            href: '#/administration'
        });
        breadcrumbParent.setChild(breadcrumbChild);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    searchAllItems: function() {

        var searchItems = this.getSearchItems(),
            criteriaContainer = searchItems.down('container[name=filter]').getContainer(),
            store = this.getStore('Mdc.store.Devices');

        if(searchItems.down('#mrid').getValue() != "") {
            var button = searchItems.down('button[name=mRIDBtn]');
            button = this.createCriteriaButton(button, criteriaContainer, 'mRIDBtn', Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID')+': '+searchItems.down('#mrid').getValue());
            store.getProxy().setExtraParam('mRID', searchItems.down('#mrid').getValue());
        } else {
            delete store.getProxy().extraParams.mRID;
        }
        if(searchItems.down('#sn').getValue() != "") {
            var button = searchItems.down('button[name=serialNumberBtn]');
            button = this.createCriteriaButton(button, criteriaContainer, 'serialNumberBtn', Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number')+': '+searchItems.down('#sn').getValue());
            store.getProxy().setExtraParam('serialNumber', searchItems.down('#sn').getValue());
        } else {
            delete store.getProxy().extraParams.serialNumber;
        }
        if(searchItems.down('#type').getValue() != null) {
            var button = searchItems.down('button[name=typeBtn]');
            button = this.createCriteriaButton(button, criteriaContainer, 'typeBtn', Uni.I18n.translate('searchItems.type', 'MDC', 'Type')+': '+searchItems.down('#type').getRawValue());
            store.getProxy().setExtraParam('deviceTypeName', searchItems.down('#type').getRawValue());
        } else {
            delete store.getProxy().extraParams.deviceTypeName;
        }

        this.applySort();

        searchItems.down('#resultsPanel').removeAll();
        searchItems.down('#resultsPanel').add(Ext.create('Mdc.view.setup.searchitems.SearchResults', {store: store}));

//        searchItems.down('#searchResults').store.on('beforeload', function setLastOperation(store, operation) {
//            store.lastOperation = operation;
//            this.removeListener('beforeload', setLastOperation);
//        }, this, { single: true });

        searchItems.down('#searchResults').store.on('load', function showResults() {
            searchItems.down('#contentLayout').getLayout().setActiveItem(1);
            this.removeListener('load', showResults);
        });
        searchItems.down('#contentLayout').getLayout().setActiveItem(2);
    },

    chooseSort: function (menu, item) {
        var searchItems = this.getSearchItems(),
            sortContainer = searchItems.down('container[name=sortitemspanel]').getContainer(),
            value = item.value;
        switch (value) {
            case 'mRID':
                var button = sortContainer.down('button[name=sortbymridbtn]');
                this.createSortButton(button, sortContainer, 'sortbymridbtn', item.value, item.text);
                break;
            case 'serialNumber':
                var button = sortContainer.down('button[name=sortbysnbtn]');
                this.createSortButton(button, sortContainer, 'sortbysnbtn', item.value, item.text);
                break;
            case 'deviceConfiguration.deviceType.name':
                var button = sortContainer.down('button[name=sortbytypebtn]');
                this.createSortButton(button, sortContainer, 'sortbytypebtn', item.value, item.text);
                break;
        }
        this.searchAllItems();
    },

    clearCriteria: function (btn) {
        var searchItems = this.getSearchItems(),
            criteriaContainer = searchItems.down('container[name=filter]').getContainer(),
            me = this;
        criteriaContainer.items.each(function (btns) {
            me.filterCloseclick(btns);
            btns.destroy();
        });
    },

    clearSort: function (btn) {
        var searchItems = this.getSearchItems(),
            sortContainer = searchItems.down('container[name=sortitemspanel]').getContainer();
        sortContainer.items.each(function (btns) {
            btns.destroy();
        });
        this.searchAllItems();
    },

    clearFilterContent: function (criteriaContainer) {
        criteriaContainer.items.each(function (btns) {
            btns.destroy();
        });
    },

    clearSortContent: function (sortContainer) {
        sortContainer.items.each(function (btns) {
            btns.destroy();
        });
    },

    switchSort: function (btn) {
        btn.sortDirection = btn.sortDirection == Uni.component.sort.model.Sort.ASC
            ? Uni.component.sort.model.Sort.DESC
            : Uni.component.sort.model.Sort.ASC;
        var iconCls = btn.sortDirection == Uni.component.sort.model.Sort.ASC
            ? 'x-btn-sort-item-asc'
            : 'x-btn-sort-item-desc';
        btn.setIconCls(iconCls);
        this.searchAllItems();
    },

    searchClick: function(btn) {
        var searchItems = this.getSearchItems();
        this.clearFilterContent(searchItems.down('container[name=filter]').getContainer());
        this.searchAllItems();
    },

    clearAllItems: function(btn) {
        var searchItems = this.getSearchItems();
        searchItems.down('#mrid').setValue("");
        searchItems.down('#sn').setValue("");
        searchItems.down('#type').setValue(null);
        this.clearFilterContent(searchItems.down('container[name=filter]').getContainer());
        this.clearFilterContent(searchItems.down('container[name=sortitemspanel]').getContainer());
        this.searchAllItems();
     },

    createCriteriaButton: function(button, container, name, text) {
        if (Ext.isEmpty(button)) {
            button = new Skyline.button.TagButton({
                text: text,
                name: name,
                action: 'customizeFilter'
            });
            container.add(button);
        } else {
            button.setText(text);
        }
    },

    createSortButton: function(button, container, name, sortName, text) {
        if (Ext.isEmpty(button)) {
            container.add({
                xtype: 'sort-item-btn',
                name: name,
                sortName: sortName,
                text: text,
                iconCls: 'x-btn-sort-item-asc',
                sortDirection: Uni.component.sort.model.Sort.ASC
            });
        }
    },

    filterCloseclick: function(btn) {
        var searchItems = this.getSearchItems();
        switch (btn.name) {
            case 'mRIDBtn':
                searchItems.down('#mrid').setValue("");
                break;
            case 'serialNumberBtn':
                searchItems.down('#sn').setValue("");
                break;
        case 'typeBtn':
            searchItems.down('#type').setValue(null);
            break;
        }
        this.searchAllItems();
    },

    sortCloseclick: function(btn) {
        btn.destroy();
        this.searchAllItems();
    },

    applySort: function() {
        var searchItems = this.getSearchItems(),
            sortContainer = searchItems.down('container[name=sortitemspanel]').getContainer(),
            store = this.getStore('Mdc.store.Devices'),
            sortItems = [];

        sortContainer.items.each(function (btns) {
            var dir = btns.sortDirection == Uni.component.sort.model.Sort.ASC
                ? 'ASC'
                : 'DSC';
            sortItems.push({property:btns.sortName,direction:dir});
        });
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sortItems));
    },

    cancelSearching: function(btn) {

    }
});