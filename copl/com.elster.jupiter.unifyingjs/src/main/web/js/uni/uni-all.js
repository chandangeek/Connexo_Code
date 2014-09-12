/**
 * Adds the UnifyingJS config to the ExtJS loader.
 */
(function () {
    Ext.Loader.setConfig({
        enabled: true,
        disableCaching: true,
        paths: {
            'Uni': '../uni/src',
            'Ext.ux.form': '../uni/packages/Ext.ux.form',
            'Ext.ux.Rixo': '../uni/packages/Ext.ux.Rixo',
            'Ext.ux.window': '../uni/packages/Ext.ux.window.Notification'
        }
    });
}());