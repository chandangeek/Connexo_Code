/**
 * Loader class for the Unifying JS project, it makes sure every dependency is
 * ready to use.
 *
 * @note
 */
Ext.define('Uni.Loader', {

    requires: [
        'Ext.tip.QuickTipManager',
        'Ext.state.CookieProvider',
        'Uni.util.I18n'
    ],

    onReady: function (callback) {
        this.loadFont();
        this.loadTooltips();
        this.loadStateManager();
        this.loadOverrides();

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
        Ext.require('Uni.override.JsonWriterOverride');
        Ext.require('Uni.override.RestOverride');
        Ext.require('Uni.override.StoreOverride');
    }

});