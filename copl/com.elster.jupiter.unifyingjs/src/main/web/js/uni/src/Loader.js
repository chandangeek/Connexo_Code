/**
 * Loader class for the Unifying JS project, it makes sure every dependency is
 * ready to use.
 *
 * @note
 */
Ext.define('Uni.Loader', {

    scriptLoadingCount: 0,

    requires: [
        'Ext.tip.QuickTipManager',
        'Uni.util.I18n',
        'Ext.state.CookieProvider',

        'Uni.controller.Configuration',
        'Uni.controller.Error',
        'Uni.controller.Navigation',
        'Uni.controller.Notifications',
        'Uni.controller.Search'
    ],

    /**
     * Initializes the internationalization components that should be used during loading.
     *
     * @param {String} components Components to load
     */
    initI18n: function (components) {
        I18n.init(components);
    },

    onReady: function (callback) {
        var me = this;

        this.loadFont();
        this.loadTooltips();
        this.loadStateManager();
        this.loadOverrides();
        this.loadStores();

        this.loadScripts(function () {
            me.afterLoadingScripts(callback);
        });
    },

    loadFont: function () {
        Ext.setGlyphFontFamily('icomoon');
    },

    loadTooltips: function () {
        Ext.tip.QuickTipManager.init();
    },

    loadStateManager: function () {
        Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));
    },

    loadOverrides: function () {
        Ext.require('Uni.override.ApplicationOverride');
        Ext.require('Uni.override.CheckboxOverride');
        Ext.require('Uni.override.FieldBaseOverride');
        Ext.require('Uni.override.LabelableOverride');
        Ext.require('Uni.override.FieldContainerOverride');
        Ext.require('Uni.override.NumberFieldOverride');
        Ext.require('Uni.override.JsonWriterOverride');
        Ext.require('Uni.override.RestOverride');
        Ext.require('Uni.override.StoreOverride');
    },

    loadStores: function () {
        Ext.require('Uni.store.AppItems');
        Ext.require('Uni.store.Notifications');
        Ext.require('Uni.store.Translations');
    },

    loadScript: function (src, callback) {
        var me = this,
            script = document.createElement('script'),
            loaded;

        script.setAttribute('src', src);
        if (callback) {
            me.scriptLoadingCount++;
            script.onreadystatechange = script.onload = function () {
                if (!loaded) {
                    me.scriptLoadingCount--;
                    if (me.scriptLoadingCount === 0) {
                        callback();
                    }
                }
                loaded = true;
            };
        }

        document.getElementsByTagName('head')[0].appendChild(script);
    },

    /**
     * Loads the required scripts asynchronously and calls the callback when all scripts have finished loading.
     *
     * @param {Function} callback Callback to call after all scripts have finished loading
     */
    loadScripts: function (callback) {
        this.loadScript('../uni/resources/js/underscore/underscore-min.js', callback);
        this.loadScript('../uni/resources/js/moment/min/moment.min.js', callback);
    },

    afterLoadingScripts: function (callback) {
        I18n.load(function () {
            callback();
        });
    }

});