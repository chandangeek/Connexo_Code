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
        var me = this,
            linkColors = ['#FF0000','#CC3300','#996600','#669900','#33CC00'];

        me.forEachLink(function(link){
            return {
                id: link.id,
                w: link.d.linkQuality*2,
                c: linkColors[link.d.linkQuality-1],
                t: link.d.linkQuality
            };
        })
    },

    showDeviceType: function(){
        var me = this,
            deviceTypeColors = {},
            icon,
            colorIndex = 0;

        me.forEachNode(function(node){
            if(node.d && !Ext.isEmpty(node.d.gateway) && node.d.gateway){
                icon = KeyLines.getFontIcon('icon-diamond4');
            } else {
                icon = KeyLines.getFontIcon('icon-circle2');
            }
            if (Ext.isEmpty(deviceTypeColors[node.d.type])) {
                deviceTypeColors[node.d.type] = me.colors[colorIndex++];
            }
            return {
                        id: node.id,
                        b: null,
                        fi: {
                            //c: '#8e8e8e',
                            c: deviceTypeColors[node.d.type],
                            t: icon
                        }
            }
        });

        for (type in deviceTypeColors) {
            icon = '<span class="'+me.deviceIcon+'" style="display:inline-block; font-size:16px; color:'+deviceTypeColors[type]+'"></span>';
            me.addLegendItem(icon, type);
        }
    },

    showAlarms: function(){
        var me = this,
            glyphs;

        me.forEachNode(function(node){
            if (node.d.alarms || node.d.issues) {
                if (node.d.alarms && node.d.issues) {
                    glyphs = [
                        {
                            c: me.issueAlarmColor,
                            p: 'ne',
                            t: node.d.alarms
                        },
                        {
                            c: me.issueAlarmColor,
                            p: 'se',
                            t: node.d.issues
                        }
                    ];
                } else if (node.d.alarms) {
                    glyphs = [
                        {
                            c: me.issueAlarmColor,
                            p: 'ne',
                            t: node.d.alarms
                        }
                    ];
                } else {
                    glyphs = [
                        {
                            c: me.issueAlarmColor,
                            p: 'se',
                            t: node.d.issues
                        }
                    ];
                }
                return {
                    id: node.id,
                    g: glyphs
                    // No halo for the Issues/Alarms layer
                    //ha0: {
                    //    c: me.issueAlarmColor,
                    //    r: 50,
                    //    w: 3
                    //}
                };
            }
        });
    },

    showHopLevel: function(){
        var me = this,
            graphDistances = me.chart.graph().distances(this.top),
            hopColors = {},
            hopLevel,
            labels = Object.keys(graphDistances).map(function(key) {
                hopLevel = graphDistances[key];
                if (Ext.isEmpty(hopColors[hopLevel])) {
                    hopColors[hopLevel] = me.colors[hopLevel];
                }
                return {
                    id: key,
                    c: me.colors[hopLevel]
                };
            }),
            icon;

        me.chart.setProperties(labels);

        for (hopLevel in hopColors) {
            icon = '<span class="'+ me.hopLevelIcon +'" style="display:inline-block; font-size:20px; color:'+hopColors[hopLevel]+'"></span>';
            me.addLegendItem(icon, hopLevel);
        }
    }
});