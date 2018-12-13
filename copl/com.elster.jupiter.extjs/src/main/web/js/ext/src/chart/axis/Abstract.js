/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
/**
 * @class Ext.chart.axis.Abstract
 * Base class for all axis classes.
 * @private
 */
Ext.define('Ext.chart.axis.Abstract', {

    /* Begin Definitions */

    requires: ['Ext.chart.Chart'],

    /* End Definitions */
    
    /**
     * @cfg {Ext.chart.Label} label
     * The config for chart label.
     */

    /**
     * @cfg {String[]} fields
     * The fields of model to bind to this axis.
     * 
     * For example if you have a data set of lap times per car, each having the fields:
     * `'carName'`, `'avgSpeed'`, `'maxSpeed'`. Then you might want to show the data on chart
     * with `['carName']` on Name axis and `['avgSpeed', 'maxSpeed']` on Speed axis.
     */

    /**
     * Creates new Axis.
     * @param {Object} config (optional) Config options.
     */
    constructor: function(config) {
        config = config || {};

        var me = this,
            pos = config.position || 'left';

        pos = pos.charAt(0).toUpperCase() + pos.substring(1);
        //axisLabel(Top|Bottom|Right|Left)Style
        config.label = Ext.apply(config['axisLabel' + pos + 'Style'] || {}, config.label || {});
        config.axisTitleStyle = Ext.apply(config['axisTitle' + pos + 'Style'] || {}, config.labelTitle || {});
        Ext.apply(me, config);
        me.fields = Ext.Array.from(me.fields);
        this.callParent();
        me.labels = [];
        me.getId();
        me.labelGroup = me.chart.surface.getGroup(me.axisId + "-labels");
    },

    alignment: null,
    grid: false,
    steps: 10,
    x: 0,
    y: 0,
    minValue: 0,
    maxValue: 0,

    getId: function() {
        return this.axisId || (this.axisId = Ext.id(null, 'ext-axis-'));
    },

    /*
      Called to process a view i.e to make aggregation and filtering over
      a store creating a substore to be used to render the axis. Since many axes
      may do different things on the data and we want the final result of all these
      operations to be rendered we need to call processView on all axes before drawing
      them.
    */
    processView: Ext.emptyFn,

    drawAxis: Ext.emptyFn,
    addDisplayAndLabels: Ext.emptyFn
});
