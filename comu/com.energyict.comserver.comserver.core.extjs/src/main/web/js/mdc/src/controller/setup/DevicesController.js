Ext.define('Mdc.controller.setup.DevicesController', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.form.filter.FilterCombobox'
    ],

    stores: [
        'Mdc.store.Devices',
        'Mdc.store.filter.DeviceTypes'
    ],

    /**
     * @cfg {String} itemId prefix for the component
     */
    prefix: '',

    init: function () {
        if (this.prefix) {
            var control = {};

            control[this.prefix + ' devices-sort-menu'] = {
                click: this.chooseSort
            };

            control[this.prefix + ' filter-toolbar #itemsContainer button'] = {
                click: this.switchSort
            };
            control[this.prefix + ' filter-toolbar button[action=clear]'] = {
                click: this.clearSort
            };
            control[this.prefix + ' filter-top-panel'] = {
                removeFilter: this.removeTheFilter,
                clearAllFilters: this.clearFilter
            };
            control[this.prefix + ' uni-filter-combo'] = {
                updateTopFilterPanelTagButtons: this.onFilterChange,
                specialkey: this.applyFilter
            };
            control[this.prefix + ' textfield'] = {
                specialkey: this.applyFilter
            };
            control[this.prefix + ' button[action=applyfilter]'] = {
                click: this.applyFilter
            };
            control[this.prefix + ' button[action=clearfilter]'] = {
                click: this.clearFilter
            };
            this.control(control);
        }

        this.callParent(arguments);
    },

    clearSort: function (btn) {
        var sortContainer = this.getSortingToolbar().getContainer();
        sortContainer.items.each(function (btns) {
            btns.destroy();
        });
        this.applyFilter();
    },

    applySort: function () {
        var sortContainer = this.getSortingToolbar().getContainer();
            store = this.getStore('Mdc.store.Devices'),
            sortItems = [];

        sortContainer.items.each(function (btns) {
            var dir = btns.sortDirection == Uni.component.sort.model.Sort.ASC
                ? 'ASC'
                : 'DSC';
            sortItems.push({property: btns.sortName, direction: dir});
        });
        store.getProxy().setExtraParam('sort', Ext.JSON.encode(sortItems));
    },

    switchSort: function (btn) {
        btn.sortDirection = btn.sortDirection == Uni.component.sort.model.Sort.ASC
            ? Uni.component.sort.model.Sort.DESC
            : Uni.component.sort.model.Sort.ASC;
        var iconCls = btn.sortDirection == Uni.component.sort.model.Sort.ASC
            ? 'x-btn-sort-item-asc'
            : 'x-btn-sort-item-desc';
        btn.setIconCls(iconCls);
        this.applyFilter();
    },

    chooseSort: function (menu, item) {
       var sortContainer = this.getSortingToolbar().getContainer();
       var value = item.value;

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
        //this.applyFilter();
    },

    createSortButton: function (button, container, name, sortName, text) {
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

    initFilter: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getSideFilterForm().loadRecord(router.filter);
        this.setFilterView();
    },

    applyFilter: function () {
        this.applySort();
        var filterForm = this.getSideFilterForm();
        filterForm.updateRecord();
        filterForm.getRecord().save();
    },

    clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeTheFilter: function (key) {
        var router = this.getController('Uni.controller.history.Router');
        var record = router.filter;
        switch (key) {
            default:
                record.set(key, null);
        }
        record.save();
    },

    onFilterChange: function (combo) {
        if (!_.isEmpty(combo.getRawValue())) {
            var filterView = this.getCriteriaPanel();
            filterView.setFilter(combo.getName(), combo.getFieldLabel(), combo.getRawValue());
        }
    },


    setFilterView: function () {
        var filterForm = this.getSideFilterForm();
        var filterView = this.getCriteriaPanel();

        var serialNumberField = filterForm.down('[name=serialNumber]');
        var serialNumberValue = serialNumberField.getValue().trim();
        var mRIDField = filterForm.down('[name=mRID]');
        var mRIDValue = mRIDField.getValue().trim();

        if (serialNumberValue != "") {
            filterView.setFilter('serialNumber', serialNumberField.getFieldLabel(), serialNumberValue);
        }
        if (mRIDValue != "") {
            filterView.setFilter('mRID', mRIDField.getFieldLabel(), mRIDValue);
        }
    },

    getSideFilterForm: function() {},

    getSortingToolbar: function() {},

    getSearchItems: function() {},

    getCriteriaPanel: function() {}
});