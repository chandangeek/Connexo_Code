Ext.define('Mdc.view.setup.devicechannels.DeviceTopologiesTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-view-setup-devicechannels-topologiestopfilter',

    requires: [
        'Mdc.store.DeviceTypes',
        'Mdc.store.DeviceConfigurations',
        'Mdc.store.filter.DeviceTypes'
    ],

    store: 'Mdc.store.DeviceTopology',

    initComponent: function () {
        var me = this;
        me.filters = [
            {
                type: 'text',
                dataIndex: 'name',
                emptyText: Uni.I18n.translate('deviceCommunicationTopology.name', 'MDC', 'Name')
            },
            {
                type: 'text',
                dataIndex: 'serialNumber',
                emptyText: Uni.I18n.translate('deviceCommunicationTopology.serialNumber', 'MDC', 'Serial number')
            },
            {
                type: 'combobox',
                itemId: 'mdc-topologytopfilter-device-types-combo',
                dataIndex: 'deviceTypeId',
                emptyText: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Mdc.store.filter.DeviceTypes',
                listeners: {
                    collapse: {
                        scope: me,
                        fn: me.onDeviceTypeChanged
                    }
                }
            },
            {
                type: 'combobox',
                itemId: 'mdc-topologytopfilter-device-configs-combo',
                dataIndex: 'deviceConfigurationId',
                emptyText: Uni.I18n.translate('searchItems.configuration', 'MDC', 'Configuration'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Mdc.store.DeviceConfigurations',
                loadStore: false,
                hidden: true
            }
        ];
        me.callParent(arguments);
    },

    onDeviceTypeChanged: function() {
        var me = this,
            typeCombo = me.down('#mdc-topologytopfilter-device-types-combo'),
            cfgCombo = me.down('#mdc-topologytopfilter-device-configs-combo'),
            cfgStore = cfgCombo.getStore();

        if (typeCombo.getValue().length === 1) {
            cfgCombo.setVisible(true);

            cfgStore.getProxy().setExtraParam('deviceType', typeCombo.getValue()[0]);
            cfgStore.load(function () {
                cfgStore.sort('name', 'ASC');
                if (cfgStore.getCount() === 0) {
                    me.clearCfgCombo(cfgCombo);
                }
                cfgCombo.select(cfgCombo.getValue());
            });
        } else {
            me.clearCfgCombo(cfgCombo);
        }
    },

    clearCfgCombo: function (combo) {
        combo.setValue('');
        combo.setVisible(false);
    }

});