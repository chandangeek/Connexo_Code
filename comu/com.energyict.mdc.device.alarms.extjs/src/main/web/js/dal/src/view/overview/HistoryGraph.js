Ext.define('Dal.view.overview.HistoryGraph', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.chart.*',
        'Ext.data.JsonStore',
        'Ext.util.HashMap'
    ],
    ui: 'tile',
    alias: 'widget.history-graph',
    store: undefined,
    defaultColors: ['#BEE64B', '#33CC99', '#00CCCC', '#7ED4E6', '#2887C8', '#C3CDE6', '#7070CC', '#C9A0DC', '#733380', '#2D383A',
        '#5E8C31', '#7BA05B', '#4D8C57', '#3AA655', '#93DFB8', '#1AB385', '#29AB87', '#00CC99', '#00755E',
        '#8DD9CC', '#01786F', '#30BFBF', '#008080', '#8FD8D8',
        '#95E0E8', '#6CDAE7', '#76D7EA', '#0095B7', '#009DC4', '#02A4D3', '#47ABCC', '#4997D0', '#339ACC',
        '#93CCEA', '#00468C', '#0066CC', '#1560BD', '#0066FF', '#A9B2C3', '#4570E6', '#7A89B8', '#4F69C6',
        '#8D90A1', '#8C90C8', '#9999CC', '#ACACE6', '#766EC8', '#6456B7', '#3F26BF', '#8B72BE', '#652DC1', '#6B3FA0',
        '#8359A3', '#8F47B3', '#BF8FCC', '#803790', '#D6AEDD', '#C154C1', '#FC74FD', '#C5E17A', '#9DE093', '#63B76C', '#6CA67C', '#5FA777'],

    colors: null,
    colorsPerReasons: null,
    showLegend: true,
    tooltipAlarmMsg: null,
    width: 800,
    height: 300,
    layout: 'fit',
    xMinValue: null,
    xMaxValue: null,
    yMaxSumPerDay: null,
    dayInMilieconds: 86400 * 1000,
    fields: null,
    defaultFields: null,
    legendTitle: undefined,
    translationFields: null,
    translations: null,

    processStore: function () {
        var me = this;
        me.store.each(function (rec) {
            me.xMinValue = (me.xMinValue == null) ? rec.get('date') : Math.min(me.xMinValue, rec.get('date'));
            me.xMaxValue = Math.max(me.xMaxValue, rec.get('date'));
        });

        me.store.each(function (rec) {
            var sumPerDay = 0;
            Ext.Array.each(me.fields, function (field) {
                sumPerDay += rec.get(field);
            });
            me.yMaxSumPerDay = Math.max(me.yMaxSumPerDay, sumPerDay);
        });
    },

    constructColors: function () {
        var me = this;

        Ext.define('Ext.chart.theme.ColumnTheme', {
            extend: 'Ext.chart.theme.Base',
            constructor: function (config) {
                this.callParent([Ext.apply({
                    colors: me.defaultColors
                }, config)]);
            }
        });
    },

    constructTranslation: function () {
        var me = this;

        me.translations = new Ext.util.HashMap();
        if (me.translationFields) {
            Ext.Array.each(me.translationFields, function (translationField) {
                me.translations.add(translationField.reason, translationField.translation);
            });
        }
    },

    refresh: function (data) {
        var me = this, storeFields;


    },

    destroyControls: function () {
        var me = this;
        me.xMinValue = null;
        me.xMaxValue = null;
        me.yMaxSumPerDay = 0;
        me.colors = null;
        me.fields = me.defaultFields;
        //    me.remove(me.down('chart'), true);
    },

    getChart: function (store) {

    }

});