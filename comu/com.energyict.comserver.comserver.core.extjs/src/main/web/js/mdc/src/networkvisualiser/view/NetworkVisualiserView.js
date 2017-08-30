Ext.define('Mdc.networkvisualiser.view.NetworkVisualiserView', {
    extend: 'Uni.graphvisualiser.VisualiserPanel',
    alias: 'widget.visualiserpanel',
    requires: [
        'Mdc.networkvisualiser.view.NetworkVisualiserMenu'
    ],
    itemId: 'visualiserpanel',
    title: Uni.I18n.translate('general.networkVisualizer', 'MDC', 'Network visualizer'),
    ui: 'large',
    menu: 'Mdc.networkvisualiser.view.NetworkVisualiserMenu',
    header:{
        titlePosition: 0,
        style: {
            paddingRight: '0px'
        },
        items:[]
    },
    contextMenuItems: [
        {
            xtype: 'menuitem',
            text: Uni.I18n.translate('general.navigateTo.deviceLandingPage', 'MDC', 'Navigate to device landing page'),
            section: 3, /*SECTION_VIEW*/
            handler: function(menuItem) {
                menuItem.visualiser.router.getRoute('devices/device').forwardInNewTab({
                    deviceId: menuItem.visualiser.chart.getItem(menuItem.graphId).d.name
                });
            }
        },
        {
            xtype: 'menuitem',
            text: Uni.I18n.translate('general.navigateTo.communicationTopology', 'MDC', 'Navigate to communication topology'),
            gatewayOnly: true, /*only show for gateways*/
            section: 3, /*SECTION_VIEW*/
            handler: function(menuItem) {
                menuItem.visualiser.router.getRoute('devices/device/topology').forwardInNewTab({
                    deviceId: menuItem.visualiser.chart.getItem(menuItem.graphId).d.name
                });
            }
        },
        {
            xtype: 'menuitem',
            text: Uni.I18n.translate('general.sendCommands', 'MDC', 'Send commands'),
            section: 1, /*SECTION_ACTION*/
            handler: function(menuItem) {
                menuItem.visualiser.router.getRoute('devices/device/commands/add').forwardInNewTab({
                    deviceId: menuItem.visualiser.chart.getItem(menuItem.graphId).d.name
                });
            }
        },
        {
            xtype: 'menuitem',
            text: Uni.I18n.translate('general.retriggerCommTasks', 'MDC', 'Retrigger communication tasks'),
            section: 1, /*SECTION_ACTION*/
            handler: function(menuItem) {
                menuItem.visualiser.fireEvent('showretriggerwindow', menuItem.visualiser.chart.getItem(menuItem.graphId).d.name);
            }
        }
    ],
    propertyViewerTitle: Uni.I18n.translate('general.deviceSummary', 'MDC', 'Device summary'),

    initComponent: function () {
        if (this.getHeader().items.length === 0) {
            this.getHeader().items.push(
                {
                    xtype: 'button',
                    iconCls: 'icon-spinner11',
                    text: Uni.I18n.translate('general.refresh', 'MDC', 'Refresh'),
                    handler: this.onRefresh
                }
            );
        }
        this.callParent();
    },

    showLinkQuality: function(){
        var me = this;
            // Commented code = temporary code to be able to play with (more) colors and thickness
            // linkColors = [
            //     'grey',
            //     '#FF0000',
            //     '#FF8000',
            //     '#FFFF00',
            //     '#80FF00',
            //     '#00FF00'
            // ],
            // linkColors = [
            //     'grey',
            //     '#C00000',
            //     '#C06000',
            //     '#C0C000',
            //     '#60C000',
            //     '#00C000'
            // ],
            // counter = 0;

        me.forEachLink(function(link){
            return {
                id: link.id,
                w: link.d.linkQuality ? 3 : 2,
                c: link.d.linkQuality ? (link.d.linkQuality <= 51 ? me.badLinkQualityColor : me.goodLinkQualityColor) : 'grey'
                // w: counter % 6 === 0 ? 2 : (counter%6===1 || counter%6===5) ? 5 : 3,
                // c: linkColors[counter++ % 6]
            };
        });

        var icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+me.badLinkQualityColor+'"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.bad', 'MDC', 'Link quality <= 51'));
        icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+me.goodLinkQualityColor+'"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.good', 'MDC', 'Link quality > 51'));
        icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:grey"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.undefined', 'MDC', 'Link quality undefined'));
        // icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+linkColors[1]+'"></span>';
        // me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.undefined', 'MDC', '0 < Link quality <= 64'));
        // icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+linkColors[2]+'"></span>';
        // me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.undefined', 'MDC', '64 < Link quality <= 128'));
        // icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+linkColors[3]+'"></span>';
        // me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.undefined', 'MDC', '128 < Link quality <= 192'));
        // icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+linkColors[4]+'"></span>';
        // me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.undefined', 'MDC', '192 < Link quality < 255'));
        // icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+linkColors[5]+'"></span>';
        // me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.undefined', 'MDC', 'Link quality = 255'));
    },

    showDeviceType: function(){
        var me = this,
            deviceTypeColors = {},
            icon,
            colorIndex = 0;

        me.forEachNode(function(node){
            if(node.d && !Ext.isEmpty(node.d.gateway) && node.d.gateway){
                icon = KeyLines.getFontIcon(me.gatewayIcon);
            } else {
                icon = KeyLines.getFontIcon(me.deviceIcon);
            }
            if (Ext.isEmpty(deviceTypeColors[node.d.deviceType])) {
                deviceTypeColors[node.d.deviceType] = me.colors[colorIndex++];
            }
            return {
                id: node.id,
                b: null,
                fi: {
                    c: deviceTypeColors[node.d.deviceType],
                    t: icon
                }
            }
        });

        for (var type in deviceTypeColors) {
            icon = '<span class="'+me.deviceIcon+'" style="display:inline-block; font-size:16px; color:'+deviceTypeColors[type]+'"></span>';
            me.addLegendItem(icon, type);
        }
    },

    showIssuesAndAlarms: function(){
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
                };
            }
        });

        var icon = '<span class="'+me.issueAlarmIcon+'" style="display:inline-block; margin-bottom: 22px; font-size:16px; color:'+me.issueAlarmColor+'"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.alarms', 'MDC', '# Alarms'));
        icon = '</sub><span class="'+me.issueAlarmIcon+'" style="display:inline-block; margin-top: 7px; font-size:16px; color:'+me.issueAlarmColor+'"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.issues', 'MDC', '# Issues'));
    },

    showHopLevel: function(){
        var me = this,
            hopLevelsByNodeIds = me.chart.graph().distances(this.top),
            hopLevel,
            hopLevelColors = {},
            icon;

        // Determine the hopLevelColors
        Ext.Array.each(Object.keys(hopLevelsByNodeIds), function(nodeId) {
            hopLevel = hopLevelsByNodeIds[nodeId];
            if (Ext.isEmpty(hopLevelColors[hopLevel])) {
                hopLevelColors[hopLevel] = me.colors[hopLevel];
            }
        });

        me.forEachNode(function(node){
            if(node.d && !Ext.isEmpty(node.d.gateway) && node.d.gateway){
                icon = KeyLines.getFontIcon(me.gatewayIcon);
            } else {
                icon = KeyLines.getFontIcon(me.deviceIcon);
            }
            return {
                id: node.id,
                b: null,
                fi: {
                    c: hopLevelColors[hopLevelsByNodeIds[node.id]],
                    t: icon
                }
            }
        });

        for (hopLevel in hopLevelColors) {
            icon = '<span class="'+me.deviceIcon+'" style="display:inline-block; font-size:16px; color:'+hopLevelColors[hopLevel]+'"></span>';
            me.addLegendItem(icon, Uni.I18n.translatePlural('legend.hop.level', Number(hopLevel), 'MDC', 'No hops', '{0} hop', '{0} hops'));
        }
    },

    showDeviceLifeCycleStatus: function(){
        var me = this,
            deviceTypeColors = {},
            icon,
            colorIndex = 0;

        me.forEachNode(function(node){
            if(node.d && !Ext.isEmpty(node.d.gateway) && node.d.gateway){
                icon = KeyLines.getFontIcon(me.gatewayIcon);
            } else {
                icon = KeyLines.getFontIcon(me.deviceIcon);
            }
            if (Ext.isEmpty(deviceTypeColors[node.d.deviceLifecycleStatus])) {
                deviceTypeColors[node.d.deviceLifecycleStatus] = me.colors[colorIndex++];
            }
            return {
                id: node.id,
                b: null,
                fi: {
                    c: deviceTypeColors[node.d.deviceLifecycleStatus],
                    t: icon
                }
            }
        });

        for (var type in deviceTypeColors) {
            icon = '<span class="'+me.deviceIcon+'" style="display:inline-block; font-size:16px; color:'+deviceTypeColors[type]+'"></span>';
            me.addLegendItem(icon, type);
        }
    },

    showCommunicationStatus: function() {
        var me = this;

        me.forEachNode(function(node){
            if (node.d.failedCommunications > 0) {
                return {
                    id: node.id,
                    ha0: {
                        c: me.failedCommunicationStatusColor,
                        r: 34,
                        w: 3
                    }
                };
            }
        });

        var icon = '<span class="'+me.failedCommunicationIcon+'" style="display:inline-block; font-size:16px; color:'+me.failedCommunicationStatusColor+'"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.failingComTasks', 'MDC', 'Failed communication tasks'));
    },

    displayNodeProperties: function(id){
        var me = this,
            graphData = this.chart.getItem(id).d,
            downStream = me.getDownStreamNodesLinks(id),
            shortestPaths = me.chart.graph().shortestPaths(me.top[0], id, {direction: 'any'}),
            parentIndex = shortestPaths.one.length-2;

        graphData.parent = parentIndex<0 ? '-' : this.chart.getItem(shortestPaths.one[parentIndex]).d.name;
        graphData.hopLevel = shortestPaths.one.length-1;
        graphData.descendants = downStream.nodes.length;
        me.fireEvent('showdevicesummary', graphData);
    },

    onRefresh: function(buttonPressed) {
        var visualiserPanel = buttonPressed.up('visualiserpanel'),
            layersToQuery = [],
            layerName = undefined,
            getMatchingLayerName = function(methodName) {
                switch(methodName) {
                    case 'showDeviceType':
                        return 'topology.GraphLayer.DeviceType';
                    case 'showLinkQuality':
                        return 'topology.GraphLayer.linkQuality';
                    case 'showIssuesAndAlarms':
                        return 'topology.GraphLayer.IssuesAndAlarms';
                    case 'showDeviceLifeCycleStatus':
                        return 'topology.GraphLayer.DeviceLifeCycleStatus';
                    case 'showCommunicationStatus':
                        return 'topology.GraphLayer.CommunicationStatus';
                    default:
                        return undefined;
                }
            };

        Ext.each(visualiserPanel.activeLayers, function(layerFunction) {
            layerName = getMatchingLayerName(layerFunction.name);
            if (!Ext.isEmpty(layerName)) {
                layersToQuery.push(layerName);
            }
        });
        visualiserPanel.store.getProxy().setExtraParam('filter', Ext.encode([
            {
                property: 'layers',
                value: layersToQuery
            },
            {
                property: 'refresh',
                value: true
            }
        ]));
        visualiserPanel.refreshGraph();
    }


});