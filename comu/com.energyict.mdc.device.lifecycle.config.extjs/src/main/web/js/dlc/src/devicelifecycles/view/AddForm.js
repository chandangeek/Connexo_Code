/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycles.view.AddForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-life-cycles-add-form',
    required: [
        'Uni.util.FormInfoMessage',
        'Uni.util.FormErrorMessage'
    ],
    router: null,
    infoText: null,
    btnAction: null,
    btnText: null,
    route: null,
    hideInfoMsg: false,
    itemId: 'device-life-cycles-add-form',
    ui: 'large',
    width: '100%',
    defaults: {
        labelWidth: 250
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-info-message',
                itemId: 'info-message',
                text: me.infoText,
                margin: '0 0 20 0',
                hidden: me.hideInfoMsg,
                width: 800
            },
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                name: 'form-errors',
                margin: '0 0 20 20',
                hidden: true,
                width: 800
            },
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'device-life-cycle-name',
                width: 500,
                required: true,
                fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                allowBlank: false,
                enforceMaxLength: true,
                maxLength: 80,
                listeners: {
                    afterrender: function(field) {
                        field.focus(false, 200);
                    }
                }
            },
            {
                xtype: 'fieldcontainer',
                ui: 'actions',
                fieldLabel: '&nbsp',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'actionBtnContainer',
                        text: me.btnText,
                        ui: 'action',
                        action: me.btnAction
                    },
                    {
                        xtype: 'button',
                        itemId: 'cancel-link',
                        text: Uni.I18n.translate('general.cancel', 'DLC', 'Cancel'),
                        ui: 'link',
                        href: me.router.getRoute(me.route).buildUrl()
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});