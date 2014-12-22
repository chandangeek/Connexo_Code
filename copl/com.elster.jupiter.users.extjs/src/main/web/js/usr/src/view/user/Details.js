Ext.define('Usr.view.user.Details', {
    extend: 'Ext.form.Panel',
    alias: 'widget.userDetails',
    itemId: 'userDetails',
    frame: true,
    hidden: true,
    requires: [
        'Usr.store.Users',
        'Usr.model.User',
        'Ext.button.Button',
        'Usr.view.user.UserActionMenu'
    ],
    title: 'user',


    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.userAndRole'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'user-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'form',
            itemId: 'userDetailsForm',
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
                            defaults: {
                                labelWidth: 150
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'authenticationName',
                                    fieldLabel: Uni.I18n.translate('user.name', 'USR', 'Name')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'description',
                                    fieldLabel: Uni.I18n.translate('user.description', 'USR', 'Description')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'domain',
                                    fieldLabel: Uni.I18n.translate('user.domain', 'USR', 'Domain')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'roles',
                                    fieldLabel: Uni.I18n.translate('user.roles', 'USR', 'Roles')
                                },
                                {
                                    xtype: 'component',
                                    margin: '10 0 10 150',
                                    html: '<h3>' + Uni.I18n.translate('user.preferences', 'USR', 'User preferences') + '</h3>'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'language',
                                    fieldLabel: Uni.I18n.translate('user.language', 'USR', 'Language')
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
                                labelWidth: 150
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'createdOn',
                                    fieldLabel: Uni.I18n.translate('user.created', 'USR', 'Created on')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'modifiedOn',
                                    fieldLabel: Uni.I18n.translate('user.modified', 'USR', 'Modified on')
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});

