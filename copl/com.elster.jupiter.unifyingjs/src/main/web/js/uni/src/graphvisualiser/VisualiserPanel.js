Ext.define('Uni.graphvisualiser.VisualiserPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.visualiserpanel',
    itemId: 'VisualiserPanel',
    resuires: [
        'Uni.graphvisualiser.VisualiserMenu'
    ],
    layout: 'fit',
    padding: 10,
   // frame: true,
    device: null,
    router: null,
    graphLayout: 'standard',
    chartData: {
        type: 'LinkChart',
        items: []
    },

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

    colors2: [
        '#45CCFF',
        '#49E83E',
        '#FFD432',
        '#E84B30',
        '#B243FF',
        "#BEE64B",
        "#33CC99",
        "#00CCCC",
        "#2887C8",
        "#C3CDE6",
        "#7070CC",
        "#C9A0DC"
    ],

    html: "<div id='test-keylines' style='top: 0; bottom: 0; left: 0; right: 0; position: absolute;'></div>",


    //tiles: {
    //    id: 'tiles',
    //    name: 'Connexo tiles',
    //    url: 'http://neitvs021.eict.local/osm_tiles/{z}/{x}/{y}.png',
    //    attribution: 'Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, under <a href="http://www.openstreetmap.org/copyright">ODbL</a>.',
    //    //subdomains: 'abcd',
    //    minZoom: 0,
    //    maxZoom: 20,
    //    ext: 'png',
    //    //src: 'images/hubway/stamen-toner-lite.png',
    //    opacity: 0.5
    //},
    listeners: {
        boxready: function (panel) {
            this.initCanvas(panel);
            this.sideMenu = Ext.create('Uni.graphvisualiser.VisualiserMenu');
            this.sideMenu.show().alignTo(Ext.get('test-keylines'), 'tl-tl');
            this.propertyViewer = Ext.create('Uni.graphvisualiser.VisualiserPropertyViewer');
            this.propertyViewer.show().alignTo(Ext.get('test-keylines'), 'tr-tr');
            //this.propertyViewer.displayProperties();
        }

        ,
        resize: function(panel,w,h){
            KeyLines.setSize('test-keylines',w-10,h);
            this.sideMenu.alignTo(Ext.get('test-keylines'), 'tl-tl');
            this.propertyViewer.alignTo(Ext.get('test-keylines'), 'tr-tr');
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

        //for (var i = 0; i < me.data.nodes.length; i++) {
        //    var node = me.data.nodes[i];
        //    me.chartData.items.push({
        //        id: node.id,
        //        type: 'node',
        //        t: node.deviceType,
        //        c: "rgb(0, 153, 51)",
        //        e: 0.5,
        //        pos: {
        //            lat: 50.82979 + (Math.random() * 0.1 - 0.05),
        //            lng: 3.30008 + (Math.random() * 0.1 - 0.05)
        //        }
        //    });
        //
        //}
        //for (var j = 0; j < me.data.links.length; j++) {
        //    var link = me.data.links[j];
        //    me.chartData.items.push({
        //        id: link.source + link.target,
        //        type: 'link',
        //        id1: link.source,
        //        id2: link.target,
        //        w: 1
        //    });
        //
        //}
        KeyLines.paths({assets: 'resources/js/keylines/assets/'});
        KeyLines.create({id: 'test-keylines', options:{navigation: {p: 'se'},iconFontFamily: 'Icomoon'}},function(err, chart) {
            me.chart = chart;
            me.chart.bind('click',me.upStreamFromNode,me);
            me.chart.bind('dblclick',me.combine,me);
            me.chart.bind('contextmenu',me.contextMenu,me);
            me.chart.load({
                type: 'LinkChart',
               // items: [{id:'id1', type: 'node', x:150, y: 150, t:'Hello World!'}]
            });
            me.loadData();
        });
        //KeyLines.create(me.body.id, function (err, chart) {
        //    me.chart = chart;
        //    me.chart.bind('dblclick', function (id) {
        //        //function areNeighboursOf(item){
        //        //    return neighbours[item.id];
        //        //}
        //        //var neighbours = {};
        //        //if(id === null) {
        //        //    // clicked on background - restore all the elements in the foreground
        //        //    me.chart.foreground(function(){ return true; }, {type: 'all'});
        //
        //        //
        //        if (me.chart.map().isShown()) {
        //            me.chart.map().hide(function () {
        //                var items = [];
        //                me.chart.each({type: 'node'}, function (node) {
        //                    var item = {id: node.id};
        //                    item.e = 0.5;
        //                    items.push(item);
        //                });
        //                me.chart.animateProperties(items, {time: 100});
        //            });
        //        } else {
        //            me.chart.map().show(function () {
        //                var items = [];
        //                me.chart.each({type: 'node'}, function (node) {
        //                    var item = {id: node.id};
        //                    item.e = 0.1;
        //                    items.push(item);
        //                });
        //                me.chart.animateProperties(items, {time: 100});
        //            });
        //        }
        //
        //
        //        //} else {
        //        //    var item = me.chart.getItem(id);
        //        //    if (item && item.type === 'node') {
        //        //        var result = me.chart.graph().neighbours(id);
        //        //        $.each(result.nodes, function (i, id){
        //        //            neighbours[id] = true;
        //        //        });
        //        //        $.each(result.links, function (i, id){
        //        //            neighbours[id] = true;
        //        //        });
        //        //        neighbours[id] = true;
        //        //        me.chart.foreground(areNeighboursOf, {type: 'all'});
        //        //    }
        //        //}
        //    });
        //    me.chart.bind('viewchange', function(){
        //        var me = this;
        //        var zoomTemp = 0;
        //        console.log(me.viewOptions().zoom);
        //        var items = [];
        //        if(me.viewOptions().zoom !== zoomTemp && this.map().isShown()){
        //            zoomTemp = me.viewOptions().zoom;
        //            this.each({type: 'node'}, function (node) {
        //                var item = { id: node.id };
        //                var size = (me.viewOptions().zoom - 1)*2;
        //                if(size < 0.2)size=0.2;
        //                if(size > 1)size=1;
        //                item.e = size;
        //                //   item.e = Math.random();
        //                items.push(item);
        //            });
        //            this.animateProperties(items, { time: 100 });
        //        }
        //    },me.chart);
        //    // me.chart.load(me.chartData, chart.layout);
        //    me.chart.load(me.chartData, function () {
        //        me.chart.layout();
        //        me.chart.map().options({tiles: me.tiles});
        //        // me.chart.map().show();
        //        // me.chart.layout();
        //    });
        //
        //});
    },

    contextMenu: function(id,x,y){
        console.log(id + '-' + x + '-' + y);
        //Ext.get('body').on('contextmenu',function(e){
        //    e.preventDefault();
        //    return false;
        //});
        if(id) {
            var menu_grid = new Ext.menu.Menu({
                items: [
                    {
                        text: 'Upstream', handler: function () {
                        Ext.ComponentQuery.query('visualiserpanel')[0].upStreamFromNode(id);
                    }
                    },
                    {
                        text: 'Downstream', handler: function () {
                        Ext.ComponentQuery.query('visualiserpanel')[0].downStreamFromNode(id);
                    }
                    }
                ],
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
                me.doLayout();
            });
        } else {
            var result = me.getDownStreamNodesLinks(id);
            result.nodes.push(id);
            me.chart.combo().combine({ids: result.nodes},null,function(){
                me.doLayout();
            });
        }
        //me.doLayout();
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
               // var result = me.chart.graph().neighbours(id,{direction: 'from'});
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
                        },
                        //bu: {
                        //    c: 'rgb(255, 0, 0)',         //the bubble fill colour
                        //    p: 'ne',                     //bubble in NE position. use 'se', 'sw', 'nw' for the other positions
                        //    t: 'Bubble'                  //the bubble text
                        //}
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
                    //me.chart.map().options({tiles: me.tiles});
                    // me.chart.map().show();
                    // me.chart.layout();
                });
       // debugger;
    },

    clearFilters: function(){
        var me = this;
        var items = [];
        me.chart.each({type: 'node'},function(node){
            items.push({
                id: node.id,
                b: "rgb(0, 102, 153)",
                c: "rgb(255,255,255)",
                e: 1,
                fi: null,
                g: null,
                ha0: null

            });
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

    showLinkQuality: function(){
        var me = this;
        var items = [];
        var colors = ['#FF0000','#CC3300','#996600','#669900','#33CC00'];
        me.chart.each({type: 'link'},function(link){
       //     debugger;
            items.push({
                id: link.id,
                w: link.d.linkQuality*2,
                c: colors[link.d.linkQuality-1],
                t: link.d.linkQuality
            });
        });
        me.chart.animateProperties(items, { time: 200 });
    },

    showDeviceType: function(){
        var me = this;
        var items = [];
        var icon;
        me.chart.each({type: 'node'},function(node){
            if(node.d.type === 'device'){
                icon = KeyLines.getFontIcon('icon-calculator')
            } else {
                icon = KeyLines.getFontIcon('icon-station2')
            }
            items.push({
                id: node.id,
                b: null,
                fi: {
                    c: '#8e8e8e',
                    t: icon
                }
            });
        });
        me.chart.setProperties(items, false);
        //me.chart.animateProperties(items, { time: 200 });
    },

    showAlarms: function(){
        var me = this;
        var items = [];
        var icon;
        me.chart.each({type: 'node'},function(node) {
            if (node.d.alarms) {
                items.push({
                    id: node.id,
                    g: [
                        {
                            c: 'rgb(255, 0, 0)',
                            p: 'ne',
                            t: node.d.alarms
                        }
                    ],
                    ha0: {
                        c: 'rgb(255, 0, 0)',         //the halo fill colour
                        r: 50,                      //the halo radius
                        w: 3                        //the halo width
                    }
                });
            }
        });
        me.chart.setProperties(items, false);
    },

    showHopLevel: function(){
        var me = this;
        var distances = me.chart.graph().distances(this.top);
        var labels = Object.keys(distances).map(function(key) {
            return {id: key, c: me.colors[distances[key]]};
        });
        me.chart.setProperties(labels);

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
