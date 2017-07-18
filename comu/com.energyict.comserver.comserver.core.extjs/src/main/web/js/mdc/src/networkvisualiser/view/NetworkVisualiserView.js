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
            xtype: 'menuitem',
            text: 'Navigate to device landing page',
            section: 3, /*SECTION_VIEW*/
            handler: function(menuItem) {
                menuItem.visualiser.router.getRoute('devices/device').forward({
                    deviceId: menuItem.visualiser.chart.getItem(menuItem.graphId).d.name
                });
            }
        },
        {
            xtype: 'menuitem',
            text: 'Navigate to communication topology',
            section: 3, /*SECTION_VIEW*/
            handler: function(menuItem) {
                menuItem.visualiser.router.getRoute('devices/device/topology').forward({
                    deviceId: menuItem.visualiser.chart.getItem(menuItem.graphId).d.name
                });
            }
        }
    ],
    propertyViewerTitle: Uni.I18n.translate('general.deviceSummary', 'MDC', 'Device summary'),

    initComponent: function () {
        this.callParent();
    },

    showLinkQuality: function(){
        var me = this;

        me.forEachLink(function(link){
            return {
                id: link.id,
                w: 3,
                c: link.d.linkQuality <= 51 ? me.badLinkQualityColor : me.goodLinkQualityColor
            };
        });

        var icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+me.badLinkQualityColor+'"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.bad', 'MDC', 'Link quality <= 51'));
        icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+me.goodLinkQualityColor+'"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.good', 'MDC', 'Link quality > 51'));
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

    showCommunicationStatus: function() {
        var me = this;

        me.forEachNode(function(node){
            if (!Ext.isEmpty(node.d.failedComTasks)) {
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
            propertiesToDisplay = {},
            downStream = me.getDownStreamNodesLinks(id),
            shortestPaths = me.chart.graph().shortestPaths(me.top[0], id, {direction: 'any'}),
            parentIndex = shortestPaths.one.length-2,
            orderOfProperties = [
                'name',
                'serialNumber',
                'deviceType',
                'deviceConfiguration',
                'parent',
                'hopLevel',
                'descendants',
                'alarms',
                'issues',
                'failedComTasks'
            ],
            fieldLabels = [
                Uni.I18n.translate('general.name', 'MDC', 'Name'),
                Uni.I18n.translate('general.serialNumber', 'MDC', 'Serial number'),
                Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                Uni.I18n.translate('general.parent', 'MDC', 'Parent'),
                Uni.I18n.translate('general.numberOfHops', 'MDC', 'Number of hops'),
                Uni.I18n.translate('general.totalDescendants', 'MDC', 'Total descendants'),
                Uni.I18n.translate('general.alarms', 'MDC', 'Alarms'),
                Uni.I18n.translate('general.issues', 'MDC', 'Issues'),
                Uni.I18n.translate('general.failedCommunicationTasks', 'MDC', 'Failed communication tasks')
            ];

        // 1. Prepare the properties to display (in the right format)
        graphData.parent = parentIndex<0 ? '-' : this.chart.getItem(shortestPaths.one[parentIndex]).d.name;
        graphData.hopLevel = shortestPaths.one.length-1;
        graphData.descendants = downStream.nodes.length;

        for (var property in graphData) {
            if (graphData.hasOwnProperty(property)) {
                var propertyIndex = orderOfProperties.indexOf(property),
                    fieldLabel = propertyIndex<0 ? undefined : fieldLabels[propertyIndex],
                    htmlEncode = true,
                    graphDataPropertyValue = graphData[property];

                switch(property) {
                    case 'alarms':
                    case 'issues':
                        if (graphDataPropertyValue === 0) {
                            graphDataPropertyValue = undefined; // Display a zero value as a dash
                        }
                        break;
                    case 'failedComTasks':
                        graphDataPropertyValue = undefined;
                        if (Ext.isArray(graphDataPropertyValue)) {
                            if (graphDataPropertyValue.length > 1) {
                                var formattedResult = '';
                                Ext.Array.each(graphDataPropertyValue, function (value) {
                                    formattedResult += (value + '</br>');
                                });
                                graphDataPropertyValue = formattedResult;
                                htmlEncode = false;
                            } else if (graphDataPropertyValue.length === 1) {
                                graphDataPropertyValue = graphDataPropertyValue[0];
                            }
                        }
                        break;
                    default:
                        break;
                }
                if (!Ext.isEmpty(fieldLabel)) {
                    propertiesToDisplay[fieldLabel] = {
                        value: graphDataPropertyValue,
                        htmlEncode: htmlEncode,
                        order: propertyIndex
                    };
                }
            }
        }

        // 2. Display them
        Ext.ComponentQuery.query('#uni-property-viewer')[0].displayProperties(propertiesToDisplay);
    },

    showNotYetImplementedMessage: function() {
        var box = Ext.create('Ext.window.MessageBox', {
                buttons: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.close', 'MDC', 'Close'),
                        action: 'close',
                        name: 'close',
                        ui: 'remove',
                        handler: function () {
                            box.close();
                        }
                    }
                ]
            }),
            config = {};

        Ext.apply(config, {
            title: 'To be implemented',
            msg: 'Not yet implemented',
            modal: false,
            ui: 'message-error',
            icon: 'icon-warning2',
            style: 'font-size: 34px;'
        });

        box.show(config);
    }

});