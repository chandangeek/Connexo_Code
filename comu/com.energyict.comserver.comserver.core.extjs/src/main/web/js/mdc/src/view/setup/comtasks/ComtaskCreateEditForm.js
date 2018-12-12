/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskCreateEditForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comtaskCreateEdit',
    requires: [
        'Uni.util.FormErrorMessage'
    ],

    content: [
        {
            xtype: 'form',
            ui: 'large',
            defaults: {
                labelWidth: 200
            },
            items: [
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'errors',
                    hidden: true,
                    width: 380

                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    itemId: 'addComtaskName',
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    required: true,
                    width: 500,
                    listeners: {
                        afterrender: function(field) {
                            field.focus(false, 200);
                        }
                    }
                },
                {
                    xtype: 'toolbar',
                    margin: '0 0 0 215',
                    items: [
                        {
                            xtype: 'button',
                            name: 'action',
                            ui: 'action',
                            itemId: 'createEditTask'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancelLink',
                            text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                            action: 'cancel',
                            ui: 'link'
                        }
                    ]
                }
            ]
        }
    ]
});