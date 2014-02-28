Ext.define('Mtr.controller.Dashboard', {
    extend: 'Ext.app.Controller',

    stores: [
        'mock.Browsers',
        'mock.Gapped',
        'mock.Irregular',
        'mock.Stocks'
    ],
    models: [
    ],
    views: [
        'dashboard.Browse'
    ],

    refs: [
        {
            ref: 'dashboard',
            selector: 'dashboardBrowse'
        },
        {
            ref: 'piechart',
            selector: 'dashboardBrowse #piechart'
        },
        {
            ref: 'linechart',
            selector: 'dashboardBrowse #linechart'
        },
        {
            ref: 'splinechart',
            selector: 'dashboardBrowse #splinechart'
        },
        {
            ref: 'columnchart',
            selector: 'dashboardBrowse #columnchart'
        }
    ],

    init: function () {
        this.initMenu();

        this.control({
            'dashboardBrowse #piechart': {
                afterrender: this.initPiechart
            },
            'dashboardBrowse #linechart': {
                afterrender: this.initLinechart
            },
            'dashboardBrowse #splinechart': {
                afterrender: this.initSplinechart
            },
            'dashboardBrowse #columnchart': {
                afterrender: this.initColumnchart
            }
        });
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Dashboard',
            href: Mtr.getApplication().getHistoryDashboardController().tokenizeShowOverview(),
            glyph: 'xf009@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    initPiechart: function () {
        this.getPiechart().bindStore(this.getMockBrowsersStore());
    },
    initLinechart: function () {
        this.getLinechart().bindStore(this.getMockGappedStore());
    },
    initSplinechart: function () {
        this.getSplinechart().bindStore(this.getMockIrregularStore());
    },
    initColumnchart: function () {
        this.getColumnchart().bindStore(this.getMockStocksStore());
    },

    showOverview: function () {
        var widget = Ext.widget('dashboardBrowse');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
