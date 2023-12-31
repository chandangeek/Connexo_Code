/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.require(['Ext.draw.Text', 'Ext.slider.Single']);
Ext.onReady(function() {
    var fill = Ext.getBody().hasCls('x-theme-access') ? '#fff' : '#000';

    Ext.create('Ext.draw.Text', {
        renderTo: Ext.get('text-ph'),
        padding: 20,
        height: 350,
        degrees: 45,
        text: 'With Ext JS 4.0 Drawing',
        textStyle: {
            fill: fill,
            'font-size': '18px',
            'font-family': 'Arial'
        }
    });

    Ext.create('Ext.draw.Text', {
        renderTo: Ext.get('text-ph'),
        padding: 20,
        height: 350,
        degrees: 90,
        text: 'Creating a rotated Text component',
        textStyle: {
            fill: fill,
            'font-size': '18px',
            'font-family': 'Arial'
        }
    });

    Ext.create('Ext.draw.Text', {
        renderTo: Ext.get('text-ph'),
        id: 'snappy',
        width: 200,
        height: 350,
        autoSize: false,
        viewBox: false,
        padding: 20,
        degrees: 315,
        text: 'Is a snap!',
        textStyle: {
            padding: 20,
            fill: fill,
            'font-size': '18px',
            'font-family': 'Arial',
            y: 50
        }
    });

    Ext.create('Ext.slider.Single', {
        renderTo: Ext.get('slider-ph'),
        hideLabel: true,
        width: 400,
        minValue: 0,
        maxValue: 360,
        value: 315,
        listeners: {
            change: function(slider, value) {
                var sprite = Ext.getCmp('snappy').surface.items.first();
                sprite.setAttributes({
                    rotation: {
                        degrees: value
                    }
                }, true);
            }
        }
    });
});
