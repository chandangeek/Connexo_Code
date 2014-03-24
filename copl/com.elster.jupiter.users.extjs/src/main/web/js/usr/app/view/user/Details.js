Ext.define('Usr.view.user.Details', {
    extend: 'Ext.form.Panel',
    alias: 'widget.userDetails',
    itemId: 'userDetails',
    hidden: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    //width: 600,
    //height: 450,
    //constrain: true,
    requires: [
        'Usr.store.Users',
        'Usr.model.User'
    ],

    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                border: false,
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    anchor: '100%',
                    margins: '0 0 5 0'
                },

                tbar: [
                    {
                        xtype: 'component',
                        html: '<h4></h4>',
                        id: 'els_usm_userDetailsHeader'
                    },
                    '->',
                    {
                        icon: '../usr/resources/images/gear-16x16.png',
                        text: Uni.I18n.translate('general.actions', 'USM', 'Actions'),
                        menu: {
                            items: [
                                {
                                    text: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
                                    itemId: 'editUser',
                                    action: 'editUser'

                                }
                            ]
                        }
                    }
                ],

                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'authenticationName',
                                        readOnly: true,
                                        fieldLabel: Uni.I18n.translate('user.name', 'USM', 'Name')
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'description',
                                        readOnly: true,
                                        fieldLabel: Uni.I18n.translate('user.description', 'USM', 'Description')
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'domain',
                                        readOnly: true,
                                        fieldLabel: Uni.I18n.translate('user.domain', 'USM', 'Domain')
                                    },
                                    {
                                        xtype: 'textareafield',
                                        name: 'roles',
                                        readOnly: true,
                                        id: 'els_usm_userDetailsRoles',
                                        fieldLabel: Uni.I18n.translate('user.roles', 'USM', 'Roles')
                                    },
                                    {
                                        xtype: 'component',
                                        margin: '0 0 5 150',
                                        html: '<h3>' + Uni.I18n.translate('user.preferences', 'USM', 'User preferences') + '</h3>'
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'language',
                                        readOnly: true,
                                        fieldLabel: Uni.I18n.translate('user.language', 'USM', 'Language')
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'currency',
                                        readOnly: true,
                                        fieldLabel: Uni.I18n.translate('user.currency', 'USM', 'Currency')
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'createdOn',
                                        readOnly: true,
                                        fieldLabel: Uni.I18n.translate('user.created', 'USM', 'Created on')
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'modifiedOn',
                                        readOnly: true,
                                        fieldLabel: Uni.I18n.translate('user.modified', 'USM', 'Modified on')
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});

