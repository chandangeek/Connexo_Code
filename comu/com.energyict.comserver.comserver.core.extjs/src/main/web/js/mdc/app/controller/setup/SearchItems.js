Ext.define('Mdc.controller.setup.SearchItems', {
    extend: 'Ext.app.Controller',

    requires: [
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
            },
            '#globalSearch[action=search]': {
                click: this.showSearchByHistory
            }
        });
    },

    showSearchByHistory  : function () {
        location.href = '#/searchitems';
    },

    showSearchItems : function () {
        var widget = Ext.widget('searchItems');
        this.getApplication().fireEvent('changecontentevent', widget);
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

        if(searchItems.down('#type').getValue() != "") {
            var button = searchItems.down('button[name=typeBtn]');
            button = this.createCriteriaButton(button, criteriaContainer, 'typeBtn', Uni.I18n.translate('searchItems.type', 'MDC', 'Type')+': '+searchItems.down('#type').getRawValue());
            store.getProxy().setExtraParam('deviceTypeName', searchItems.down('#type').getRawValue());
        } else {
            delete store.getProxy().extraParams.deviceTypeName;
        }

        if(searchItems.down('#configuration').getValue() != "") {
            var button = searchItems.down('button[name=configurationBtn]');
            button = this.createCriteriaButton(button, criteriaContainer, 'configurationBtn', Uni.I18n.translate('searchItems.configuration', 'MDC', 'Configuration')+': '+searchItems.down('#configuration').getRawValue());
            store.getProxy().setExtraParam('deviceConfigurationName', searchItems.down('#configuration').getRawValue());
        } else {
            delete store.getProxy().extraParams.deviceConfigurationName;
        }

        this.applySort();

        searchItems.down('#resultsPanel').removeAll();
        searchItems.down('#resultsPanel').add(Ext.create('Mdc.view.setup.searchitems.SearchResults', {store: store}));

        var isFilterSet = this.isFilterFilled(searchItems);
        this.showSearchContentContainer(isFilterSet);

        if (isFilterSet) {
            searchItems.down('#searchResults').store.on('load', function showResults() {
                searchItems.down('#contentLayout').getLayout().setActiveItem(1);
                this.removeListener('load', showResults);
            });
            searchItems.down('#contentLayout').getLayout().setActiveItem(2);
        } else {
            searchItems.down('#contentLayout').getLayout().setActiveItem(0);
        }
    },

    isFilterFilled: function (srcItems) {
        var criteriaContainer = srcItems.down('container[name=filter]').getContainer(),
            sortContainer = srcItems.down('container[name=sortitemspanel]').getContainer();
        return criteriaContainer.items.length > 0 || sortContainer.items.length > 0;
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
            case 'deviceConfiguration.name':
                var button = sortContainer.down('button[name=sortbyconfigurationbtn]');
                this.createSortButton(button, sortContainer, 'sortbyconfigurationbtn', item.value, item.text);
                break;
        }

        this.searchAllItems();
    },

    clearCriteria: function (btn) {
        this.clearAllCriteria();
        this.searchAllItems();
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

    clearAllCriteria: function() {
        var searchItems = this.getSearchItems();
        searchItems.down('#mrid').setValue("");
        searchItems.down('#sn').setValue("");
        searchItems.down('#type').setValue("");
        searchItems.down('#configuration').setValue("");
        this.clearFilterContent(searchItems.down('container[name=filter]').getContainer());
    },

    clearAllItems: function(btn) {
        var searchItems = this.getSearchItems();
        this.clearAllCriteria();
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
                searchItems.down('#mrid').setValue('');
                break;
            case 'serialNumberBtn':
                searchItems.down('#sn').setValue('');
                break;
            case 'typeBtn':
                searchItems.down('#type').setValue('');
                searchItems.down('#configuration').setValue('');
                if (searchItems.down('button[name=configurationBtn]') != null) {
                    searchItems.down('button[name=configurationBtn]').destroy();
                }
                break;
            case 'configurationBtn':
                searchItems.down('#configuration').setValue('');
                break;
        }
        btn.destroy();
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
        var me = this,
            searchItems = this.getSearchItems();
        searchItems.down('#searchResults').getStore().clearListeners();
        me.clearAllItems();
    },

    showSearchContentContainer: function(isVisible) {
        var searchItems = this.getSearchItems(),
            centerConatiner = searchItems.getCenterContainer();
        if (isVisible != centerConatiner.down('#searchContentFilter').isVisible()) {
            isVisible
                ? centerConatiner.down('#searchContentFilter').show()
                : centerConatiner.down('#searchContentFilter').hide();
        }
    }
});