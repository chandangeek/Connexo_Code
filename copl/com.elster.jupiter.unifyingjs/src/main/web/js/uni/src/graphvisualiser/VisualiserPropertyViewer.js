Ext.define('Uni.graphvisualiser.VisualiserPropertyViewer', {
    extend: 'Ext.form.Panel',
    width: 300,
 //   height: 400,
    itemId: 'property-viewer',
    floating: true,
    collapsed: true,
    collapsible: true,
    title: 'Property viewer',
    ui: 'small',
    style: {
        'background-color': 'white'
    },
    //html: 'Test Panel',
    displayProperties: function(properties){
        this.removeAll();
        if(properties){
            for (var property in properties) {
                if (properties.hasOwnProperty(property)) {
                    this.add({
                        xtype: 'displayfield',
                        value: properties[property],
                        fieldLabel: property
                    })
                }
            }
        }
        this.expand();
    }
});