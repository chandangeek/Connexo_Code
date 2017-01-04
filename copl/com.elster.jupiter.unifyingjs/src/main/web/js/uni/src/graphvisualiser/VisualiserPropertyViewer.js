Ext.define('Uni.graphvisualiser.VisualiserPropertyViewer', {
    extend: 'Ext.form.Panel',
    minWidth: 400,
 //   height: 400,
 //   maxWidth: 500,
    itemId: 'uni-property-viewer',
    floating: true,
    collapsed: true,
    collapsible: true,
  //  collapseMode: 'header',
  //  collapseDirection: 'right',
    title: Uni.I18n.translate('general.propertyViewer', 'UNI', 'Property viewer'),
    ui: 'small',
    layout: 'vbox',
    style: {
        'background-color': 'white'
    },

    displayProperties: function(properties){
        this.removeAll();
        if(properties){
            for (var property in properties) {
                if (properties.hasOwnProperty(property)) {
                    this.add({
                        xtype: 'displayfield',
                        value: properties[property].value,
                        htmlEncode: properties[property].htmlEncode,
                        fieldLabel: property,
                        labelWidth: 150
                    });
                }
            }
        }
        this.doLayout();
        this.expand();
    }
});