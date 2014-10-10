Ext.define('Mdc.view.setup.devicesearch.DevicesSideFilter', {
    extend: 'Ext.panel.Panel',
    xtype: 'mdc-search-results-side-filter',

    requires: [
        'Uni.component.filter.view.Filter',
        //'Mdc.store.DeviceTypes',
        'Dsh.view.widget.common.SideFilterCombo',
        'Mdc.store.filter.DeviceTypes',
        //'Mdc.view.setup.devicesearch.SideFilterCombo',
        //'Mdc.view.setup.devicesearch.SideFilterCombo',
        //'Uni.form.filter.FilterCombobox',
        'Mdc.store.filter.DeviceTypes'
    ],

    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('searchItems.sideFilter.title', 'MDC', 'Search for devices'),
    ui: 'medium',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                ui: 'filter',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    labelAlign: 'top'
                },
                items: [
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
                        xtype: 'side-filter-combo',
                        name: 'deviceTypes',
                        itemId: 'type',
                        fieldLabel: Uni.I18n.translate('searchItems.type', 'MDC', 'Type'),
                        displayField: 'name',
                        valueField: 'id',
                        store: 'Mdc.store.filter.DeviceTypes',
                        //store: Ext.create('Mdc.store.DeviceTypes', {storeId: 'DeviceTypesCbSearch'}),
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
                        xtype: 'combobox',
                        triggerAction: 'all',
                        //xtype: 'side-filter-combo-devicesearch',
                        name: 'deviceConfigurations',
                        loadStore: false,
                        itemId: 'configuration',
                        store: Ext.create('Mdc.store.DeviceConfigurations'),
                        queryMode: 'local',
                        fieldLabel: Uni.I18n.translate('searchItems.configuration', 'MDC', 'Configuration'),
                        displayField: 'name',
                        hidden: true,
                        valueField: 'id',
                        forceSelection: false,
                        editable: false,
                        allowBlank: true,
                        multiSelect: true,
                        listConfig: {
                            getInnerTpl: function () {
                                return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {name} </div>';
                            }
                        }
                    }
                ],
                dockedItems: [
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
                ]
            }
        ];

        me.callParent(arguments);
    },

    onCollapseDeviceType: function () {
        var me = this,
            form = me.down('form'),
            typeConfig = me.down('#type'),
            comboConfig = form.down('#configuration');

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
            });
        } else {
            me.clearComboConfiguration(comboConfig);
        }
    },

    onChangeDeviceType: function (comp, newValue) {
        var me = this,
            form = me.down('form'),
            typeConfig = me.down('#type'),
            comboConfig = form.down('#configuration');

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
            });
        }
        if (newValue[0] === '') {
            me.clearComboConfiguration(comboConfig);
        }
    },

    clearComboConfiguration: function (cmbConfig) {
        cmbConfig.setValue('');
        cmbConfig.setVisible(false);
    }
});

