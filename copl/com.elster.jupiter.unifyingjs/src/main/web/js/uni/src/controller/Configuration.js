/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.controller.Configuration
 */
Ext.define('Uni.controller.Configuration', {
    extend: 'Ext.app.Controller',

    refs: [
        {
            ref: 'logo',
            selector: 'navigationLogo'
        },
        {
            ref: 'appSwitcher',
            selector: 'navigationAppSwitcher'
        }
    ],

    init: function () {
        this.getApplication().on('changeapptitleevent', this.changeAppTitle, this);
        this.getApplication().on('changeappglyphevent', this.changeAppGlyph, this);
    },

    changeAppTitle: function (title) {
        var logo = this.getLogo();
        logo.setLogoTitle(title);
    },

    changeAppGlyph: function (glyph) {
        var logo = this.getLogo();
        logo.setLogoGlyph(glyph);
    }

});