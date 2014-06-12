Ext.define('Usr.view.Login', {
    extend: 'Ext.container.Viewport',
    xtype: 'login',
    itemId: 'login',

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
                            inputType: 'password'
                        }
                    ],
                    buttons: [
                        {
                            ui: 'action',
                            formBind: true,
                            type: 'submit',
                            action: 'login',
                            itemId: 'loginButton',
                            text: 'Login',
                            margin: '0 0 0 10'
                        }
                    ]
                }
            ]
        }
    ]
});