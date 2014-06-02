Ext.define('Mdc.view.setup.searchitems.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.search-side-filter',
    itemId: 'sideFilter',
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('searchItems.searchFor', 'MDC', 'Search for devices'),
    ui: 'medium',

    requires: [
        'Uni.component.filter.view.Filter',
        'Mdc.store.DeviceTypes',
        'Mdc.store.DeviceConfigurations'
    ],
    items: [
        {
            xtype: 'filter-form',
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
                    store: new Mdc.store.DeviceTypes({
                        storeId:'DeviceTypesCbSearch'
                    }),
                    fieldLabel: Uni.I18n.translate('searchItems.type', 'MDC', 'Type'),
                    displayField: 'name',
                    valueField: 'id',

                    forceSelection: false,
                    editable: false,
                    allowBlank:true,
                    multiSelect: true,
                    triggerAction: 'all',
                    listConfig : {
                        getInnerTpl : function() {
                            return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {name} </div>';
                        }
                    },
                    listeners:{
                        collapse: function(){
                            var me = this.up('#sideFilter'),
                                comboConfig = me.down('#configuration');
                            if (this.getValue().length == 1) {
                                var store = comboConfig.getStore();
                                comboConfig.setVisible(true);
                                store.getProxy().setExtraParam('deviceType', this.getValue()[0]);
                                store.load(function(){
                                    store.sort('name', 'ASC');
                                    comboConfig.bindStore(store);
                                    if (store.getCount() == 0) {
                                        me.clearComboConfiguration(comboConfig);
                                    }
                                });
                            } else {
                                me.clearComboConfiguration(comboConfig);
                            }
                        },
                        change: function(comp, newValue) {
                            var me = comp.up('#sideFilter'),
                                comboConfig = me.down('#configuration');
                            if (newValue == "") {
                                me.clearComboConfiguration(comboConfig);
                            }
                        }
                    }
                },
                {
                    xtype: 'combobox',
                    name: 'type',
                    itemId: 'configuration',
                    store: 'DeviceConfigurations',
                    queryMode: 'local',
                    fieldLabel: Uni.I18n.translate('searchItems.configuration', 'MDC', 'Configuration'),
                    displayField: 'name',
                    hidden: true,
                    valueField: 'id',
                    forceSelection: false,
                    editable: false,
                    allowBlank: true,
                    multiSelect: true,
                    listConfig : {
                        getInnerTpl : function() {
                            return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {name} </div>';
                        }
                    }
                }
            ],
            buttons: [
                {
                    text: Uni.I18n.translate('searchItems.clearAll', 'MDC', 'Search'),
                    itemId: 'searchAllItems',
                    action: 'applyfilter'
                },
                {
                    text: Uni.I18n.translate('searchItems.clearAll', 'MDC', 'Clear all'),
                    itemId: 'clearAllItems',
                    action: 'clearfilter'
                }
            ]
        }
    ],
    clearComboConfiguration: function(cmbConfig){
        cmbConfig.setValue("");
        cmbConfig.setVisible(false);
    }
});