Ext.define('Uni.graphvisualiser.VisualiserLegendFloat', {
    extend: 'Ext.panel.Panel',
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
    ui: 'visualiser',
    shadow: false,
    style: {
        'background-color': 'white'
    },
    layout: {
        type: 'fit'
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

    addLegendItem: function(icon, text) {
        var legendTable = this.down('#uni-visualiser-legend-table');
        legendTable.add(
            {
                xtype: 'displayfield',
                fieldLabel: '',
                labelWidth: 0,
                margin: '0 5 0 0',
                iconForRenderer: icon, // to make it work when the legend panel is collapsed and the rendering is done later on when the panel expands
                renderer: function(raw, displayField) {
                    return displayField.iconForRenderer;
                }
            }
        );
        legendTable.add(
            {
                xtype: 'displayfield',
                fieldLabel: '',
                labelWidth: 0,
                margin: '0 15 0 0',
                value: text
            }
        );
    },

    reset: function() {
        this.down('#uni-visualiser-legend-table').removeAll();
    }
});