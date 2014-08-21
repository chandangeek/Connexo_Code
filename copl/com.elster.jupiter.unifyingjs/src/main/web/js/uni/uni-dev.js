/**
 * Adds the Unifying JS config to the Ext JS loader.
 * When changing, also change uni-all.js
 */
(function () {
    Ext.Loader.setConfig({
        enabled: true,
        disableCaching: true,
        paths: {
            'Ext.ux.window': '../uni/packages/Ext.ux.window.Notification',
            'Ext.ux': '../uni/packages/ux',
            'Uni': '../uni/src',
            'Uni.property': '../uni/packages/property/src'
        }
    });
}());