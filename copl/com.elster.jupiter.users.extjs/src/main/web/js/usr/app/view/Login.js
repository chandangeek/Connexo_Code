Ext.define('Usr.view.Login', {
    extend: 'Ext.container.Viewport',
    alias: 'widget.login',
    itemId: 'login',

    requires: [
        'Ext.form.Panel',
        'Ext.Img',
        'Ext.form.Label',
        'Ext.form.field.Text',
        'Ext.button.Button'
    ],

    //cls:'uni-content-container uni-content-container-default',

    layout: 'fit',
    items: {
        xtype: 'panel',
        ui: 'login-container',
        layout: {
            type: 'vbox',
            align: 'center',
            pack: 'center'
        },
        items: {
            xtype: 'panel',
            ui: 'login-panel',
//            height: 330,
            width: 500,
            itemId: 'contentPanel',
            layout: {
                type: 'vbox',
                align: 'stretch',
                pack: 'center'
            },
            items: [
                {
                    layout: {
                        type: 'hbox',
                        pack: 'center'
                    },
                    items: {
                        xtype: 'image',
                        align: 'center',
                        src: '/apps/usr/resources/images/connexo.png',
                        width: 248,
                        height: 46,
                        margin: '0 0 24'
                    }
                },
                {
                    xtype: 'form',
                    itemId: 'login-form',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'displayfield',
                            itemId: 'errorLabel',
                            hidden: true,
                            fieldStyle: {
                                color: 'orangered'
                            },
                            margin: '0 0 8 116'
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: 'User name',
                            name: 'username',
                            itemId: 'username',
                            allowBlank: true
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: 'Password',
                            name: 'password',
                            itemId: 'password',
                            inputType: 'password',
                            allowBlank: true
                        }
                    ],
                    buttons: [
                        {
                            ui: 'action',
                            formBind: true,
                            type: 'submit',
                            action: 'login',
                            itemId: 'loginButton',
                            text: 'Login'
                        }
                    ]
                }
            ]
        }
    }
});