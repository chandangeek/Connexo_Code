/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.AddEditTrustStore', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.truststore-add',
    requires: [
        'Uni.util.FormErrorMessage'
    ],

    action: 'add',

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                layout: 'vbox',
                title: this.action === 'edit'
                    ? Uni.I18n.translate('general.editTrustStore', 'PKJ', 'Edit trust store')
                    : Uni.I18n.translate('general.addTrustStore', 'PKJ', 'Add trust store'),
                items: [
                    {
                        xtype: 'form',
                        //width: 650,
                        itemId: 'addForm',
                        buttonAlign: 'left',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250,
                            width: 521
                        },
                        items: [
                            {
                                xtype: 'uni-form-error-message',
                                itemId: 'pkj-add-truststore-error-form',
                                name: 'form-errors',
                                margin: '0 0 10 0',
                                hidden: true,
                                width: 750
                            },
                            {
                                xtype: 'textfield',
                                name: 'name',
                                itemId: 'pkj-add-truststore-name',
                                fieldLabel: Uni.I18n.translate('general.name', 'PKJ', 'Name'),
                                allowBlank: false,
                                required: true,
                                listeners: {
                                    afterrender: function(field) {
                                        field.focus(false, 200);
                                    }
                                }
                            },
                            {
                                xtype: 'textareafield',
                                name: 'description',
                                itemId: 'pkj-edd-truststore-description',
                                fieldLabel: Uni.I18n.translate('general.description', 'PKJ', 'Description'),
                                height: 128
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'form-buttons',
                                fieldLabel: '&nbsp;',
                                layout: 'hbox',
                                margin: '20 0 0 0',
                                items: [
                                    {
                                        xtype: 'button',
                                        itemId: 'pkj-add-truststore-add-btn',
                                        text: this.action === 'edit' ? Uni.I18n.translate('general.save', 'PKJ', 'Save') : Uni.I18n.translate('general.add', 'PKJ', 'Add'),
                                        ui: 'action',
                                        action: me.action
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'pkj-add-truststore-cancel-link',
                                        text: Uni.I18n.translate('general.cancel', 'PKJ', 'Cancel'),
                                        ui: 'link',
                                        action: 'cancel',
                                        href: me.returnLink
                                    }
                                ]
                            }

                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});