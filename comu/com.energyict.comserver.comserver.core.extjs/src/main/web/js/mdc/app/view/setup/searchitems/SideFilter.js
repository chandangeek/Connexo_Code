Ext.define('Mdc.view.setup.searchitems.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.search-side-filter',
    itemId: 'sideFilter',
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('searchItems.searchFor', 'MDC', 'Search for devices'),
    ui: 'filter',

    requires: [
        'Uni.component.filter.view.Filter',
        'Mdc.store.DeviceTypes',
        'Mdc.store.DeviceConfigurations'
    ],
    items: [
        {
        xtype: 'filter-form',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
                    {
                        xtype: 'textfield',
                        name: 'mrid',

                        itemId: 'mrid',
                        fieldLabel: Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID'),
                        labelAlign: 'top'
                    },
                    {
                        xtype: 'textfield',
                        name: 'sn',
                        itemId: 'sn',
                        fieldLabel: Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number'),
                        labelAlign: 'top'
                    },
                    {
                        xtype: 'combobox',
                        name: 'type',
                        itemId: 'type',
                        store: 'DeviceTypes',
                        fieldLabel: Uni.I18n.translate('searchItems.type', 'MDC', 'Type'),
                        displayField: 'name',
                        valueField: 'id',
                        forceSelection: false,
                        editable: false,
                        labelAlign: 'top',
                        allowBlank:true,
                        listeners: {
                            select: function (comp) {
                                if (comp.getValue() == "-1")
                                    comp.setValue(null);
                            },
                            change: function(comp, newValue){
                                var me = this.up('#sideFilter'),
                                    comboConfig = me.down('#configuration');
                                if (newValue != null && newValue != -1) {
                                    comboConfig.setVisible(true);
                                    var store = comboConfig.getStore();
                                    store.removeAll();
                                    store.getProxy().setExtraParam('deviceType', newValue);
                                    store.load(function(){
                                        store.insert(0, Ext.create('Mdc.model.DeviceConfiguration', {
                                            id: -1,
                                            name: '&nbsp;'
                                        }));
                                        comboConfig.bindStore(store);
                                        if (store.getCount() == 1) {
                                            me.clearComboConfiguration(comboConfig);
                                        }
                                    });
                                } else {
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
                        labelAlign: 'top',
                        allowBlank:true,
                        listeners: {
                            select: function (comp) {
                                if (comp.getValue() == "-1")
                                    comp.setValue(null);
                            }
                        }
                    }
                ]
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
        ],
        clearComboConfiguration: function(cmbConfig){
            cmbConfig.setValue(null);
            cmbConfig.setVisible(false);
        }
});