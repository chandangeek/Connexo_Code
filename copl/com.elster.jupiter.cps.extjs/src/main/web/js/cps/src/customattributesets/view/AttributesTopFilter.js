/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.view.AttributesTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'custom-attributes-top-filter',

    store: 'Cps.customattributesets.store.CustomAttributeSets',

    filters: [
        {
            type: 'combobox',
            itemId: 'cps-attribute-set-type-combo',
            minWidth: 200,
            dataIndex: 'domainExtension',
            emptyText: Uni.I18n.translate('customattributesets.topfilter.customattributetypeempty', 'CPS', 'Custom attribute set type'),
            displayField: 'displayValue',
            valueField: 'id',
            store: 'Cps.customattributesets.store.AttributeTypes'
        }
    ]
});