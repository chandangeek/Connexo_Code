Ext.define('Uni.graphvisualiser.VisualiserPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.visualiserpanel',
    itemId: 'VisualiserPanel',
    requires: [
        'Uni.graphvisualiser.VisualiserMenu',
        'Uni.graphvisualiser.VisualiserPropertyViewer'
    ],
    layout: 'fit',
    padding: 10,
    device: null,
    router: null,
    graphLayout: 'standard',
    chartData: {
        type: 'LinkChart',
        items: []
    },
    activeLayers: [],

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

    html: "<div id='graph-drawing-area' style='top: 0; bottom: 0; left: 0; right: 0; position: absolute;'></div>",

    listeners: {
        boxready: function (panel) {
            this.initCanvas(panel);
            //this.sideMenu = Ext.create('Uni.graphvisualiser.VisualiserMenu');
            this.sideMenu = Ext.create(this.menu, {visualiser: this});
            this.sideMenu.show().alignTo(Ext.get('graph-drawing-area'), 'tl-tl');
            this.propertyViewer = Ext.create('Uni.graphvisualiser.VisualiserPropertyViewer');
            this.propertyViewer.show().alignTo(Ext.get('graph-drawing-area'), 'tr-tr');
            //this.propertyViewer.displayProperties();
        }

        ,
        resize: function(panel,w,h){
            KeyLines.setSize('graph-drawing-area',w-10,h);
            this.sideMenu.alignTo(Ext.get('graph-drawing-area'), 'tl-tl');
            this.propertyViewer.alignTo(Ext.get('graph-drawing-area'), 'tr-tr');
            if(this.chart){
                this.doLayout();
            }
        },
        beforedestroy: function(){
            Ext.ComponentQuery.query('#visualiser-menu')[0].destroy();
            Ext.ComponentQuery.query('#property-viewer')[0].destroy();
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
        KeyLines.create({id: 'graph-drawing-area', options:{navigation: {p: 'se'},iconFontFamily: 'Icomoon'}},function(err, chart) {
            me.chart = chart;
            me.chart.bind('click',me.upStreamFromNode,me);
            me.chart.bind('dblclick',me.combine,me);
            me.chart.bind('contextmenu',me.contextMenu,me);
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
        Ext.ComponentQuery.query('#property-viewer')[0].collapse();
    },

    upStreamFromNode: function(id){
        var me = this;
        console.log(arguments);
        var neighbours = {};
        function areNeighboursOf(item){
            return neighbours[item.id];
        }
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
            }
            me.displayNodeProperties(id);
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
        console.log(arguments);
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
        Ext.ComponentQuery.query('#property-viewer')[0].displayProperties(this.chart.getItem(id).d);
    },



    loadData: function(){
        var nodes = this.store.data.items[0].nodes();
        var links = this.store.data.items[0].links();
        var me = this;
        me.top = ["1"];
        nodes.each(function(node){
            me.chartData.items.push({
                        id: node.get('id'),
                        type: 'node',
                       // t: node.deviceType,
                        b: "rgb(0, 102, 153)",
                        c: "rgb(255,255,255)",
                        t: node.get('name'),
                        e: 1,
                        fb: true,
                        //fs: 24,
                        d: {
                            type: node.get('type'),
                            alarms: node.get('alarms')
                        },
                        pos: {
                            lat: 50.82979 + (Math.random() * 0.1 - 0.05),
                            lng: 3.30008 + (Math.random() * 0.1 - 0.05)
                        }
                    });
        });
        links.each(function(link){
            me.chartData.items.push({
                        id: link.get('source') + '-' + link.get('target'),
                        type: 'link',
                        id1: link.get('source'),
                        id2: link.get('target'),
                        w: 2,
                        a2: true,
                        d: {
                            linkQuality: link.get('linkQuality')
                        }
                    });
        });
        me.chart.load(me.chartData, function () {
                    me.chart.layout();
                });
    },

    clearLayers: function(){
        var me = this;
        var items = [];
        me.activeLayers = [];
        me.setDefaultStyle();
    },

    setDefaultStyle: function(){
        var me = this;
        var items = [];
        me.chart.each({type: 'node'},function(node){
            if(!me.chart.combo().isCombo(node.id)) {
                items.push({
                    id: node.id,
                    b: "rgb(0, 102, 153)",
                    c: "rgb(255,255,255)",
                    e: 1,
                    fi: null,
                    //g: null,
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
    }


});
