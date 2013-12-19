/**
 * Loader class for the Unifying JS project, it makes sure every dependency is
 * ready to use.
 *
 * @note
 */
Ext.define('Uni.Loader', {

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

    onReady: function (callback) {
        this.loadFont();
        this.loadTooltips();
        this.loadStateManager();
        this.loadOverrides();
        this.loadStores();

        callback.call();
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
    }

});