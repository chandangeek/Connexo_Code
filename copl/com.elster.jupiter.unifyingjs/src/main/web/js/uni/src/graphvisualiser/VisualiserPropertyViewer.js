Ext.define('Uni.graphvisualiser.VisualiserPropertyViewer', {
    extend: 'Ext.form.Panel',
    minWidth: 300,
 //   height: 400,
 //   maxWidth: 500,
    itemId: 'uni-property-viewer',
    floating: true,
    collapsed: true,
    collapsible: true,
  //  collapseMode: 'header',
  //  collapseDirection: 'right',
    title: Uni.I18n.translate('general.deviceSummary', 'UNI', 'Device summary'),
    ui: 'small',
    layout: 'vbox',
    style: {
        'background-color': 'white'
    },
    //html: 'Test Panel',
    displayProperties: function(properties){
        var fieldLabel;

        this.removeAll();
        if(properties){
            for (var property in properties) {
                if (properties.hasOwnProperty(property)) {
                    var fieldLabel = undefined,
                        value = properties[property];
                    switch(property) {
                        case 'name':
                            fieldLabel = Uni.I18n.translate('general.name', 'UNI', 'Name');
                            break;
                        case 'type':
                            fieldLabel = Uni.I18n.translate('general.deviceType', 'UNI', 'Device type');
                            break;
                        case 'alarms':
                            fieldLabel = Uni.I18n.translate('general.alarms', 'UNI', 'Alarms');
                            if (value === 0) {
                                value = '-';
                            }
                            break;
                        default:
                            break;
                    }
                    if (!Ext.isEmpty(fieldLabel)) {
                        this.add({
                            xtype: 'displayfield',
                            value: value,
                            fieldLabel: fieldLabel
                        });
                    }
                }
            }
        }
        this.doLayout();
        this.expand();
    }
});