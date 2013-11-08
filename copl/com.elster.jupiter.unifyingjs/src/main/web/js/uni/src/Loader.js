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
//        'Uni.controller.Error',
        'Uni.controller.Navigation',
        'Uni.util.I18n'
    ],

    onReady: function (callback) {
        this.loadFont();
        this.loadTooltips();
        this.loadStateManager();
        this.loadRequirements();

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

    loadRequirements: function () {
        Ext.require('Uni.override.CheckboxOverride');
        Ext.require('Uni.override.FieldBaseOverride');
        Ext.require('Uni.override.JsonWriterOverride');
        Ext.require('Uni.override.RestOverride');
        Ext.require('Uni.override.StoreOverride');
    }

});