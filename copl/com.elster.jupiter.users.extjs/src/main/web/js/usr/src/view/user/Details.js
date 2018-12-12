/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.user.Details', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.userDetails',
    itemId: 'userDetails',
    frame: true,
    requires: [
        'Usr.store.Users',
        'Usr.model.User',
        'Ext.button.Button',
        'Usr.view.user.UserActionMenu'
    ],
    title: Uni.I18n.translate('users.user','USR','User'),


    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Usr.privileges.Users.adminUsers,
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
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'authenticationName',
                                    fieldLabel: Uni.I18n.translate('general.name', 'USR', 'Name')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'description',
                                    fieldLabel: Uni.I18n.translate('general.description', 'USR', 'Description')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'domain',
                                    fieldLabel: Uni.I18n.translate('user.userdirectory', 'USR', 'User directory')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'statusDisplay',
                                    fieldLabel: Uni.I18n.translate('user.status', 'USR', 'Status')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'roles',
                                    fieldLabel: Uni.I18n.translate('general.roles', 'USR', 'Roles'),
                                    htmlEncode: false
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'createdOn',
                                    fieldLabel: Uni.I18n.translate('general.createdOn', 'USR', 'Created on'),
                                    renderer: function (value) {
                                        if (value) {
                                            var createdOnDate = moment(value).toDate();
                                            if (createdOnDate instanceof Date && !isNaN(createdOnDate.valueOf())) {
                                                return Uni.DateTime.formatDateTimeLong(createdOnDate);
                                            }
                                        } else {
                                            return '-';
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'modifiedOn',
                                    fieldLabel: Uni.I18n.translate('general.modifiedOn', 'USR', 'Modified on'),
                                    renderer: function (value) {
                                        if (value) {
                                            var modifiedOnDate = moment(value).toDate();
                                            if (modifiedOnDate instanceof Date && !isNaN(modifiedOnDate.valueOf())) {
                                                return Uni.DateTime.formatDateTimeLong(modifiedOnDate);
                                            }
                                        } else {
                                            return '-';
                                        }
                                    }
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
                                    xtype: 'fieldcontainer',
                                    fieldLabel: '<h3>' + Uni.I18n.translate('user.preferences', 'USR', 'User preferences') + '</h3>',
                                    labelAlign: 'top',
                                    layout: 'vbox',
                                    defaults: {
                                        xtype: 'displayfield',
                                        labelWidth: 250
                                    },
                                    items: [
                                        {
                                            xtype: 'displayfield',
                                            name: 'language',
                                            fieldLabel: Uni.I18n.translate('user.language', 'USR', 'Language'),
                                            renderer: function (value) {
                                                return value && value.displayValue ? Ext.String.htmlEncode(value.displayValue) : '-';
                                            }
                                        }
                                    ]
                                },
                                {
                                    xtype: 'fieldcontainer',
                                    fieldLabel: '<h3>' + Uni.I18n.translate('user.activity.section', 'USR', 'Recent activity') + '</h3>',
                                    labelAlign: 'top',
                                    layout: 'vbox',
                                    defaults: {
                                        xtype: 'displayfield',
                                        labelWidth: 250
                                    },
                                    items: [
                                        {
                                            xtype: 'displayfield',
                                            name: 'lastSuccessfulLogin',
                                            fieldLabel: Uni.I18n.translate('general.lastSuccessfulLogin', 'USR', 'Last successful login'),
                                            renderer: function (value) {
                                                if (value) {
                                                    var lastSuccessfulLogin = moment(value).toDate();
                                                    if (lastSuccessfulLogin instanceof Date && !isNaN(lastSuccessfulLogin.valueOf())) {
                                                        return Uni.DateTime.formatDateTimeLong(lastSuccessfulLogin);
                                                    }
                                                } else {
                                                    return '-';
                                                }
                                            }
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'lastUnSuccessfulLogin',
                                            fieldLabel: Uni.I18n.translate('general.lastUnSuccessfulLogin', 'USR', 'Last unsuccessful login'),
                                            renderer: function (value) {
                                                if (value) {
                                                    var lastUnSuccessfulLogin = moment(value).toDate();
                                                    if (lastUnSuccessfulLogin instanceof Date && !isNaN(lastUnSuccessfulLogin.valueOf())) {
                                                        return Uni.DateTime.formatDateTimeLong(lastUnSuccessfulLogin);
                                                    }
                                                } else {
                                                    return '-';
                                                }
                                            }
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});

