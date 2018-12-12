Ext.define('Uni.graphvisualiser.VisualiserPropertyViewer', {
    extend: 'Ext.panel.Panel',
    minWidth: 400,
    itemId: 'uni-property-viewer',
    floating: true,
    collapsed: true,
    collapsible: true,
    animCollapse: false,
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
                } else {
                    propertyViewer.collapse();
                    button.setIconCls('icon-circle-down2');
                }
            }
        }
    ],
    listeners: {
        beforecollapse: function(panel) {
            var collapseExpandButton = panel.down('#uni-property-viewer-collapse-expand-button');
            collapseExpandButton.setIconCls('icon-circle-down2');
        },
        beforeexpand: function(panel) {
            var collapseExpandButton = panel.down('#uni-property-viewer-collapse-expand-button');
            collapseExpandButton.setIconCls('icon-circle-up2');
        }
    },
    propertyViewerTitle: Uni.I18n.translate('general.propertyViewer', 'UNI', 'Property viewer'),
    ui: 'visualiser',
    shadow: false,
    layout: 'vbox',
    style: {
        'background-color': 'white'
    },

    initComponent: function () {
        this.setTitle(this.propertyViewerTitle);
        this.callParent();
    },

    displayProperties: function(properties, subTitle){
        var itemsToAdd = [];
        this.setTitle(this.propertyViewerTitle);
        if(properties){
            for (var property in properties) {
                if (property === 'Name') {
                    this.setTitle(this.propertyViewerTitle + ': ' + properties[property].value);
                }
                if (properties.hasOwnProperty(property)) {
                    itemsToAdd.push({
                        xtype: 'displayfield',
                        value: properties[property].value,
                        htmlEncode: properties[property].htmlEncode,
                        order: properties[property].order,
                        fieldLabel: property,
                        labelWidth: 150,
                        margin: '0 0 0 0'
                    });
                }
            }
        }
        // Sort them by their order attribute
        Ext.Array.sort(itemsToAdd, function(item1, item2){
            return item1.order - item2.order;
        });

        this.removeAll();
        if (!Ext.isEmpty(subTitle)) {
            this.add(
                {
                    xtype: 'displayfield',
                    htmlEncode: false,
                    value: subTitle,
                    fieldLabel: undefined,
                    margin: '0 0 10 10'
                }
            );
        }
        this.add(itemsToAdd);
        this.doLayout();
    }
});