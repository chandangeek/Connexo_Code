Ext.define('Mdc.networkvisualiser.view.NetworkVisualiserView', {
    extend: 'Uni.graphvisualiser.VisualiserPanel',
    alias: 'widget.visualiserpanel',
    requires: [
        'Mdc.networkvisualiser.view.NetworkVisualiserMenu'
    ],
    itemId: 'visualiserpanel',
    menu: 'Mdc.networkvisualiser.view.NetworkVisualiserMenu',
    contextMenuItems: [
        {
            text: 'TestComponent', handler: function (menuItem) {
                menuItem.visualiser.showLinkQuality(menuItem.graphId);
            }
        }
    ],


    initComponent: function () {
        this.callParent();
    },

    showLinkQuality: function(){
        var me = this;
        me.forEachLink(function(link){
            var colors = ['#FF0000','#CC3300','#996600','#669900','#33CC00'];
            return {
                id: link.id,
                w: link.d.linkQuality*2,
                c: colors[link.d.linkQuality-1],
                t: link.d.linkQuality
            };
        })
    },

    showDeviceType: function(){
        var me = this;
        me.forEachNode(function(node){
            if(node.d && node.d.type === 'device'){
                icon = KeyLines.getFontIcon('icon-calculator')
            } else {
                icon = KeyLines.getFontIcon('icon-station2')
            }
            return {
                        id: node.id,
                        b: null,
                        fi: {
                            c: '#8e8e8e',
                            t: icon
                        }
            }
        });
    },

    showAlarms: function(){
        var me = this;
        me.forEachNode(function(node){
            if (node.d.alarms) {
                return {
                    id: node.id,
                    g: [
                        {
                            c: 'rgb(255, 0, 0)',
                            p: 'se',
                            t: node.d.alarms
                        }
                    ],
                    ha0: {
                        c: 'rgb(255, 0, 0)',
                        r: 50,
                        w: 3
                    }
                };
            }
        });
    },

    showHopLevel: function(){
        var me = this;
        var distances = me.chart.graph().distances(this.top);
        var labels = Object.keys(distances).map(function(key) {
            return {id: key, c: me.colors[distances[key]]};
        });
        me.chart.setProperties(labels);
    }
});