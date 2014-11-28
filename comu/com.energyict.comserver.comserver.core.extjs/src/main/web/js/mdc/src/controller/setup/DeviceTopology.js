Ext.define('Mdc.controller.setup.DeviceTopology', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.model.TopologyFilter'
    ],

    stores: [
        'Mdc.store.DeviceTopology'
    ],

    views: [
        'Mdc.view.setup.devicetopology.Setup'
    ],

    refs: [
        {ref: 'deviceTopology', selector: '#deviceTopologySetup'},
        {ref: 'sideFilter', selector: '#deviceTopologySetup #topologySideFilter'},
        {ref: 'sideFilterForm', selector: '#deviceTopologySetup #topologySideFilter form'},
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

    checkFilterIsEmpty: function () {
        var router = this.getController('Uni.controller.history.Router'),
            filter = router.filter;

        return Ext.isEmpty(filter.get('configuration')) && Ext.isEmpty(filter.get('sn')) && Ext.isEmpty(filter.get('mrid')) && Ext.isEmpty(filter.get('type'))
    },

    clearAllFilters: function () {
        if (!this.checkFilterIsEmpty()) {
            this.getDeviceTopology().down('#deviceTopologyGrid').getStore().removeAll();
        }
        this.getSideFilterForm().getRecord().getProxy().destroy();

    },

    searchClick: function () {
        this.getDeviceTopology().down('#deviceTopologyGrid').getStore().removeAll();
        this.getSideFilterForm().updateRecord();
        this.getSideFilterForm().getRecord().save();

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
        this.searchClick();
    },

    clearConfigurationCombo: function () {
        var sideFilter = this.getSideFilter();

        sideFilter.down('combobox[name=configuration]').setValue(null);
    },

    loadTopology: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            sideFilter = me.getSideFilter(),
            widget = me.getDeviceTopology(),
            filter = me.getTopFilter(),
            grid = widget.down('#deviceTopologyGrid'),
            topologyStore = grid.getStore(),
            mridField = sideFilter.down('#mrid'),
            serialField = sideFilter.down('#sn'),
            typeCombo = sideFilter.down('#type'),
            configurationCombo = sideFilter.down('#configuration'),
            properties = [],
            loadStoreWithFilter;

        loadStoreWithFilter = function () {
            me.getSideFilterForm().loadRecord(router.filter);

            properties = me.pushProperty(properties, mridField, filter, 'mrid');
            properties = me.pushProperty(properties, serialField, filter, 'serialNumber');
            properties = me.pushProperty(properties, typeCombo, filter, 'deviceTypeId');
            properties = me.pushProperty(properties, configurationCombo, filter, 'deviceConfigurationId');

            topologyStore.getProxy().setExtraParam('filter', Ext.encode(properties));
            topologyStore.load();
            widget.setLoading(false);
        };


        widget.setLoading();

        if (router.filter.get('type') && !Ext.isEmpty(router.filter.get('type')[0])) {
            typeCombo.getStore().load({
                callback: function () {
                    if (router.filter.get('type').length === 1) {
                        configurationCombo.getStore().getProxy().setExtraParam('deviceType', router.filter.get('type')[0]);
                        configurationCombo.show();
                        configurationCombo.getStore().load({
                            callback: function () {
                                loadStoreWithFilter();
                            }
                        })
                    } else {
                        loadStoreWithFilter();
                    }
                }
            });
        } else {
            loadStoreWithFilter();
        }

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
        filter.show();
        return properties;
    },

    showTopologyView: function (mRID) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            viewPort = Ext.ComponentQuery.query('viewport')[0],
            deviceTopologyStore,
            widget;


        viewPort.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget = Ext.widget('deviceTopologySetup', { device: device, router: router });
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                deviceTopologyStore = widget.down('#deviceTopologyGrid').getStore();
                deviceTopologyStore.getProxy().setUrl(device.get('mRID'));
                viewPort.setLoading(false);
                me.loadTopology();
            }
        });
    }

});
