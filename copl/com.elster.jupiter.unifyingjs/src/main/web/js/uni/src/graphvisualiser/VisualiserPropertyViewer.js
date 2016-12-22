Ext.define('Uni.graphvisualiser.VisualiserPropertyViewer', {
    extend: 'Ext.form.Panel',
    minWidth: 300,
 //   height: 400,
 //   maxWidth: 500,
    itemId: 'property-viewer',
    floating: true,
    collapsed: true,
    collapsible: true,
  //  collapseMode: 'header',
  //  collapseDirection: 'right',
    title: 'Property viewer',
    ui: 'small',
    layout: 'vbox',
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
        this.doLayout();
        this.expand();
    }
});