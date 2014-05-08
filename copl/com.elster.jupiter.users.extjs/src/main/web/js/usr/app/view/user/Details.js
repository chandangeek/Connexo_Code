Ext.define('Usr.view.user.Details', {
    extend: 'Ext.form.Panel',
    frame: true,
    alias: 'widget.userDetails',
    itemId: 'userDetails',
    hidden: true,
    requires: [
        'Usr.store.Users',
        'Usr.model.User'
    ],
    title: 'user',
    tools: [
        {
            xtype: 'button',
            iconCls: 'x-uni-action-iconA',
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
            xtype: 'form',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5
            },
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
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'authenticationName',
                                    fieldLabel: Uni.I18n.translate('user.name', 'USM', 'Name')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'description',
                                    fieldLabel: Uni.I18n.translate('user.description', 'USM', 'Description')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'domain',
                                    fieldLabel: Uni.I18n.translate('user.domain', 'USM', 'Domain')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'roles',
                                    id: 'els_usm_userDetailsRoles',
                                    fieldLabel: Uni.I18n.translate('user.roles', 'USM', 'Roles')
                                },
                                {
                                    xtype: 'component',
                                    margin: '10 0 10 150',
                                    html: '<h3>' + Uni.I18n.translate('user.preferences', 'USM', 'User preferences') + '</h3>'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'language',
                                    fieldLabel: Uni.I18n.translate('user.language', 'USM', 'Language')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'currency',
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
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'createdOn',
                                    fieldLabel: Uni.I18n.translate('user.created', 'USM', 'Created on')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'modifiedOn',
                                    fieldLabel: Uni.I18n.translate('user.modified', 'USM', 'Modified on')
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});

