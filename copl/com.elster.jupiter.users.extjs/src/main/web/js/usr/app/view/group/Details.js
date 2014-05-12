Ext.define('Usr.view.group.Details', {
    extend: 'Ext.form.Panel',
    alias: 'widget.groupDetails',
    itemId: 'groupDetails',
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
        'Usr.model.User',
        'Ext.layout.container.Column'
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
                        id: 'els_usm_groupDetailsHeader'
                    },
                    '->',
                    {
                        icon: '../usr/resources/images/gear-16x16.png',
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
                                        id: 'els_usm_groupDetailsPrivileges',
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
                                    labelWidth: 250
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
        ];

        this.callParent(arguments);
    }
});

