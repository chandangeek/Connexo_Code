Ext.define('Cps.customattributesets.view.AttributesTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'custom-attributes-top-filter',

    store: 'Cps.customattributesets.store.CustomAttributeSets',

    filters: [
        {
            type: 'combobox',
            minWidth: 200,
            dataIndex: 'domainExtension',
            emptyText: Uni.I18n.translate('customattributesets.topfilter.customattributetypeempty', 'CPS', 'Custom attribute set type'),
            displayField: 'localizedValue',
            valueField: 'value',
            store: 'Cps.customattributesets.store.AttributeTypes'
        }
    ]
});