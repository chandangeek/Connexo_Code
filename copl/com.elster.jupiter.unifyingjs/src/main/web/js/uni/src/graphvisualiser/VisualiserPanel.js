Ext.define('Uni.graphvisualiser.VisualiserPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uniVisualiserPanel',
    itemId: 'VisualiserPanel',
    requires: [
        'Uni.graphvisualiser.VisualiserMenu',
        'Uni.graphvisualiser.VisualiserPropertyViewer',
        'Uni.graphvisualiser.VisualiserLegendFloat',
        'Uni.view.menu.ActionsMenu'
    ],
    layout: {
        type: 'border'
    },
    items: [
        {
            xtype: 'container',
            html: "<div id='graph-drawing-area' style='top: 0; bottom: 0; left: 0; right: 0; position: absolute;'></div>",
            region: 'center'
        }
    ],
    padding: 10,
    deviceId2Select: null, // int
    router: null,
    graphLayout: 'standard',
    chartData: {
        type: 'LinkChart',
        items: []
    },
    activeLayers: [],
    propertyViewerTitle: Uni.I18n.translate('general.propertyViewer', 'UNI', 'Property viewer'),
    yOffset: 0,
    nodeStoreForComboBox: undefined,
    showGatewayLegend: undefined,
    showDeviceLegend: undefined,
    freshNodes: [],

    gatewayIcon: 'icon-diamond4',
    deviceIcon: 'icon-circle2',
    linkIcon: 'icon-arrow-down-right2',
    issueAlarmIcon: 'icon-circle-small',
    failedCommunicationIcon: 'icon-circle',
    noCoordDevice: 'icon-ruler',

    neutralColor: '#006699',
    whiteColor: '#FFFFFF',
    blackColor: '#000000',
    collapsedColor: '#8e8e8e',
    issueAlarmColor: '#FF0000',
    failedCommunicationStatusColor: '#FF0000',
    goodLinkQualityColor: '#70BB51',
    badLinkQualityColor: '#EB5642',
    noCoordinatesColor: '#a19fa3',
    colors: [
        '#BEE64B', '#33CC99', '#00CCCC', '#7ED4E6', '#2887C8', '#C3CDE6', '#7070CC', '#C9A0DC', '#733380', '#2D383A',
        '#5E8C31', '#7BA05B', '#4D8C57', '#3AA655', '#93DFB8', '#1AB385', '#29AB87', '#00CC99', '#00755E',
        '#8DD9CC', '#01786F', '#30BFBF', '#008080', '#8FD8D8',
        '#95E0E8', '#6CDAE7', '#76D7EA', '#0095B7', '#009DC4', '#02A4D3', '#47ABCC', '#4997D0', '#339ACC',
        '#93CCEA', '#00468C', '#0066CC', '#1560BD', '#0066FF', '#A9B2C3', '#4570E6', '#7A89B8', '#4F69C6',
        '#8D90A1', '#8C90C8', '#9999CC', '#ACACE6', '#766EC8', '#6456B7', '#3F26BF', '#8B72BE', '#652DC1', '#6B3FA0',
        '#8359A3', '#8F47B3', '#BF8FCC', '#803790', '#D6AEDD', '#C154C1', '#FC74FD', '#C5E17A', '#9DE093', '#63B76C', '#6CA67C', '#5FA777'
    ],

    listeners: {
        boxready: function (panel) {
            this.initCanvas(panel);
            // this.initShit();
        },
        resize: function(panel,w,h){
            KeyLines.setSize('graph-drawing-area', w-10, h-this.yOffset);
            if(this.sideMenu && this.propertyViewer){
                this.sideMenu.alignTo(Ext.get('graph-drawing-area'), 'tl-tl', [-5, -5]);
                this.propertyViewer.alignTo(Ext.get('graph-drawing-area'), 'tr-tr', [-5, -5]);
            }
            if (this.legendPanel) {
                this.legendPanel.show().alignTo(Ext.get('graph-drawing-area'), 'bl-bl', [-5, -15]);
            }
            if(this.chart){
                this.doLayout();
            }
        },
        beforedestroy: function(){
            if (this.sideMenu) {
                this.sideMenu.destroy();
            }
            if (this.propertyViewer) {
                this.propertyViewer.destroy();
            }
            if (this.legendPanel) {
                this.legendPanel.destroy();
            }
        }
    },

    initComponent: function() {
        this.activeLayers = [];
        this.freshNodes = [];
        this.callParent(arguments);
    },

    initShit: function () {
        var mapopts = {
            zoom: 1
        };
        var map = L.map('graph-drawing-area', mapopts).setView([45.828315, 21.2800133], 15);

        var roadMutant = L.gridLayer.googleMutant({
            maxZoom: 24,
            type: 'roadmap'
        }).addTo(map);
    },


    initCanvas: function (me) {
        var me = this;
        KeyLines.paths({assets: 'resources/js/keylines/assets/'});
        KeyLines.create(
            {
                id: 'graph-drawing-area',
                options: {

                    navigation: {
                        p: 'se'
                    },
                    iconFontFamily: 'Icomoon',
                    marqueeLinkSelection: 'off',
                    overview: {
                        icon:false
                    },
                    selectionColour: me.neutralColor
                }
            },
            function(err, chart) {
                me.chart = chart;
                me.chart.bind('click', me.highlightUpStreamFromNode, me);
                me.chart.bind('dblclick', me.combine, me);
                me.chart.bind('contextmenu', me.contextMenu, me);
                me.chart.bind('delete', function() { return true; }); // prevent deleting nodes
                me.chart.bind('dragstart', me.preventDraggingNonNodes, me);
                me.chart.load({
                    type: 'LinkChart'
                });
                me.loadData(chart);
            });


    },

    contextMenu: function(id, x, y) {
        var me = this,
            singleSelection = this.chart.selection().length === 1;
        if (singleSelection) {
            this.doShowSingleSelectionContextMenu(id, x, y);
        } else { // multiple selection
            var singleNode = undefined,
                multipleNodes = false,
                singleSelectedNodeId = undefined,
                item = undefined;
            Ext.Array.forEach(me.chart.selection(), function(selectedItem) {
                item = me.chart.getItem(selectedItem);
                if (item && item.type === 'node') {
                    if (!Ext.isDefined(singleNode)) {
                        singleNode = true;
                        singleSelectedNodeId = selectedItem;
                    } else if (singleNode) {
                        singleNode = false;
                        multipleNodes = true;
                    }
                }
            });
            if (!Ext.isDefined(singleNode)) { // No node(s) selected (only links)
                return;
            } else if (singleNode) {
                this.doShowSingleSelectionContextMenu(singleSelectedNodeId, x, y);
            } else {
                this.doShowMultiSelectionContextMenu(x, y);
            }
        }
        return false;
    },

    doShowSingleSelectionContextMenu: function(id, x, y) {
        var me = this,
            item = this.chart.getItem(id);

        if (Ext.isEmpty(item) || item.type != 'node') {
            return;
        }

        var visualiser = Ext.ComponentQuery.query('visualiserpanel')[0],
            items = [
                {
                    xtype: 'menuitem',
                    itemId: 'uni-visualiser-single-selection-highlight-upstream-menu-item',
                    text: Uni.I18n.translate('general.highlight.upstream', 'UNI', 'Highlight upstream'),
                    section: 1, /*SECTION_ACTION*/
                    chartNodeId: id,
                    visualiser: visualiser,
                    handler: function () {
                        visualiser.highlightUpStreamFromNode(id);
                    }
                },
                {
                    xtype: 'menuitem',
                    itemId: 'uni-visualiser-single-selection-highlight-downstream-menu-item',
                    text: Uni.I18n.translate('general.highlight.downstream', 'UNI', 'Highlight downstream'),
                    section: 1, /*SECTION_ACTION*/
                    chartNodeId: id,
                    visualiser: visualiser,
                    handler: function () {
                        visualiser.highlightDownStreamFromNode(id);
                    }
                }
            ];

        if (visualiser.chart.combo().isCombo(id) /*=collapsed node*/ || this.hasChildren(id)) {
            items.push(
                {
                    xtype: 'menuitem',
                    itemId: 'uni-visualiser-single-selection-expand-collapse-menu-item',
                    text: visualiser.chart.combo().isCombo(id)
                        ? Uni.I18n.translate('general.expand', 'UNI', 'Expand')
                        : Uni.I18n.translate('general.collapse', 'UNI', 'Collapse'),
                    section: 1, /*SECTION_ACTION*/
                    chartNodeId: id,
                    visualiser: visualiser,
                    handler: function () {
                        visualiser.combine(id);
                    }
                }
            );
        }
        if (this.contextMenuItems) {
            Ext.each(this.contextMenuItems, function (menuItem) {
                menuItem.chartNodeId = id;
                menuItem.visualiser = visualiser;
            });
            items = items.concat(this.contextMenuItems);
        }

        var popupMenu = Ext.create('Uni.view.menu.ActionsMenu',
                {
                    itemId: 'uni-visualiser-single-selection-context-menu',
                    items: items,
                    listeners: {
                        render: function (menu) {
                            menu.getEl().on('contextmenu', Ext.emptyFn, null, {preventDefault: true});
                        }
                    }
                }
            ),
            position = this.getPosition();

        me.preprocessSingleSelectionMenuItemsBeforeShowing(popupMenu, function() {
            popupMenu.showAt([position[0] + x, position[1] + y]);
        });
    },

    preprocessSingleSelectionMenuItemsBeforeShowing: function(menu, doShowMenuFunction) {
        // Can be overridden in 'classes' extending this one (ic. NetworkVisualiserView)
        // to eg. hide or disable certain menu items
        if (Ext.isFunction(doShowMenuFunction)) {
            doShowMenuFunction();
        }
    },

    doShowMultiSelectionContextMenu: function(x, y) {
        var me = this,
            item = undefined,
            visualiser = Ext.ComponentQuery.query('visualiserpanel')[0],
            items = [
                {
                    xtype: 'menuitem',
                    itemId: 'uni-visualiser-multi-selection-collapse-menu-item',
                    text: Uni.I18n.translate('general.collapse', 'UNI', 'Collapse'),
                    section: 1, /*SECTION_ACTION*/
                    handler: function () {
                        var selectedNodes = [],
                            currentSelection = [],
                            hopLevelsByNodeIds = me.chart.graph().distances(me.top),
                            hopLevel = undefined,
                            minHopLevel = 1000;

                        Ext.Array.forEach(me.chart.selection(), function(itemId) {
                            item = me.chart.getItem(itemId);
                            if (item && item.type === 'node'
                                && !visualiser.chart.combo().isCombo(itemId) /* the node is not already collapsed */
                                && me.hasChildren(itemId) /* if not, there's nothing to combine */ ) {
                                selectedNodes.push(itemId);
                            }
                        });
                        while (selectedNodes.length > 0) {
                            minHopLevel = 1000;
                            currentSelection = [];
                            Ext.Array.forEach(selectedNodes, function (nodeId) {
                                hopLevel = hopLevelsByNodeIds[nodeId];
                                minHopLevel = hopLevel < minHopLevel ? hopLevel : minHopLevel;
                            });
                            Ext.Array.forEach(selectedNodes, function (nodeId) {
                                hopLevel = hopLevelsByNodeIds[nodeId];
                                if (hopLevel === minHopLevel) {
                                    currentSelection.push(nodeId);
                                }
                            });

                            Ext.Array.forEach(currentSelection, function (selectedItem) {
                                if (!visualiser.chart.combo().isCombo(selectedItem)) {
                                    var result = me.getDownStreamNodesLinks(selectedItem);
                                    result.nodes.push(selectedItem);
                                    selectedNodes = Ext.Array.difference(selectedNodes, result.nodes);
                                    me.chart.combo().combine({
                                        ids: result.nodes,
                                        glyph: null,
                                        style: {
                                            c: null,
                                            fi: {
                                                c: me.collapsedColor,
                                                t: KeyLines.getFontIcon('icon-plus')
                                            }
                                        }
                                    }, null, function () {
                                        if (selectedNodes.length === 0) {
                                            me.doLayout();
                                        }
                                    });
                                }
                            });
                        }
                    }
                },
                {
                    xtype: 'menuitem',
                    itemId: 'uni-visualiser-multi-selection-expand-menu-item',
                    text: Uni.I18n.translate('general.expand', 'UNI', 'Expand'),
                    section: 1, /*SECTION_ACTION*/
                    handler: function () {
                        Ext.Array.forEach(me.chart.selection(), function(selectedItem) {
                            item = me.chart.getItem(selectedItem);
                            if (item && item.type === 'node') {
                                if (visualiser.chart.combo().isCombo(selectedItem)) {
                                    visualiser.combine(selectedItem);
                                }
                            }
                        });
                    }
                }
            ],
            popupMenu = Ext.create('Uni.view.menu.ActionsMenu',
                {
                    itemId: 'uni-visualiser-multi-selection-context-menu',
                    items: items,
                    listeners: {
                        render: function (menu) {
                            menu.getEl().on('contextmenu', Ext.emptyFn, null, {preventDefault: true});
                        }
                    }
                }
            ),
            position = this.getPosition();

        me.preprocessMultiSelectionMenuItemsBeforeShowing(popupMenu, function() {
            popupMenu.showAt([position[0] + x, position[1] + y]);
        });
    },

    preprocessMultiSelectionMenuItemsBeforeShowing: function(menu, doShowMenuFunction) {
        // Can be overridden in 'classes' extending this one to eg. hide or disable certain menu items
        if (Ext.isFunction(doShowMenuFunction)) {
            doShowMenuFunction();
        }
    },

    collapsePropertyViewer: function(){
        Ext.ComponentQuery.query('#uni-property-viewer')[0].collapse();
    },

    setSelection: function(value){
       this.chart.selection(value);
    },

    highlightUpStreamFromNode: function(id){
        var me = this,
            neighbours = {},
            areNeighboursOf = function(item) {
                return neighbours[item.id];
            };

        if(id === null) {
            me.chart.foreground(function(){ return true; }, {type: 'all'});
        } else {
            var item = me.chart.getItem(id);
            if (item && item.type === 'node') {
                var result = me.chart.graph().shortestPaths(me.top[0],id, {direction: 'any'});
                Ext.each(result.onePath, function (id){
                    neighbours[id] = true;
                });
                neighbours[id] = true;
                me.chart.foreground(areNeighboursOf, {type: 'all'});
                me.displayNodeProperties(id);
            }
        }
    },

    combine: function(id){
        var me = this;
        if(me.chart.combo().isCombo(id)){
            me.chart.combo().uncombine(id,null,function(){
                // Previously collapsed nodes may miss certain data,
                // so first re-merge the latest queried data:
                me.chart.merge(me.freshNodes, function() {
                    me.clearAllLegendItems();
                    me.setDefaultStyle();
                    Ext.each(me.activeLayers,function(layer){
                        layer.call(me);
                    });
                    me.doLayout();
                });
            });
        } else {
            var result = me.getDownStreamNodesLinks(id);
            result.nodes.push(id);
            me.chart.combo().combine({
                ids: result.nodes,
                glyph: null,
                style: {
                    c: null,
                    fi: {
                        c: me.collapsedColor,
                        t: KeyLines.getFontIcon('icon-plus')
                    }
                }

            },null,function(){
                me.doLayout();
            });
        }
    },

    highlightDownStreamFromNode: function(id){
        var me = this;
        var neighbours = {};
        function areNeighboursOf(item){
            return neighbours[item.id];
        }
        if(id === null) {
            me.chart.foreground(function(){ return true; }, {type: 'all'});
        } else {
            var item = me.chart.getItem(id);
            if (item && item.type === 'node') {
                var result = me.getDownStreamNodesLinks(id);
                Ext.each(result.nodes, function (id){
                    neighbours[id] = true;
                });
                Ext.each(result.links, function (id){
                    neighbours[id] = true;
                });
                neighbours[id] = true;
                me.chart.foreground(areNeighboursOf, {type: 'all'});
                me.displayNodeProperties(id);
            }
        }
    },

    getDownStreamNodesLinks: function(id){
        return this.chart.graph().neighbours(id, {direction: 'from', hops: 1000});
    },

    hasChildren: function(id) {
        return this.chart.graph().neighbours(id, {direction: 'from', hops: 1}).nodes.length > 0;
    },

    displayNodeProperties: function(id){
        Ext.ComponentQuery.query('#uni-property-viewer')[0].displayProperties(this.chart.getItem(id).d);
    },

    addFloatingPanels: function(){
        var me = this;
        me.sideMenu = Ext.create(me.menu, {visualiser: me});
        me.propertyViewer = Ext.create('Uni.graphvisualiser.VisualiserPropertyViewer', {
            propertyViewerTitle: me.propertyViewerTitle
        });
        me.legendPanel = Ext.create('Uni.graphvisualiser.VisualiserLegendFloat');
        me.showFloatingPanels();
    },

    hideFloatingPanels: function() {
        var me = this;
        me.sideMenu.hide();
        me.propertyViewer.hide();
        me.legendPanel.hide();
    },

    showFloatingPanels: function() {
        var me = this;
        me.sideMenu.show().alignTo(Ext.get('graph-drawing-area'), 'tl-tl', [-5, -5]);
        me.propertyViewer.show().alignTo(Ext.get('graph-drawing-area'), 'tr-tr', [-5, -5]);
        me.legendPanel.show().alignTo(Ext.get('graph-drawing-area'), 'bl-bl', [-5, -15]);
    },

    loadData: function (chart) {
        var me = this,
            performAfterTheQuery = function() {
                me.addFloatingPanels();
                me.chart.load(me.chartData, function () {
                    me.chart.layout();
                    if (me.showGatewayLegend) {
                        icon = '<span class="' + me.gatewayIcon + '" style="display:inline-block; font-size:16px; color:' + me.neutralColor + '"></span>';
                        me.addLegendItem(icon, Uni.I18n.translate('general.gateway', 'UNI', 'Gateway'));
                    }
                    if (me.showDeviceLegend) {
                        icon = '<span class="' + me.deviceIcon + '" style="display:inline-block; font-size:16px; color:' + me.neutralColor + '"></span>';
                        me.addLegendItem(icon, Uni.I18n.translate('general.device', 'UNI', 'Device'));

                    }

                });
                me.sideMenu.down('combobox').bindStore(me.nodeStoreForComboBox);
                if (!Ext.isEmpty(me.deviceId2Select)) {
                    me.setSelection(me.deviceId2Select+'');
                    me.highlightUpStreamFromNode(me.deviceId2Select+'');
                }
            };

        me.queryChartData(performAfterTheQuery);
    },

    queryChartData: function(callback, dataArray2Store, getLinksToo) {
        var me = this,
            pageMainContent = Ext.ComponentQuery.query('viewport')[0],
            doGetLinksToo = Ext.isDefined(getLinksToo) ? getLinksToo : true;

        pageMainContent.setLoading(true);
        if (dataArray2Store === undefined) {
            dataArray2Store = me.chartData.items;
        }

        me.store.load(function() {
            var nodes = me.store.data.items[0].nodes(),
                links = me.store.data.items[0].links(),
                icon;

            me.showGatewayLegend = false;
            me.showDeviceLegend = false;
            me.nodeStoreForComboBox = new Ext.data.SimpleStore({ fields: ['id', 'name'] });
            nodes.each(function (node) {
                me.nodeStoreForComboBox.add({
                    id: node.get('id'),
                    name: node.get('name')
                });
                if (!Ext.isEmpty(node.get('gateway')) && node.get('gateway')) {
                    me.top = [node.get('id') + '']; // Assumption: there is only ONE gateway node (that's Keyline's top node)
                    icon = KeyLines.getFontIcon(me.gatewayIcon);
                    me.showGatewayLegend = true;
                    if (me.deviceId2Select === node.get('id')) {
                        me.deviceId2Select = undefined; // don't (pre)select the gateway
                    }
                } else {
                    icon = KeyLines.getFontIcon(me.deviceIcon);
                    me.showDeviceLegend = true;
                }
                dataArray2Store.push(
                    {
                        id: node.get('id'),
                        type: 'node',
                        b: null, // no border (color)
                        c: me.whiteColor, // fill color
                        // t: node.get('name'), // No name/label in the graph
                        e: 0.5,
                        fi: {
                            c: (!Ext.isEmpty(node.get('hasCoordonates')) && !node.get('hasCoordonates')) ? me.noCoordinatesColor : me.neutralColor,
                            t: icon
                        },
                        fb: true, // label in bold
                        d: {
                            name: node.get('name'),
                            serialNumber: node.get('serialNumber'),
                            deviceType: node.get('deviceType'),
                            deviceLifecycleStatus: node.get('deviceLifecycleStatus'),
                            deviceConfiguration: node.get('deviceConfiguration'),
                            gateway: Ext.isEmpty(node.get('gateway')) ? false : node.get('gateway'),
                            alarms: node.get('alarms'),
                            issues: node.get('issues'),
                            failedCommunications: node.get('failedCommunications'),
                            failedComTasks: node.get('failedComTasks'),
                            hasCoordonates: node.get('hasCoordonates')
                        },
                        pos: {
                            lat: Ext.isEmpty(node.get('deviceCoordinates')) ? '' : node.get('deviceCoordinates').latitude.value,
                            lng: Ext.isEmpty(node.get('deviceCoordinates')) ? '' : node.get('deviceCoordinates').longitude.value
                            // lat: Ext.isEmpty(node.get('deviceCoordinates')) ? 45.2251093 : node.get('deviceCoordinates').latitude.value,
                            // lng: Ext.isEmpty(node.get('deviceCoordinates')) ? 22.0192515 : node.get('deviceCoordinates').longitude.value
                            // //lat: 45.595855 ,
                            // lng: 21.845745
                        }
                    }
                );
            });
            if (doGetLinksToo) {
                links.each(function (link) {
                    dataArray2Store.push(
                        {
                            id: link.get('source') + '-' + link.get('target'),
                            type: 'link',
                            id1: link.get('source'),
                            id2: link.get('target'),
                            w: 2,
                            a2: true,
                            d: {
                                linkQuality: link.get('linkQuality')
                            }
                        }
                    );
                });
            }

            pageMainContent.setLoading(false);
            if (Ext.isFunction(callback)) {
                callback();
            }
        });
    },

    clearLayers: function(){
        var me = this;
        me.activeLayers = [];
        me.clearAllLegendItems();
        me.setDefaultStyle();
    },

    setDefaultStyle: function(){
        var me = this,
            items = [],
            icon,
            showGatewayLegend = false,
            showDeviceLegend = false;

        me.chart.each({type: 'node'},function(node){
            if(!me.chart.combo().isCombo(node.id)) {
                if(node.d && !Ext.isEmpty(node.d.gateway) && node.d.gateway){
                    icon = KeyLines.getFontIcon(me.gatewayIcon);
                    showGatewayLegend = true;
                } else {
                    icon = KeyLines.getFontIcon(me.deviceIcon);
                    showDeviceLegend = true;
                }
                items.push(
                    {
                    id: node.id,
                        e: 0.5,
                    b: null, // no border (color)
                    c: me.whiteColor, // fill color
                    fi: {
                        c: (!Ext.isEmpty(node.d.hasCoordonates) && !node.d.hasCoordonates) ? me.noCoordinatesColor : me.neutralColor,
                        t: icon
                    },
                    ha0: null,
                    g: me.chart.combo().isCombo(node.id) ? comboGlyph : null
                });
            }
        });

        me.chart.each({type: 'link'},function(link){
            items.push({
                id: link.id,
                w: 2,
                a2: true,
                c: null,
                t: null
            });
        });
        me.chart.setProperties(items, false);

        if (showGatewayLegend) {
            icon = '<span class="' + me.gatewayIcon + '" style="display:inline-block; font-size:16px; color:' + me.neutralColor + '"></span>';
            me.addLegendItem(icon, Uni.I18n.translate('general.gateway', 'UNI', 'Gateway'));
        }
        if (showDeviceLegend) {
            icon = '<span class="' + me.deviceIcon + '" style="display:inline-block; font-size:16px; color:' + me.neutralColor + '"></span>';
            me.addLegendItem(icon, Uni.I18n.translate('general.device', 'UNI', 'Device'));
            iconNoCoordinates = '<span class="' + me.deviceIcon + '" style="display:inline-block; font-size:16px; color:' + '#a19fa3' + '"></span>';
            me.addLegendItem(iconNoCoordinates, Uni.I18n.translate('general.device.noCoordinates', 'UNI', 'Device without coordinates'));
        }
    },

    forEachNode: function(fNode){
        var me=this,items = [];
        me.chart.each({type: 'node'},function(node){
            if(!me.chart.combo().isCombo(node.id)){
                var result = fNode(node);
                if(result)items.push(result);
            }
        });
        me.chart.setProperties(items, false);
    },

    forEachLink: function(fLink){
        var me=this,items = [];
        me.chart.each({type: 'link'},function(link){
            if(!me.chart.combo().isCombo(link.id)){
                var result = fLink(link);
                if(result)items.push(result)
            }
        });
        me.chart.setProperties(items, false);
    },

    forEachNodeAndLink: function(fNode,fLink){
        var me=this,items = [];
        me.chart.each({type: 'node'},function(node){
            if(!me.chart.combo().isCombo(node.id)){
                var result = fNode(node);
                if(result)items.push(result);
            }
        });
        me.chart.each({type: 'link'},function(link){
            var result = fLink(link);
            if(result)items.push(result)
        });
        me.chart.setProperties(items, false);
    },

    addLayer: function(layerFunction){
        this.activeLayers.push(layerFunction);
    },

    removeLayer: function(layerFunction) {
        Ext.Array.remove(this.activeLayers, layerFunction);
    },

    showLayers:function(){
        var me = this;
        Ext.each(me.activeLayers,function(filter){
            filter.call(me);
        });
        me.legendPanel.show().alignTo(Ext.get('graph-drawing-area'), 'bl-bl', [-5, -15]);
    },

    doLayout: function(name){
        if(name){
            this.graphLayout = name;
        }
        var options = {
            fit: true,
            animate: true,
            tidy: true,
            top: this.top
        };
        this.chart.layout(this.graphLayout,options);
    },

    addLegendItem: function(icon, text) {
        this.legendPanel.addLegendItem(icon, text);
    },

    clearAllLegendItems: function() {
        this.legendPanel.reset();
    },

    clearGraph: function() {
        this.chartData = {
            type: 'LinkChart',
            items: []
        };
        if (this.chart) {
            this.chart.clear();
        }
    },

    preventDraggingNonNodes: function(type, id){
        var me = this;
        if (id != null) {
            var item = me.chart.getItem(id);
            if (item && item.type != 'node') {
                return true;
            }
        }
    },

    refreshGraph: function() {
        var me = this,
            performAfterTheQuery = function() {
                me.chart.load(me.chartData, function () {
                    me.clearAllLegendItems();
                    me.setDefaultStyle();
                    me.showFloatingPanels();
                    me.showLayers();
                    me.doLayout();
                });
            };

        me.hideFloatingPanels();
        me.clearGraph();
        me.queryChartData(performAfterTheQuery);
    },

    refreshLayers: function(linksIncluded) {
        var me = this,
            performAfterTheQuery = function() {
                me.chart.merge(me.freshNodes, performAfterTheMerge);
            },
            performAfterTheMerge = function() {
                me.clearAllLegendItems();
                me.setDefaultStyle();
                me.showLayers();
            };

        me.freshNodes = [];
        me.queryChartData(performAfterTheQuery, me.freshNodes, linksIncluded);
    }
});
