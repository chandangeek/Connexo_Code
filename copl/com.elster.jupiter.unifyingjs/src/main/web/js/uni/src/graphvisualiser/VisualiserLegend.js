Ext.define('Uni.graphvisualiser.VisualiserLegend', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uniVisualiserLegend',
    //width: 100,
    height: 130,
    itemId: 'uni-visualiser-legend',
    floating: true,
    collapsed: true,
    collapsible: true,
    title: Uni.I18n.translate('general.legend', 'UNI', 'Legend'),
    ui: 'small',
    style: {
        'background-color': 'white'
    },
    layout: {
        type: 'vbox'
        //type: 'hbox',
        //align: 'stretch',
        //defaultMargins: {
        //    top: 0,
        //    right: 5,
        //    bottom: 0,
        //    left: 5
        //},
        //pack: 'center'
    },
    items: [
        {
            xtype: 'panel',
            itemId: 'uni-visualiser-legend-table',
            layout: {
                type: 'table',
                columns: 2
            }
        }
    ],
    listeners: {
        collapse: function(panel) {
            panel.doAlign();
        },
        expand: function(panel) {
            panel.doAlign();
        },
        click: function() {
            if (Ext.isEmpty(this.isDown)) {

            }
            this.alignTo(Ext.get('graph-drawing-area'), 'br-br', [-5, -15]);
        }
    },

    addItem: function(item) {
        var table = this.down('#uni-visualiser-legend-table');
        table.add(item);
        this.expand();
        this.doAlign();
        table.layout.columns = table.layout.columns + 1;
        //this.width = this.width + 75;
        this.doLayout();
    },

    clearAllItems: function() {
        //this.removeAll();
        //this.doAlign();
    },

    removeItems: function(typesToRemove) {
        var me = this,
            itemsToRemove = [];
        Ext.Array.each(this.items.items, function(item) {
            if (typesToRemove.indexOf(item.layerType) !== -1) {
                itemsToRemove.push(item);
            }
        });
        if (!Ext.isEmpty(itemsToRemove)) {
            Ext.Array.each(itemsToRemove, function(item) {
                me.remove(item);
            });
        }
        me.doAlign();
    },

    doAlign: function() {
        this.alignTo(Ext.get('graph-drawing-area'), 'br-br', [-5, -15]);
    }

});