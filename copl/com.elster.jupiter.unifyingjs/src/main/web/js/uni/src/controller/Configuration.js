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
        this.getApplication().on('addappitemevent', this.addAppItem, this);
        this.getApplication().on('setappitemsevent', this.setAppItems, this);

        this.getApplication().on('changeapptitleevent', this.changeAppTitle, this);
        this.getApplication().on('changeappglyphevent', this.changeAppGlyph, this);
    },

    addAppItem: function (appItem) {
        var switcher = this.getAppSwitcher();
        // TODO Add the app item to the app switcher.
    },

    setAppItems: function (appItems) {
        var switcher = this.getAppSwitcher();
        // TODO Set the app items in the app switcher.
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