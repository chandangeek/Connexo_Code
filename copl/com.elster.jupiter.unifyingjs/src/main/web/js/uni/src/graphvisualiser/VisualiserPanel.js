Ext.define('Uni.graphvisualiser.VisualiserPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uniVisualiserPanel',
    itemId: 'VisualiserPanel',
    requires: [
        'Uni.graphvisualiser.VisualiserMenu',
        'Uni.graphvisualiser.VisualiserPropertyViewer'
    ],
    //layout: 'fit',
    layout: {
        type: 'border'
    },
    items: [
        {
            xtype: 'container',
            html: "<div id='graph-drawing-area' style='top: 0; bottom: 0; left: 0; right: 0; position: absolute;'></div>",
            region: 'center'
        },
        {
            itemId: 'uni-visualiser-legend-table',
            title: Uni.I18n.translate('general.legend', 'UNI', 'Legend'),
            region: 'south',
            //height: 120,
            collapsible: true,
            split: true,
            splitterResize: false,
            layout: {
                type: 'table',
                columns: 16 // legend icon = one column & legend text = another column
            }
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

    gatewayIcon: 'icon-diamond4',
    deviceIcon: 'icon-circle2',
    hopLevelIcon: 'icon-radio-unchecked',

    neutralColor: '#006699',
    whiteColor: '#FFFFFF',
    issueAlarmColor: '#FF0000',
    colors: [
            "#BEE64B",
            "#33CC99",
            "#00CCCC",
            "#2887C8",
            "#C3CDE6",
            "#7070CC",
            "#C9A0DC",
            "#733380",
            "#2D383A",
            "#C5E17A",
            "#5E8C31",
            "#7BA05B",
            "#7BA05B",
            "#63B76C",
            "#4D8C57",
            "#3AA655",
            "#6CA67C",
            "#5FA777",
            "#93DFB8"
    ],

    //html: "<div id='graph-drawing-area' style='top: 0; bottom: 0; left: 0; right: 0; position: absolute;'></div>",

    LAYER_DEVICETYPE: 'deviceType',

    listeners: {
        boxready: function (panel) {
            this.initCanvas(panel);
            this.sideMenu = Ext.create(this.menu, {visualiser: this});
            this.sideMenu.show().alignTo(Ext.get('graph-drawing-area'), 'tl-tl');
            this.propertyViewer = Ext.create('Uni.graphvisualiser.VisualiserPropertyViewer');
            this.propertyViewer.show().alignTo(Ext.get('graph-drawing-area'), 'tr-tr', [-5, 0]);
            //this.legend = Ext.create('Uni.graphvisualiser.VisualiserLegend');
            //this.legend.show();
            //this.legend.doAlign();
            //this.propertyViewer.displayProperties();
        }

        ,
        resize: function(panel,w,h){
            KeyLines.setSize('graph-drawing-area',w-10,h);
            this.sideMenu.alignTo(Ext.get('graph-drawing-area'), 'tl-tl');
            this.propertyViewer.alignTo(Ext.get('graph-drawing-area'), 'tr-tr', [-5, 0]);
            //this.legend.doAlign();
            if(this.chart){
                this.doLayout();
            }
        },
        beforedestroy: function(){
            Ext.ComponentQuery.query('#uni-visualiser-menu')[0].destroy();
            Ext.ComponentQuery.query('#uni-property-viewer')[0].destroy();
            //Ext.ComponentQuery.query('#visualiser-legend')[0].destroy();
        }
    },

    initComponent: function () {
        var me = this;

        //Ext.Ajax.request({
        //    url: '/api/ddr/devices/d3?page=1&start=0&limit=200',
        //    method: 'GET',
        //    success: function (operation) {
        //        me.data = JSON.parse(operation.responseText);
        //        //me.on('resize', me.initCanvas(me));
        //        me.initCanvas(me);
        //
        //    }
        //});


        me.callParent(arguments);


        //me.initCanvas(me);
    },

    initCanvas: function (me) {
        var me = this;

        KeyLines.paths({assets: 'resources/js/keylines/assets/'});
        KeyLines.create({id: 'graph-drawing-area', options:{navigation: {p: 'se', y: -150},iconFontFamily: 'Icomoon'}},function(err, chart) {
            me.chart = chart;
            me.chart.bind('click', me.upStreamFromNode, me);
            me.chart.bind('dblclick', me.combine, me);
            me.chart.bind('contextmenu', me.contextMenu, me);
            me.chart.load({
                type: 'LinkChart'
            });
            me.loadData();
        });
    },

    contextMenu: function(id,x,y){
        if(id) {
            var visualiser = Ext.ComponentQuery.query('visualiserpanel')[0];
            var items = [
                {
                    text: 'Upstream', handler: function () {
                    visualiser.upStreamFromNode(id);
                }
                },
                {
                    text: 'Downstream', handler: function () {
                    visualiser.downStreamFromNode(id);
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
            var menu_grid = new Ext.menu.Menu({
                items: items,
                listeners: {
                    render: function (menu) {
                        menu.getEl().on('contextmenu', Ext.emptyFn, null, {preventDefault: true});
                    }
                }
            });
            menu_grid.showAt([x, y]);
        }
        return false;
    },

    collapsePropertyViewer: function(){
        Ext.ComponentQuery.query('#uni-property-viewer')[0].collapse();
    },

    upStreamFromNode: function(id){
        var me = this,
            neighbours = {},
            areNeighboursOf = function(item) {
                return neighbours[item.id];
            };

        if(id === null) {
            me.chart.foreground(function(){ return true; }, {type: 'all'});
            me.collapsePropertyViewer();
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
            } else {
                me.collapsePropertyViewer();
            }
        }
    },

    combine: function(id){
        var me = this;
        if(me.chart.combo().isCombo(id)){
            me.chart.combo().uncombine(id,null,function(){
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
                label: 'Combined',
                glyph: null,
                style: {
                    c: null,
                    fi: {
                        c: '#8e8e8e',
                        t:  KeyLines.getFontIcon('icon-plus')
                    }
                }

            },null,function(){
                me.doLayout();
            });
        }
    },

    downStreamFromNode: function(id){
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
            }
        }
    },

    getDownStreamNodesLinks: function(id){
            return this.chart.graph().neighbours(id, {direction: 'from',hops: 1000});
    },

    displayNodeProperties: function(id){
        Ext.ComponentQuery.query('#uni-property-viewer')[0].displayProperties(this.chart.getItem(id).d);
    },


    loadData: function(){
        var me = this,
            nodes = this.store.data.items[0].nodes(),
            links = this.store.data.items[0].links(),
            nodeStoreForComboBox = new Ext.data.SimpleStore({
                fields: ['id', 'name']
            }),
            icon,
            showGatewayLegend = false,
            showDeviceLegend = false;

        me.top = ["1"];
        nodes.each(function(node){
            nodeStoreForComboBox.add({
                id: node.get('id'),
                name: node.get('name')
            });
            if(!Ext.isEmpty(node.get('gateWay')) && node.get('gateWay')){
                icon = KeyLines.getFontIcon(me.gatewayIcon);
                showGatewayLegend = true;
            } else {
                icon = KeyLines.getFontIcon(me.deviceIcon);
                showDeviceLegend = true;
            }
            me.chartData.items.push(
                {
                    id: node.get('id'),
                    type: 'node',
                    b: null, // no border (color)
                    c: me.whiteColor, // fill color
                    t: node.get('name'),
                    fi: {
                        c: me.neutralColor,
                        t: icon
                    },
                    fb: true, // label in bold
                    d: {
                        name: node.get('name'),
                        type: node.get('deviceType'),
                        gateway: Ext.isEmpty(node.get('gateWay')) ? false : node.get('gateWay'),
                        alarms: node.get('alarms'),
                        issues: node.get('issues')
                    },
                    pos: {
                        lat: 50.82979 + (Math.random() * 0.1 - 0.05),
                        lng: 3.30008 + (Math.random() * 0.1 - 0.05)
                    }
                }
            );
        });
        links.each(function(link){
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
        me.chart.load(me.chartData, function () {
            me.chart.layout();
            if (showGatewayLegend) {
                icon = '<span class="' + me.gatewayIcon + '" style="display:inline-block; font-size:16px; color:' + me.neutralColor + '"></span>';
                me.addLegendItem(icon, Uni.I18n.translate('general.gateway', 'UNI', 'Gateway'));
            }
            if (showDeviceLegend) {
                icon = '<span class="' + me.deviceIcon + '" style="display:inline-block; font-size:16px; color:' + me.neutralColor + '"></span>';
                me.addLegendItem(icon, Uni.I18n.translate('general.device', 'UNI', 'Device'));
            }
        });
        me.sideMenu.down('combobox').bindStore(nodeStoreForComboBox);
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
            legendTable = me.down('#uni-visualiser-legend-table');
        Ext.Array.each(items, function(item){
            legendTable.add(item);
        });
    },

    clearAllLegendItems: function() {
        this.down('#uni-visualiser-legend-table').removeAll();
    }

});
