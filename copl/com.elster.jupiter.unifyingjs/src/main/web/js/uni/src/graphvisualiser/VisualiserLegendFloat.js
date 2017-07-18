Ext.define('Uni.graphvisualiser.VisualiserLegendFloat', {
    extend: 'Ext.form.Panel',
    minWidth: 300,
    alias: 'widget.uniVisualiserLegendFloat',
    itemId: 'uni-legend-panel',
    floating: true,
    collapsed: true,
    collapsible: true,
    headerPosition: 'bottom',
    animCollapse: false,
    hideCollapseTool: true,
    tools: [
        {
            xtype: 'button',
            itemId: 'uni-legend-collapse-expand-button',
            ui: 'colexp',
            tooltip: Uni.I18n.translate('general.expand', 'UNI', 'Expand'),
            iconCls: 'icon-circle-up2',
            handler: function(button) {
                var legendPanel = button.up('uniVisualiserLegendFloat');
                if (legendPanel.getCollapsed()) {
                    legendPanel.expand();
                    legendPanel.alignTo(Ext.get('graph-drawing-area'), 'bl-bl', [-5, -15]);
                    button.setIconCls('icon-circle-down2');
                } else {
                    legendPanel.collapse();
                    legendPanel.alignTo(Ext.get('graph-drawing-area'), 'bl-bl', [-5, -15]);
                    button.setIconCls('icon-circle-up2');
                }
            }
        }
    ],
    listeners: {
        beforecollapse: function(panel) {
            var collapseExpandButton = panel.down('#uni-legend-collapse-expand-button');
            collapseExpandButton.setIconCls('icon-circle-up2');
            collapseExpandButton.setTooltip(Uni.I18n.translate('general.expand', 'UNI', 'Expand'));
        },
        beforeexpand: function(panel) {
            var collapseExpandButton = panel.down('#uni-legend-collapse-expand-button');
            collapseExpandButton.setIconCls('icon-circle-down2');
            collapseExpandButton.setTooltip(Uni.I18n.translate('general.collapse', 'UNI', 'Collapse'));
        }
    },
    title: Uni.I18n.translate('general.legend', 'UNI', 'Legend'),
    ui: 'small',
    style: {
        'background-color': 'white'
    },
    layout: {
        type: 'vbox'
    },
    items: [
        {
            xtype: 'panel',
            itemId: 'uni-visualiser-legend-table',
            layout: {
                type: 'table',
                columns: 4
            }
        }
    ],

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