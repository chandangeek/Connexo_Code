Ext.define('Uni.graphvisualiser.VisualiserPropertyViewer', {
    extend: 'Ext.form.Panel',
    minWidth: 400,
 //   height: 400,
 //   maxWidth: 500,
    itemId: 'uni-property-viewer',
    floating: true,
    collapsed: true,
    collapsible: true,
    collapseDirection: 'top',
    hideCollapseTool: true,
    tools: [
        {
            xtype: 'button',
            itemId: 'uni-property-viewer-collapse-expand-button',
            ui: 'colexp',
            tooltip: Uni.I18n.translate('general.expand', 'UNI', 'Expand'),
            iconCls: 'icon-circle-down2',
            handler: function(button) {
                var propertyViewer = button.up('#uni-property-viewer');
                if (propertyViewer.getCollapsed()) {
                    propertyViewer.expand();
                    button.setIconCls('icon-circle-up2');
                    button.setTooltip(Uni.I18n.translate('general.collapse', 'UNI', 'Collapse'));
                } else {
                    propertyViewer.collapse();
                    button.setIconCls('icon-circle-down2');
                    button.setTooltip(Uni.I18n.translate('general.expand', 'UNI', 'Expand'));
                }
            }
        }
    ],
    listeners: {
        beforecollapse: function(panel) {
            var collapseExpandButton = panel.down('#uni-property-viewer-collapse-expand-button');
            collapseExpandButton.setIconCls('icon-circle-down2');
            collapseExpandButton.setTooltip(Uni.I18n.translate('general.expand', 'UNI', 'Expand'));
        },
        beforeexpand: function(panel) {
            var collapseExpandButton = panel.down('#uni-property-viewer-collapse-expand-button');
            collapseExpandButton.setIconCls('icon-circle-up2');
            collapseExpandButton.setTooltip(Uni.I18n.translate('general.collapse', 'UNI', 'Collapse'));
        }
    },
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