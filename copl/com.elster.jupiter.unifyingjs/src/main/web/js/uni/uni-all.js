/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Adds the UnifyingJS config to the ExtJS loader.
 */
(function () {
    Ext.Loader.setConfig({
        enabled: true,
        disableCaching: true,
        paths: {
            'Ldr': '../ldr/src',
            'Uni': '../uni/src',
            'Ext.ux.form': '../uni/packages/Ext.ux.form',
            'Ext.ux.Rixo': '../uni/packages/Ext.ux.Rixo',
            'Ext.ux.window': '../uni/packages/Ext.ux.window.Notification',
            'Ext.ux.exporter': '../uni/packages/Ext.ux.Exporter'
        }
    });
}());