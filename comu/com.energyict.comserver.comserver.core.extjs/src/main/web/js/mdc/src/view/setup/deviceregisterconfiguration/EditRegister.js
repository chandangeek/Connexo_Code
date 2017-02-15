/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterconfiguration.EditRegister', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.EditRegisterForm'
    ],
    alias: 'widget.device-register-edit',
    device: null,
    returnLink: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'registersLink'
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'device-register-edit-form',
                itemId: 'mdc-device-register-edit-form',
                title: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                returnLink: me.returnLink
            }
        ];
        me.callParent(arguments);
    },

    setRegister: function(registerRecord) {
        var me = this;
        if (me.rendered) {
            me.down('#mdc-device-register-edit-form').setRegister(registerRecord);
        } else {
            me.on('afterrender', function() {
                me.down('#mdc-device-register-edit-form').setRegister(registerRecord);
            }, me, {single:true});
        }
    }
});