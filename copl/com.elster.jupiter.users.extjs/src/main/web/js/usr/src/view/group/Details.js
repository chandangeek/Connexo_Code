Ext.define('Usr.view.group.Details', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.groupDetails',
    itemId: 'groupDetails',
    frame: true,
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
    title: Uni.I18n.translate('users.group','USR','Group'),

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'USR', 'Actions'),
            privileges: Usr.privileges.Users.admin,
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
                                    fieldLabel: Uni.I18n.translate('role.name', 'USR', 'Role name')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'description',
                                    fieldLabel: Uni.I18n.translate('general.description', 'USR', 'Description')
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
                                    fieldLabel: Uni.I18n.translate('general.createdOn', 'USR', 'Created on'),
                                    renderer: function (value) {
                                        if (value) {
                                            var createdOnDate = moment(value).toDate();
                                            if (createdOnDate instanceof Date && !isNaN(createdOnDate.valueOf())) {
                                                return Uni.DateTime.formatDateTimeLong(createdOnDate);
                                            }
                                        } else {
                                            return '';
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
                                            return '';
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
});

