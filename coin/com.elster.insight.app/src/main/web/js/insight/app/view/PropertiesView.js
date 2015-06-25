Ext.define('InsightApp.view.PropertiesView', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.propertiesPanel',
    itemId: 'propertiesPanel',
    items: [
        {
            title: 'Properties',
            xtype: 'property-form',
            itemId: 'propertyForm',
            width: '100%'
        }
    ]
});

