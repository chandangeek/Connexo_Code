Ext.define('Mdc.view.setup.devicesearch.DevicesSideFilter', {
    extend: 'Ext.form.Panel',
    xtype: 'mdc-search-results-side-filter',

    ui: 'filter',

    requires: [
        'Uni.component.filter.view.Filter',
        'Mdc.store.filter.DeviceTypes',
        'Uni.form.filter.FilterCombobox',
        'Mdc.store.filter.DeviceTypes'
    ],

    cls: 'filter-form',
    title: Uni.I18n.translate('searchItems.sideFilter.title', 'MDC', 'Filter'),

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    defaults: {
        labelAlign: 'top'
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'textfield',
                name: 'mRID',
                fieldLabel: Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID')
            },
            {
                xtype: 'textfield',
                name: 'serialNumber',
                fieldLabel: Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number')
            },
            {
                xtype: 'uni-filter-combo',
                name: 'deviceTypes',
                itemId: 'type',
                fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                displayField: 'name',
                valueField: 'id',
                store: Ext.getStore('Mdc.store.filter.DeviceTypes') || Ext.create('Mdc.store.filter.DeviceTypes'),
                listeners: {
                    collapse: {
                        scope: me,
                        fn: me.onCollapseDeviceType
                    },
                    change: {
                        scope: me,
                        fn: me.onChangeDeviceType
                    }
                }
            },
            {
                xtype: 'uni-filter-combo',
                triggerAction: 'all',
                name: 'deviceConfigurations',
                loadStore: false,
                itemId: 'configuration',
                store: Ext.create('Mdc.store.DeviceConfigurations'),
                queryMode: 'local',
                fieldLabel: Uni.I18n.translate('searchItems.configuration', 'MDC', 'Configuration'),
                displayField: 'name',
                hidden: true,
                valueField: 'id'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'toolbar',
                dock: 'bottom',
                items: [
                    {
                        text: Uni.I18n.translate('searchItems.sideFilter.apply', 'MDC', 'Search'),
                        ui: 'action',
                        action: 'applyfilter'
                    },
                    {
                        text: Uni.I18n.translate('searchItems.sideFilter.clearAll', 'MDC', 'Clear all'),
                        action: 'clearfilter'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    onCollapseDeviceType: function () {
        this.updateConfigs();
    },

    onChangeDeviceType: function (comp, newValue) {
        this.updateConfigs();
    },

    updateConfigs: function () {
        var me = this,
            form = me.down('form'),
            typeConfig = me.down('#type'),
            comboConfig = me.down('#configuration');

        if (typeConfig.getValue().length === 1) {
            var store = comboConfig.getStore();
            comboConfig.setVisible(true);

            store.getProxy().setExtraParam('deviceType', typeConfig.getValue()[0]);
            store.load(function () {
                store.sort('name', 'ASC');
                comboConfig.bindStore(store);
                if (store.getCount() === 0) {
                    me.clearComboConfiguration(comboConfig);
                }
                comboConfig.select(comboConfig.getValue());
                comboConfig.fireEvent('updateTopFilterPanelTagButtons', comboConfig);
            });
        }
        else {
            me.clearComboConfiguration(comboConfig);
        }
    },

    clearComboConfiguration: function (cmbConfig) {
        cmbConfig.setValue('');
        cmbConfig.setVisible(false);
    }
});

