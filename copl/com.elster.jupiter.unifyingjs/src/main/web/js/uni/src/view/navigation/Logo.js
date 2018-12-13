/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.navigation.Logo
 */
Ext.define('Uni.view.navigation.Logo', {
    extend: 'Ext.button.Button',
    xtype: 'uni-nav-logo',
    ui: 'navigationlogo',

    text: 'Connexo',

    action: 'home',
    scale: 'medium',
    href: '#',
    hrefTarget: '_self',

    initComponent: function () {
        this.callParent(arguments);
    },

    setLogoTitle: function (title) {
        if (this.rendered) {
            this.setText(title);
        } else {
            this.text = title;
        }
    },

    setLogoGlyph: function (glyph) {
        if (this.rendered) {
            this.setGlyph(glyph);
        } else {
            this.glyph = glyph;
        }
    }
});