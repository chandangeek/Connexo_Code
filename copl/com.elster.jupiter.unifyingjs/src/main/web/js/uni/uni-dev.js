/**
 * Adds the Unifying JS config to the Ext JS loader.
 */
(function () {
    Ext.Loader.setConfig({
        enabled: true,
        disableCaching: true,
        paths: {
            'Uni': '../uni/src'
        }
    });
}());