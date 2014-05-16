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
        'Ext.button.Button'
    ],
    title: 'Group',

    tools: [
        {
            xtype: 'button',
            iconCls: 'x-uni-action-iconA',
            text: Uni.I18n.translate('general.actions', 'USM', 'Actions'),
            menu: {
                items: [
                    {
                        text: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
                        itemId: 'editGroup',
                        action: 'editGroup'
                    }
                ]
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
                                    fieldLabel: Uni.I18n.translate('group.name', 'USM', 'Role name')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'description',
                                    fieldLabel: Uni.I18n.translate('group.description', 'USM', 'Description')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'privileges',
                                    fieldLabel: Uni.I18n.translate('group.privileges', 'USM', 'Privileges')
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
                                    fieldLabel: Uni.I18n.translate('group.created', 'USM', 'Created on')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'modifiedOn',
                                    fieldLabel: Uni.I18n.translate('group.modified', 'USM', 'Modified on')
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});

