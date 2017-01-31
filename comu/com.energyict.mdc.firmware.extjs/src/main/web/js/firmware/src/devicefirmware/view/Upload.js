/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.view.Upload', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-firmware-upload',

    title: null,
    route: null,
    requires: [
        'Fwc.devicefirmware.view.UploadForm',
        'Uni.util.FormErrorMessage'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: me.title,
            items: [
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'property-form',

                    defaults: {
                        labelWidth: 250,
                        width: 400,
                        resetButtonHidden: true
                    }
                },
                {
                    xtype: 'device-firmware-upload-form',
                    router: me.router
                }
            ]
        };

        me.callParent(arguments);
    }
});
