/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
/**
 * @private
 */
Ext.define('Ext.chart.MaskLayer', {
    extend: 'Ext.Component',
    
    constructor: function(config) {
        config = Ext.apply(config || {}, {
            style: 'position:absolute;background-color:#ff9;cursor:crosshair;opacity:0.5;border:1px solid #00f;'
        });
        this.callParent([config]);    
    },
    
    initComponent: function() {
        var me = this;
        me.callParent(arguments);
        me.addEvents(
            'mousedown',
            'mouseup',
            'mousemove',
            'mouseenter',
            'mouseleave'
        );
    },

    initDraggable: function() {
        this.callParent(arguments);
        this.dd.onStart = function (e) {
            var me = this,
                comp = me.comp;
    
            // Cache the start [X, Y] array
            this.startPosition = comp.getPosition(true);
    
            // If client Component has a ghost method to show a lightweight version of itself
            // then use that as a drag proxy unless configured to liveDrag.
            if (comp.ghost && !comp.liveDrag) {
                 me.proxy = comp.ghost();
                 me.dragTarget = me.proxy.header.el;
            }
    
            // Set the constrainTo Region before we start dragging.
            if (me.constrain || me.constrainDelegate) {
                me.constrainTo = me.calculateConstrainRegion();
            }
        };
    }
});