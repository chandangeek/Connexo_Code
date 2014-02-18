/**
 * Adds the Unifying JS config to the Ext JS loader.
 */
(function () {
    Ext.Loader.setConfig({
        enabled: true,
        disableCaching: true,
        paths: {
            'Ext.ux.window': '../uni/packages/Ext.ux.window.Notification',
            'Uni': '../uni/src'
        }
    });
}());