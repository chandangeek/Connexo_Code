/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Login.view.Viewport', {
    extend: 'Ext.container.Viewport',
    xtype: 'login',

    requires: [
        'Ext.form.Panel',
        'Ext.Img',
        'Ext.form.Label',
        'Ext.form.field.Text',
        'Ext.button.Button'
    ],

    layout: {
        type: 'vbox',
        align: 'center',
        pack: 'center'
    },

    items: [
        {
            xtype: 'panel',
            ui: 'login',
            minHeight: 330,
            width: 500,
            itemId: 'contentPanel',

            layout: {
                type: 'vbox',
                align: 'stretch',
                pack: 'center'
            },

            items: [
                {
                    xtype: 'container',
                    ui: 'logo'
                },
                {
                    xtype: 'form',
                    itemId: 'login-form',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    buttonAlign:'left',
                    defaults: {
                        xtype: 'textfield',
                        labelSeparator: '',
                        allowBlank: true
                    },
                    items: [
                        {
                            xtype: 'displayfield',
                            ui: 'error',
                            cls: 'hidden',
                            itemId: 'errorLabel',
                            hidden: true,
                            value: 'Login failed. Please contact your administrator.',
                            margin: '0 0 8 116'
                        },
                        {
                            fieldLabel: 'Username',
                            name: 'username',
                            itemId: 'username',
                            labelAlign: 'right',
                            listeners: {
                                afterrender: function (field) {
                                    field.focus(false, 200);
                                }
                            }
                        },
                        {
                            fieldLabel: 'Password',
                            name: 'password',
                            itemId: 'password',
                            inputType: 'password',
                            labelAlign: 'right'
                        },
                        {
                            xtype: 'fieldcontainer',
//                            ui: 'actions',
                            fieldLabel: '&nbsp',
                            layout: {
                                type: 'hbox'
                            },
                            items: [
                                {
                                    text: 'Login',
                                    xtype: 'button',
                                    ui: 'action',
                                    action: 'login',
                                    itemId: 'loginButton'
                                }
                            ]
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'container',

            defaults: {
                xtype: 'button',
                margin: '50 0 0 0'
            },
            maxWidth: 500,
            items: [
                {
                    itemId: 'contactsupport',
                    ui: 'link',
                    text: 'Contact Support',
                    handler:function(){
                        window.location.href = "mailto:support.energyict@elster.com";
                    }
                },
                {
                    itemId: 'about',
                    ui: 'link',
                    href: '/apps/login/resources/about.html',
                    text: 'About'
                }
            ]

        }
    ]

});