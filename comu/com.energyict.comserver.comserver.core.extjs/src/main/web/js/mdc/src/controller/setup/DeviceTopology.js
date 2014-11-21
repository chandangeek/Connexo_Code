Ext.define('Mdc.controller.setup.DeviceTopology', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.DeviceTopology'
    ],

    views: [
        'Mdc.view.setup.devicetopology.Setup'
    ],

    refs: [
        {ref: 'deviceTopology', selector: '#deviceTopologySetup'},
        {ref: 'sideFilter', selector: '#deviceTopologySetup search-side-filter'},
        {ref: 'topFilter', selector: '#deviceTopologySetup #topFilterDeviceTopology'}
    ],

    init: function () {
        this.control({
            '#deviceTopologySetup #clearAllItems[action=clearfilter]': {
                click: this.clearAllFilters
            },
            '#deviceTopologySetup #searchAllItems[action=applyfilter]': {
                click: this.searchClick
            },
            '#deviceTopologySetup search-side-filter combobox[name=type]': {
                change: this.clearConfigurationCombo
            },
            '#deviceTopologySetup #topFilterDeviceTopology': {
                removeFilter: this.removeFilter,
                clearAllFilters: this.clearAllFilters
            },
            '#deviceTopologySetup search-side-filter filter-form': {
                applyfilter: this.searchClick
            }
        });
    },

    clearAllItems: function () {
        var sideFilter = this.getSideFilter(),
            mridField = sideFilter.down('#mrid'),
            serialField = sideFilter.down('#sn'),
            typeCombo = sideFilter.down('#type'),
            configurationCombo = sideFilter.down('#configuration');

        mridField.setValue(null);
        serialField.setValue(null);
        typeCombo.setValue(null);
        configurationCombo.setValue(null);
        configurationCombo.hide();
    },

    clearAllFilters: function () {
        this.clearAllItems();
        this.loadTopology();
    },

    searchClick: function () {
        this.loadTopology();
    },

    removeFilter: function (key) {
        var sideFilter = this.getSideFilter(),
            confField,
            field;

        if (key === 'mrid' || key === 'sn') {
            field = sideFilter.down('textfield[name=' + key + ']');
        } else {
            field = sideFilter.down('combobox[name=' + key + ']');
            if (key === 'type') {
                confField = sideFilter.down('combobox[name=configuration]');
                confField.setValue(null);
                confField.hide();
            }
        }

        field.setValue(null);
        this.loadTopology();
    },

    clearConfigurationCombo: function() {
        var sideFilter = this.getSideFilter();

        sideFilter.down('combobox[name=configuration]').setValue(null);
    },

    loadTopology: function () {
        var me = this,
            sideFilter = this.getSideFilter(),
            widget = this.getDeviceTopology(),
            filter = this.getTopFilter(),
            filterBtns = Ext.ComponentQuery.query('#topFilterDeviceTopology tag-button'),
            grid = widget.down('#deviceTopologyGrid'),
            topologyStore = grid.getStore(),
            mridField = sideFilter.down('#mrid'),
            serialField = sideFilter.down('#sn'),
            typeCombo = sideFilter.down('#type'),
            configurationCombo = sideFilter.down('#configuration'),
            properties = [];


        widget.setLoading();

        Ext.each(filterBtns, function (btn) {
            btn.destroy();
        });

        properties = this.pushProperty(properties, mridField, filter, 'mrid');
        properties = this.pushProperty(properties, serialField, filter, 'serialNumber');
        properties = this.pushProperty(properties, typeCombo, filter, 'deviceTypeId');
        properties = this.pushProperty(properties, configurationCombo, filter, 'deviceConfigurationId');

        topologyStore.getProxy().setExtraParam('filter', Ext.encode(properties));
        topologyStore.load({
            callback: function () {
                widget.setLoading(false);
            }
        });
    },

    pushProperty: function (properties, field, filter, propertyName) {
        var fieldType = field.xtype,
            value = field.getValue(),
            first = true,
            comboNames = '';

        if (value) {
            switch (fieldType) {
                case 'textfield':
                    properties = this.setProperty(properties, field, filter, propertyName, value);
                    break;
                case 'combobox':
                    if (!Ext.isEmpty(value[0])) {
                        Ext.each(value, function (id) {
                            if (first) {
                                comboNames = field.findRecord(field.valueField, id).get('name');
                                first = false;
                            } else {
                                comboNames += ", " + field.findRecord(field.valueField, id).get('name');
                            }
                        });

                        properties = this.setProperty(properties, field, filter, propertyName, comboNames);
                    }
                    break;
            }
        }

        return properties;
    },

    setProperty: function (properties, field, filter, propertyName, filterValue) {
        var fieldLabel = field.fieldLabel,
            itemId = field.itemId,
            value = field.getValue();

        properties.push({
            property: propertyName,
            value: value
        });

        filter.setFilter(itemId, fieldLabel, filterValue, false);
        return properties;
    },

    showTopologyView: function (mRID) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            deviceTopologyStore,
            widget;


        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget = Ext.widget('deviceTopologySetup', { device: device, router: router });
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                deviceTopologyStore = widget.down('#deviceTopologyGrid').getStore();
                deviceTopologyStore.getProxy().setUrl(device.get('mRID'));
                me.loadTopology();
            }
        });
    }

});
