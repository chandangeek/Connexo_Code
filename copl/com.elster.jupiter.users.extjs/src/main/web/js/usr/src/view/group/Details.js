Ext.define('Usr.view.group.Details', {
    extend: 'Ext.form.Panel',
    alias: 'widget.groupDetails',
    itemId: 'groupDetails',
    frame: true,
    hidden: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    requires: [
        'Usr.store.Users',
        'Usr.model.User',
        'Ext.button.Button',
        'Usr.view.group.GroupActionMenu'
    ],
    title: 'Group',

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.userAndRole'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'group-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'form',
            itemId: 'groupDetailsForm',
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
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('group.name', 'USR', 'Role name')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'description',
                                    fieldLabel: Uni.I18n.translate('group.description', 'USR', 'Description')
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
                                    fieldLabel: Uni.I18n.translate('group.created', 'USR', 'Created on'),
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '';
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'modifiedOn',
                                    fieldLabel: Uni.I18n.translate('group.modified', 'USR', 'Modified on'),
                                    renderer: function (value) {
                                        return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '';
                                    }
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});

