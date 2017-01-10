Ext.define('Uni.graphvisualiser.VisualiserPropertyViewer', {
    extend: 'Ext.form.Panel',
    minWidth: 400,
 //   height: 400,
 //   maxWidth: 500,
    itemId: 'uni-property-viewer',
    floating: true,
    collapsed: true,
    collapsible: true,
    title: Uni.I18n.translate('general.propertyViewer', 'UNI', 'Property viewer'),
    ui: 'small',
    layout: 'vbox',
    style: {
        'background-color': 'white'
    },

    displayProperties: function(properties){
        var itemsToAdd = [];
        if(properties){
            for (var property in properties) {
                if (properties.hasOwnProperty(property)) {
                    itemsToAdd.push({
                        xtype: 'displayfield',
                        value: properties[property].value,
                        htmlEncode: properties[property].htmlEncode,
                        order: properties[property].order,
                        fieldLabel: property,
                        labelWidth: 150
                    });
                }
            }
        }
        // Sort them by their order attribute
        Ext.Array.sort(itemsToAdd, function(item1, item2){
            return item1.order - item2.order;
        });

        this.removeAll();
        this.add(itemsToAdd);
        this.doLayout();
        this.expand();
    }
});