/**
 * Loader class for the Unifying JS project, it makes sure every dependency is
 * ready to use.
 *
 * @note
 */
Ext.define('Uni.Loader', {

    requires: [
        'Ext.tip.QuickTipManager',
        'Ext.state.CookieProvider'
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
        Ext.require('Uni.util.CheckboxOverride');
        Ext.require('Uni.util.FieldBaseOverride');
        Ext.require('Uni.util.JsonWriterOverride');
        Ext.require('Uni.util.StoreOverride');
    }

});