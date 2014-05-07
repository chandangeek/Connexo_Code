/**
 * Adds the Unifying JS config to the Ext JS loader.
 * When changing, also change the uni-dev.js
 */
(function () {
    Ext.Loader.setConfig({
        enabled: true,
        disableCaching: false,
        paths: {
            'Ext.ux.window': '../uni/packages/Ext.ux.window.Notification',
            'Uni': '../uni/src'
        }
    });
}());