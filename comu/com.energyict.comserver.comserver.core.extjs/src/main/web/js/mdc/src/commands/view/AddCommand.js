/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.AddCommand', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-command',

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.addCommand', 'MDC', 'Add command'),
                items: [
                ]
            },
            {
                xtype: 'container',
                layout: {
                    type: 'hbox'
                },
                items: [
                    {
                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                        itemId: 'add-custom-attribute-sets-grid-add',
                        xtype: 'button',
                        ui: 'action',
                        disabled: true
                    },
                    {
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        itemId: 'mdc-add-command-cancel',
                        xtype: 'button',
                        ui: 'link'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }

});