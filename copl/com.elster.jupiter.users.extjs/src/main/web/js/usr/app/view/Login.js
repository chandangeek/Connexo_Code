Ext.define('Usr.view.Login', {
    extend: 'Ext.container.Viewport',
    alias: 'widget.login',

    requires: [
        'Ext.form.Panel',
        'Ext.Img',
        'Ext.form.Label',
        'Ext.form.field.Text',
        'Ext.button.Button'
    ],

    id: 'usm_elster_login',
    cls:'uni-content-container uni-content-container-default',
    layout: {
        type: 'vbox',
        align: 'center',
        pack: 'center'
    },

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [
                {
                xtype: 'form',
                height: 250,
                width: 400,
                border: 1,
                style: {
                    borderColor: 'lightgray',
                    borderStyle: 'solid'
                },
                itemId: 'contentPanel',
                title: 'Welcome to Jupiter',
                bodyPadding: 10,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        itemId: 'errorContainer',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        border: 1,
                        style: {
                            borderColor: 'red',
                            borderStyle: 'solid'
                        },
                        hidden: true,
                        items: [
                            {
                                xtype: 'image',
                                itemId: 'errorIcon',
                                src: 'resources/images/warning_icon.png',
                                height: 0,
                                width: 50,
                                padding: 10
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'errorLabel',
                                height: 0,
                                width: 290,
                                padding: 10,
                                fieldStyle: {
                                    color: 'red'
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'container',
                                height: 131,
                                maxWidth: 150,
                                width: 342,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch',
                                    pack: 'center',
                                    padding: 20
                                },
                                items: [
                                    {
                                        xtype: 'image',
                                        src: 'resources/images/user_icon.jpg',
                                        height: 115,
                                        width: 115
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                flex: 3,
                                maxWidth: 280,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch',
                                    pack: 'center',
                                    padding: 20
                                },
                                items: [
                                    {
                                        xtype: 'label',
                                        text: 'User name'
                                    },
                                    {
                                        xtype: 'textfield',
                                        fieldLabel: '',
                                        name: 'username',
                                        itemId: 'username',
                                        allowBlank: true
                                    },
                                    {
                                        xtype: 'label',
                                        margins: '10 0 0 0',
                                        text: 'Password'
                                    },
                                    {
                                        xtype: 'textfield',
                                        fieldLabel: '',
                                        name: 'password',
                                        itemId: 'password',
                                        inputType: 'password',
                                        allowBlank: true
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'loginButton',
                                        listeners: {
                                            click: function(){
                                                var username = this.up('container').down('#username').getValue();
                                                var password = this.up('container').down('#password').getValue();
                                                this.fireEvent('signin', this, username, password);
                                            }
                                        },
                                        formBind: true,
                                        margin: '20 0 0 0 ',
                                        maxWidth: 80,
                                        text: 'Login'

                                    }
                                ]
                            }
                        ]
                    }
                ]
                }
            ]

        });

        me.callParent(arguments);
    }

});