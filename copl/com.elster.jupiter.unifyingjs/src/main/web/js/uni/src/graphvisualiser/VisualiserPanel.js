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
    device: null,
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

    gatewayIcon: 'icon-diamond4',
    deviceIcon: 'icon-circle2',
    linkIcon: 'icon-arrow-down-right2',
    issueAlarmIcon: 'icon-circle-small',
    failedCommunicationIcon: 'icon-circle',

    neutralColor: '#006699',
    whiteColor: '#FFFFFF',
    blackColor: '#000000',
    collapsedColor: '#8e8e8e',
    issueAlarmColor: '#FF0000',
    failedCommunicationStatusColor: '#FF0000',
    goodLinkQualityColor: '#70BB51',
    badLinkQualityColor: '#EB5642',
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
                me.loadData();
            }
        );
    },

    contextMenu: function(id,x,y){
        if(id) {
            var visualiser = Ext.ComponentQuery.query('visualiserpanel')[0];
            var items = [
                {
                    xtype: 'menuitem',
                    text: Uni.I18n.translate('general.highlight.upstream', 'UNI', 'Highlight upstream'),
                    section: 1, /*SECTION_ACTION*/
                    handler: function () {
                        visualiser.highlightUpStreamFromNode(id);
                    }
                },
                {
                    xtype: 'menuitem',
                    text: Uni.I18n.translate('general.highlight.downstream', 'UNI', 'Highlight downstream'),
                    section: 1, /*SECTION_ACTION*/
                    handler: function () {
                        visualiser.highlightDownStreamFromNode(id);
                    }
                },
                {
                    xtype: 'menuitem',
                    text: visualiser.chart.combo().isCombo(id)
                        ? Uni.I18n.translate('general.expand', 'UNI', 'Expand')
                        : Uni.I18n.translate('general.collapse', 'UNI', 'Collapse'),
                    section: 1, /*SECTION_ACTION*/
                    handler: function () {
                        visualiser.combine(id);
                    }
                }
            ];
            if(this.contextMenuItems){
                Ext.each(this.contextMenuItems, function(item){
                    item.graphId = id;
                    item.visualiser = visualiser;
                });
                items = items.concat(this.contextMenuItems);
            }

            var popupMenu = Ext.create('Uni.view.menu.ActionsMenu',
                {
                    items: items,
                    listeners: {
                        render: function (menu) {
                            menu.getEl().on('contextmenu', Ext.emptyFn, null, {preventDefault: true});
                        }
                    }
                }
            );
            var position = this.getPosition();
            popupMenu.showAt([position[0]+x, position[1]+y]);
        }
        return false;
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
                me.clearAllLegendItems();
                me.setDefaultStyle();
                Ext.each(me.activeLayers,function(layer){
                    layer.call(me);
                });
                me.doLayout();
            });
        } else {
            var result = me.getDownStreamNodesLinks(id);
            result.nodes.push(id);
            me.chart.combo().combine({
                ids: result.nodes,
                label: Uni.I18n.translate('general.collapsed', 'UNI', 'Collapsed'),
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

    displayNodeProperties: function(id){
        Ext.ComponentQuery.query('#uni-property-viewer')[0].displayProperties(this.chart.getItem(id).d);
    },

    addFloatingPanels: function(){
        var me = this;
        me.sideMenu = Ext.create(me.menu, {visualiser: me});
        me.propertyViewer = Ext.create('Uni.graphvisualiser.VisualiserPropertyViewer', {
            title: me.propertyViewerTitle
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

    loadData: function(){
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
            };

        me.queryChartData(performAfterTheQuery);
    },

    queryChartData: function(callback) {
        var me = this,
            pageMainContent = Ext.ComponentQuery.query('viewport')[0];

        pageMainContent.setLoading(true);
        this.store.load(function() {
            var nodes = me.store.data.items[0].nodes(),
                links = me.store.data.items[0].links(),
                icon;

            me.showGatewayLegend = false;
            me.showDeviceLegend = false;
            me.nodeStoreForComboBox = new Ext.data.SimpleStore({ fields: ['id', 'name'] });
            me.top = [nodes.data.items[0].get('id') + '']; // Assumption: the first node is the top node
            nodes.each(function (node) {
                me.nodeStoreForComboBox.add({
                    id: node.get('id'),
                    name: node.get('name')
                });
                if (!Ext.isEmpty(node.get('gateway')) && node.get('gateway')) {
                    icon = KeyLines.getFontIcon(me.gatewayIcon);
                    me.showGatewayLegend = true;
                } else {
                    icon = KeyLines.getFontIcon(me.deviceIcon);
                    me.showDeviceLegend = true;
                }
                me.chartData.items.push(
                    {
                        id: node.get('id'),
                        type: 'node',
                        b: null, // no border (color)
                        c: me.whiteColor, // fill color
                        // t: node.get('name'), // No name/label in the graph
                        fi: {
                            c: me.neutralColor,
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
                            failedComTasks: node.get('failedComTasks')
                        },
                        pos: {
                            lat: 50.82979 + (Math.random() * 0.1 - 0.05),
                            lng: 3.30008 + (Math.random() * 0.1 - 0.05)
                        }
                    }
                );
            });
            links.each(function (link) {
                me.chartData.items.push(
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
                    b: null, // no border (color)
                    c: me.whiteColor, // fill color
                    fi: {
                        c: me.neutralColor,
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
        this.addLegendItems([
            {
                xtype: 'displayfield',
                fieldLabel: '',
                labelWidth: 0,
                margin: '0 5 0 0',
                iconForRenderer: icon, // to make it work when the legend panel is collapsed and the rendering is done later on when the panel expands
                renderer: function(raw, displayField) {
                    return displayField.iconForRenderer;
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: '',
                labelWidth: 0,
                margin: '0 15 0 0',
                value: text
            }
        ]);
    },

    addLegendItems: function(items) {
        var me = this,
            legendTable = me.legendPanel.down('#uni-visualiser-legend-table');
        Ext.Array.each(items, function(item){
            legendTable.add(item);
        });
    },

    clearAllLegendItems: function() {
        this.legendPanel.down('#uni-visualiser-legend-table').removeAll();
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

    refreshChart: function() {
        var me = this,
            performAfterTheQuery = function() {
                me.clearAllLegendItems();
                me.setDefaultStyle();
                me.showFloatingPanels();
                me.chart.load(me.chartData, function () {
                    me.doLayout();
                    me.showLayers();
                });
            };

        me.hideFloatingPanels();
        me.queryChartData(performAfterTheQuery);
    }

});
