/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskAddActionForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.comtaskAddActionForm',
    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.view.setup.comtasks.ComtaskCommandCategoryCombo'
    ],
    router: null,
    cancelRoute: null,
    btnAction: null,
    btnText: null,
    ui: 'large',
    width: '100%',
    defaults: {
        labelWidth: 300
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                name: 'form-errors',
                margin: '0 0 20 20',
                hidden: true,
                width: 800
            },
            {
                xtype: 'comtaskCommandCategoryCombo',
                width: 570, // = width of combobox = 256
                itemId: 'mdc-comtask-addAction-command-category-combo'
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'mdc-comtask-addAction-parameters-label',
                fieldLabel: '&nbsp',
                labelWidth: 0,
                hidden: true,
                items: [
                    {
                        xtype: 'label',
                        text: Uni.I18n.translate('general.attributes', 'MDC', 'Attributes')
                    }
                ]
            },
            {
                xtype: 'container',
                margin: '0 0 30 315',
                itemId: 'mdc-comtask-addAction-parameter-error-message',
                hidden: true
            },
            {
                xtype: 'fieldcontainer',
                ui: 'actions',
                fieldLabel: '&nbsp',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'mdc-comtask-addAction-actionBtn',
                        text: me.btnText,
                        ui: 'action',
                        action: me.btnAction,
                        disabled: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'mdc-comtask-addAction-cancel-link',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        ui: 'link',
                        href: me.router.getRoute(me.cancelRoute).buildUrl()
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});