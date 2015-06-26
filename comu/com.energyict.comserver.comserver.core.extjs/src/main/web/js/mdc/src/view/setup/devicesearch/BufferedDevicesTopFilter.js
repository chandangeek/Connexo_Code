Ext.define('Mdc.view.setup.devicesearch.BufferedDevicesTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-view-setup-devicesearch-buffereddevicestopfilter',

    store: 'Mdc.store.DevicesBuffered',

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'text',
                dataIndex: 'mRID',
                emptyText: Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID')
            },
            {
                type: 'text',
                dataIndex: 'serialNumber',
                emptyText: Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number')
            },
            {
                type: 'combobox',
                itemId: 'mdc-buffered-device-types-combo',
                dataIndex: 'deviceTypes',
                emptyText: Uni.I18n.translate('searchItems.type', 'MDC', 'Type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Mdc.store.filter.DeviceTypes',
                listeners: {
                    collapse: {
                        scope: this,
                        fn: this.onDeviceTypeChanged
                    }
                }
            },
            {
                type: 'combobox',
                itemId: 'mdc-buffered-device-configs-combo',
                dataIndex: 'deviceConfigurations',
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
            typeCombo = me.down('#mdc-buffered-device-types-combo'),
            cfgCombo = me.down('#mdc-buffered-device-configs-combo'),
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