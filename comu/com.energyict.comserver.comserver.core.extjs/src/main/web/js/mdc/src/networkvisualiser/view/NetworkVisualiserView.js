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
    header: {
        titlePosition: 0,
        style: {
            paddingRight: '0px'
        },
        items: []
    },
    contextMenuItems: [
        {
            xtype: 'menuitem',
            text: Uni.I18n.translate('general.navigateTo.deviceLandingPage', 'MDC', 'Navigate to device landing page'),
            itemId: 'mdc-network-visualiser-landingpage-menu-item',
            section: 3, /*SECTION_VIEW*/
            handler: function (menuItem) {
                menuItem.visualiser.router.getRoute('devices/device').forwardInNewTab({
                    deviceId: menuItem.visualiser.chart.getItem(menuItem.chartNodeId).d.name
                });
            }
        },
        {
            xtype: 'menuitem',
            text: Uni.I18n.translate('general.navigateTo.communicationTopology', 'MDC', 'Navigate to communication topology'),
            itemId: 'mdc-network-visualiser-topology-menu-item',
            section: 3, /*SECTION_VIEW*/
            handler: function (menuItem) {
                menuItem.visualiser.router.getRoute('devices/device/topology').forwardInNewTab({
                    deviceId: menuItem.visualiser.chart.getItem(menuItem.chartNodeId).d.name
                });
            }
        },
        {
            xtype: 'menuitem',
            text: Uni.I18n.translate('general.sendCommands', 'MDC', 'Send commands'),
            itemId: 'mdc-network-visualiser-sendcommands-menu-item',
            section: 1, /*SECTION_ACTION*/
            handler: function (menuItem) {
                menuItem.visualiser.router.getRoute('devices/device/commands/add').forwardInNewTab({
                    deviceId: menuItem.visualiser.chart.getItem(menuItem.chartNodeId).d.name
                });
            }
        },
        {
            xtype: 'menuitem',
            text: Uni.I18n.translate('general.retriggerCommTasks', 'MDC', 'Retrigger communication tasks'),
            section: 1, /*SECTION_ACTION*/
            itemId: 'mdc-network-visualiser-retrigger-menu-item',
            handler: function (menuItem) {
                menuItem.visualiser.fireEvent('showretriggerwindow', menuItem.visualiser.chart.getItem(menuItem.chartNodeId).d.name);
            }
        }
    ],
    propertyViewerTitle: Uni.I18n.translate('general.deviceSummary', 'MDC', 'Device summary'),
    geoLocationlayer: '',
    geoLocationZoom: '',
    geoLocationType: '',


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

    showLinkQuality: function () {
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

        me.forEachLink(function (link) {
            return {
                id: link.id,
                w: link.d.linkQuality ? 3 : 2,
                c: link.d.linkQuality ? (link.d.linkQuality <= 51 ? me.badLinkQualityColor : me.goodLinkQualityColor) : 'grey'
                // w: counter % 6 === 0 ? 2 : (counter%6===1 || counter%6===5) ? 5 : 3,
                // c: linkColors[counter++ % 6]
            };
        });

        var icon = '<span class="' + me.linkIcon + '" style="display:inline-block; font-size:16px; color:' + me.badLinkQualityColor + '"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.bad', 'MDC', 'Link quality <= 51'));
        icon = '<span class="' + me.linkIcon + '" style="display:inline-block; font-size:16px; color:' + me.goodLinkQualityColor + '"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.good', 'MDC', 'Link quality > 51'));
        icon = '<span class="' + me.linkIcon + '" style="display:inline-block; font-size:16px; color:grey"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.undefined', 'MDC', 'Link quality undefined'));
        // icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+linkColors[1]+'"></span>';
        // me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.less.64', 'MDC', '0 < Link quality <= 64'));
        // icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+linkColors[2]+'"></span>';
        // me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.less.128', 'MDC', '64 < Link quality <= 128'));
        // icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+linkColors[3]+'"></span>';
        // me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.less.192', 'MDC', '128 < Link quality <= 192'));
        // icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+linkColors[4]+'"></span>';
        // me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.less.255', 'MDC', '192 < Link quality < 255'));
        // icon = '<span class="'+me.linkIcon+'" style="display:inline-block; font-size:16px; color:'+linkColors[5]+'"></span>';
        // me.addLegendItem(icon, Uni.I18n.translate('legend.link.quality.255', 'MDC', 'Link quality = 255'));
    },

    showDeviceType: function () {
        var me = this,
            deviceTypeColors = {},
            icon,
            colorIndex = 0,
            optionsMenu = Ext.ComponentQuery.query('#uni-visualiser-menu')[0].down('#mdc-visualiser-layer-maps').checked;

        me.forEachNode(function (node) {
            if (node.d && !Ext.isEmpty(node.d.gateway) && node.d.gateway) {
                icon = KeyLines.getFontIcon(me.gatewayIcon);
            } else {
                icon = KeyLines.getFontIcon(me.deviceIcon);
            }

            if (Ext.isEmpty(deviceTypeColors[node.d.deviceType])) {
                deviceTypeColors[node.d.deviceType] = me.colors[colorIndex++];
            }

            if (optionsMenu) {
                return {
                    id: node.id,
                    b: null,
                    fi: {
                        c: node.d.hasCoordonates ? deviceTypeColors[node.d.deviceType] : '#a19fa3',
                        t: icon
                    }
                }
            }
            else {
                return {
                    id: node.id,
                    b: null,
                    fi: {
                        c: deviceTypeColors[node.d.deviceType],
                        t: icon
                    }
                }
            }
        });

        for (var type in deviceTypeColors) {
            icon = '<span class="' + me.deviceIcon + '" style="display:inline-block; font-size:16px; color:' + deviceTypeColors[type] + '"></span>';
            me.addLegendItem(icon, type);
        }
    },

    showIssuesAndAlarms: function () {
        var me = this,
            glyphs;

        me.forEachNode(function (node) {
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

        var icon = '<span class="' + me.issueAlarmIcon + '" style="display:inline-block; margin-bottom: 22px; font-size:16px; color:' + me.issueAlarmColor + '"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.alarms', 'MDC', '# Alarms'));
        icon = '</sub><span class="' + me.issueAlarmIcon + '" style="display:inline-block; margin-top: 7px; font-size:16px; color:' + me.issueAlarmColor + '"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.issues', 'MDC', '# Issues'));
    },

    showHopLevel: function () {
        var me = this,
            hopLevelsByNodeIds = me.chart.graph().distances(this.top),
            hopLevel,
            hopLevelColors = {},
            icon;

        // Determine the hopLevelColors
        Ext.Array.each(Object.keys(hopLevelsByNodeIds), function (nodeId) {
            hopLevel = hopLevelsByNodeIds[nodeId];
            if (Ext.isEmpty(hopLevelColors[hopLevel])) {
                hopLevelColors[hopLevel] = me.colors[hopLevel];
            }
        });

        me.forEachNode(function (node) {
            if (node.d && !Ext.isEmpty(node.d.gateway) && node.d.gateway) {
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
            icon = '<span class="' + me.deviceIcon + '" style="display:inline-block; font-size:16px; color:' + hopLevelColors[hopLevel] + '"></span>';
            me.addLegendItem(icon, Uni.I18n.translatePlural('legend.hop.level', Number(hopLevel), 'MDC', 'No hops', '{0} hop', '{0} hops'));
        }
    },

    hideMap: function () {
        var me = this;
        me.chart.map().hide();
    },

    showMap: function () {
        var me = this;
        me.chart.map().show();
        me.setMapProvider();
    },

    setMapProvider: function () {
        var me = this;
        leafletMap = me.chart.map().leafletMap();
        L.tileLayer(geoLocationlayer, {
            maxZoom: geoLocationZoom
        }).addTo(leafletMap);
    },

    showDeviceLifeCycleStatus: function () {
        var me = this,
            deviceTypeColors = {},
            icon,
            colorIndex = 0;

        me.forEachNode(function (node) {
            if (node.d && !Ext.isEmpty(node.d.gateway) && node.d.gateway) {
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
            icon = '<span class="' + me.deviceIcon + '" style="display:inline-block; font-size:16px; color:' + deviceTypeColors[type] + '"></span>';
            me.addLegendItem(icon, type);
        }
    },

    showCommunicationStatus: function () {
        var me = this;

        me.forEachNode(function (node) {
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

        var icon = '<span class="' + me.failedCommunicationIcon + '" style="display:inline-block; font-size:16px; color:' + me.failedCommunicationStatusColor + '"></span>';
        me.addLegendItem(icon, Uni.I18n.translate('legend.failingComTasks', 'MDC', 'Failed communication tasks'));
    },

    displayNodeProperties: function (id) {
        if (this.chart.combo().isCombo(id)) {
            return;
        }
        var me = this,
            graphData = me.chart.getItem(id).d,
            downStream = me.getDownStreamNodesLinks(id),
            shortestPaths = me.chart.graph().shortestPaths(me.top[0], id, {direction: 'any'}),
            parentIndex = shortestPaths.one.length - 2;

        graphData.parent = parentIndex < 0 ? '-' : this.chart.getItem(shortestPaths.one[parentIndex]).d.name;
        graphData.hopLevel = shortestPaths.one.length - 1;
        graphData.descendants = downStream.nodes.length;
        me.fireEvent('showdevicesummary', graphData);
    },

    onRefresh: function (buttonPressed) {
        var visualiserPanel = buttonPressed.up('visualiserpanel'),
            layersToQuery = [],
            layerName = undefined,
            getMatchingLayerName = function (methodName) {
                switch (methodName) {
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
                    case 'showMap':
                        return 'topology.GraphLayer.DeviceGeoCoordinatesLayer';
                    default:
                        return undefined;
                }
            };

        Ext.each(visualiserPanel.activeLayers, function (layerFunction) {
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
        this.showMap();
    },

    preprocessSingleSelectionMenuItemsBeforeShowing: function (menu, doShowMenuFunction) {
        var menuWillBeShown = false;
        menu.items.each(function (menuItem) {
            if (menuItem.xtype !== 'menuseparator') {
                var chartItem = menuItem.visualiser.chart.getItem(menuItem.chartNodeId),
                    collapsedNode = menuItem.visualiser.chart.combo().isCombo(menuItem.chartNodeId);

                if (menuItem.itemId) {
                    if (menuItem.itemId === 'uni-visualiser-single-selection-highlight-downstream-menu-item' ||
                        menuItem.itemId === 'mdc-network-visualiser-sendcommands-menu-item' ||
                        menuItem.itemId === 'mdc-network-visualiser-landingpage-menu-item') {
                        menuItem.hidden = collapsedNode;
                    } else if (menuItem.itemId === 'mdc-network-visualiser-topology-menu-item') {
                        // Show the "Navigate to communication topology" menu item only for the gateway:
                        menuItem.hidden = !(chartItem.d && !Ext.isEmpty(chartItem.d.gateway) && chartItem.d.gateway);
                    } else if (menuItem.itemId === 'mdc-network-visualiser-retrigger-menu-item') {
                        // Disable the "Retrigger communication tasks" menu item when there are no communication tasks that can be triggered:
                        menuItem.disabled = true;
                        menuItem.tooltip = Uni.I18n.translate('general.retriggerCommTasks.disabledMenu.tooltip', 'MDC', 'No communication tasks to trigger on this device');
                        if (collapsedNode) {
                            menuItem.hidden = true;
                        } else {
                            var communicationTasksOfDeviceStore = Ext.getStore('Mdc.store.CommunicationTasksOfDevice');
                            communicationTasksOfDeviceStore.getProxy().setExtraParam('deviceId', chartItem.d.name);
                            menuWillBeShown = true;
                            communicationTasksOfDeviceStore.load({
                                callback: function (records) {
                                    Ext.Array.each(records, function (comTaskRecord) {
                                        var connectionDefinedOnDevice = comTaskRecord.get('connectionDefinedOnDevice'),
                                            isOnHold = comTaskRecord.get('isOnHold'),
                                            isSystemComtask = comTaskRecord.get('comTask').isSystemComTask;
                                        if (connectionDefinedOnDevice && !isOnHold && !isSystemComtask) { // at least one convenient task is found, so:
                                            menuItem.disabled = false;
                                            menuItem.tooltip = null;
                                            return false;
                                        }
                                    });
                                    if (Ext.isFunction(doShowMenuFunction)) {
                                        doShowMenuFunction();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
        // Fallback (to be sure the menu is called):
        if (!menuWillBeShown && Ext.isFunction(doShowMenuFunction)) {
            doShowMenuFunction();
        }
    }

});