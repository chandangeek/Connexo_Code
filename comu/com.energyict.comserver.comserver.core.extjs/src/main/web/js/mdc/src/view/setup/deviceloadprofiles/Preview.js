/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceloadprofiles.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfilesPreview',
    itemId: 'deviceLoadProfilesPreview',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.PreviewForm',
        'Mdc.view.setup.deviceloadprofiles.ActionMenu'
    ],
    layout: 'fit',
    frame: true,
    router: null,

    deviceId: null,

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'deviceLoadProfilesActionMenu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'deviceLoadProfilesPreviewForm',
            deviceId: me.deviceId,
            router: me.router
        };

        me.callParent(arguments);
    }
});
