/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.deviceconfigurationestimationrules.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-configuration-estimation-rules-preview',

    requires: [
        'Mdc.deviceconfigurationestimationrules.view.PreviewForm'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'device-configuration-estimation-rules-preview-form',
                itemId: 'deviceConfigurationEstimationPreviewForm',
                router: me.router,
                margin: '0 0 15 0'
            },
            {
                xtype: 'property-form',
                isEdit: false,

                defaults: {
                    xtype: 'container',
                    layout: 'form',
                    resetButtonHidden: true,
                    labelWidth: 250
                }
            }
        ];

        me.callParent(arguments);
    }
});