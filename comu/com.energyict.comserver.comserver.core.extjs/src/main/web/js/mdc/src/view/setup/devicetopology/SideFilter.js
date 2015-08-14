Ext.define('Mdc.view.setup.devicetopology.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.topology-side-filter',
    itemId: 'topologySideFilter',
    cls: 'filter-form',
    title: Uni.I18n.translate('general.filter', 'MDC', 'Filter'),
    width: 285,
    applyBtnText: Uni.I18n.translate('general.filter', 'MDC', 'Filter'),
    cleatBtnText: Uni.I18n.translate('general.clearAll', 'MDC', 'Clear all'),


    ui: 'medium',

    requires: [
        'Mdc.store.DeviceTypes',
        'Mdc.store.DeviceConfigurations'
    ],

    items: [
        {
            xtype: 'form',
            ui: 'filter',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                labelAlign: 'top',
                labelPad: 0
            },
            items: [
                {
                    xtype: 'textfield',
                    name: 'mrid',
                    itemId: 'mrid',
                    fieldLabel: Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID')
                },
                {
                    xtype: 'textfield',
                    name: 'sn',
                    itemId: 'sn',
                    fieldLabel: Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number')
                },
                {
                    xtype: 'combobox',
                    name: 'type',
                    itemId: 'type',
                    fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                    displayField: 'name',
                    valueField: 'id',

                    forceSelection: false,
                    editable: false,
                    allowBlank: true,
                    multiSelect: true,
                    triggerAction: 'all',
                    listConfig: {
                        getInnerTpl: function () {
                            return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {name} </div>';
                        }
                    },
                    listeners: {
                        collapse: function () {
                            var me = this.up('#topologySideFilter'),
                                comboConfig = me.down('#configuration');
                            if (this.getValue().length === 1 && this.getValue()[0] !== '') {
                                var store = comboConfig.getStore();
                                comboConfig.setVisible(true);
                                comboConfig.disable();
                                store.getProxy().setExtraParam('deviceType', this.getValue()[0]);
                                store.load({
                                    callback: function () {
                                        store.sort('name', 'ASC');
                                        comboConfig.bindStore(store);
                                        comboConfig.enable();
                                        if (store.getCount() === 0) {
                                            me.clearComboConfiguration(comboConfig);
                                        }
                                    }
                                });
                            } else {
                                me.clearComboConfiguration(comboConfig);
                            }
                        },
                        change: function (comp, newValue) {
                            var me = comp.up('#topologySideFilter'),
                                comboConfig = me.down('#configuration');
                            if (newValue[0] === '') {
                                me.clearComboConfiguration(comboConfig);
                            }
                        }
                    }
                },
                {
                    xtype: 'combobox',
                    name: 'configuration',
                    itemId: 'configuration',
                    store: 'Mdc.store.DeviceConfigurations',
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
                    itemId: 'dockedToolbar',
                    dock: 'bottom'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
        var dockedToolbar = this.down('#dockedToolbar');

        dockedToolbar.add([
            {
                text: this.applyBtnText,
                ui: 'action',
                itemId: 'searchAllItems',
                action: 'applyfilter'
            },
            {
                text: this.cleatBtnText,
                itemId: 'clearAllItems',
                action: 'clearfilter'
            }
        ]);

        var combo = this.down('form #type'),
            store = Ext.create('Mdc.store.DeviceTypes', {storeId: 'DeviceTypesCbSearch'});

        if (combo.rendered) {
            combo.setStore(store);
        } else {
            combo.store = store;
        }
    },

    clearComboConfiguration: function (cmbConfig) {
        cmbConfig.setValue('');
        cmbConfig.setVisible(false);
    }
});