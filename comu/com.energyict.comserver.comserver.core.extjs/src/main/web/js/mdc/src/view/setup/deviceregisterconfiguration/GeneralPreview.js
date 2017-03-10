/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterconfiguration.GeneralPreview', {
    extend: 'Ext.panel.Panel',
    itemId: 'device-register-configuration-general-preview',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu'
    ],

    frame: true,

    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'gridPreviewActionMenu',
            menu: {
                xtype: 'deviceRegisterConfigurationActionMenu'
            }
        }
    ]
});


