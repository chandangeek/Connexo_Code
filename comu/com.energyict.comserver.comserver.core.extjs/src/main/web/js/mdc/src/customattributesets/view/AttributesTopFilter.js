Ext.define('Mdc.customattributesets.view.AttributesTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'custom-attributes-top-filter',

    store: 'Mdc.customattributesets.store.CustomAttributeSets',

    filters: [
        {
            type: 'combobox',
            minWidth: 200,
            dataIndex: 'domain',
            emptyText: Uni.I18n.translate('customattributesets.topfilter.customattributetypeempty', 'MDC', 'Custom attribute set type'),
            displayField: 'localizedValue',
            valueField: 'value',
            store: 'Mdc.customattributesets.store.AttributeTypes'
        }
    ]
});