/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.userDirectory.Synchronize', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usr-user-directories-synchronize',
    overflowY: true,

    requires: [
        'Uni.util.FormErrorMessage',
        'Usr.store.MgmUserDirectoryUsers'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            items: [
                {
                    xtype: 'form',
                    itemId: 'frm-user-directory-users',
                    width: 1200,
                    ui: 'large',
                    width: '100%',
                    layout: {
                        type: 'vbox'
                    },
                    defaults: {
                        padding: '10 0 0 10'
                    },
                    items: [
                        {
                            xtype: 'panel',
                            width: 780,
                            items: [
                                {
                                   xtype: 'gridpanel',
                                    itemId: 'grd-user-directory-users',
                                    store: 'Usr.store.MgmUserDirectoryUsers',
                                    //scroll: 'vertical',
                                    //hideHeaders: true,
                                    padding: 0,
                                    width: 650,
                                    columns: [
                                        {
                                            header: Uni.I18n.translate('general.userName', 'USR', 'User name'),
                                            dataIndex: 'name',
                                            flex: 3
                                        },
                                        {
                                            header: Uni.I18n.translate('userDirectories.userStatus.status', 'USR', 'Status'),
                                            dataIndex: 'status',
                                            renderer: function (value, metaData, record) {
                                                return record.get('statusDisplay');
                                            },
                                            flex: 2
                                        },
                                        {
                                            xtype: 'actioncolumn',
                                            align: 'right',
                                            items: [
                                                {
                                                    iconCls: 'uni-icon-delete',
                                                    handler: function (grid, rowIndex) {
                                                        grid.getStore().removeAt(rowIndex);
                                                    },
                                                    getClass: function(v, meta, rec) {
                                                        if(Ext.isDefined(rec.index)) {
                                                            return 'x-hide-display';
                                                        }
                                                        return 'uni-icon-delete';
                                                    }
                                                }
                                            ],
                                            flex: 1
                                        }
                                    ],
                                    height: 210
                                }
                            ],
                            rbar: [
                                {
                                    xtype: 'container',
                                    layout: {
                                        type: 'vbox'
                                    },
                                    defaults: {
                                        margin: '8 0 0 0'
                                    },
                                    items: [
                                        {
                                            xtype: 'button',
                                            itemId: 'btn-user-directory-add-users',
                                            text: Uni.I18n.translate('userDirectories.selectUsers', 'USR', 'Select users')
                                        },
                                        {
                                            xtype: 'button',
                                            itemId: 'btn-user-directory-synchronize-users',
                                            text: Uni.I18n.translate('userDirectories.synchronizeUsers', 'USR', 'Synchronize')
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            layout: 'hbox',
                            items: [
                                {
                                    text: Uni.I18n.translate('general.save', 'USR', 'Save'),
                                    xtype: 'button',
                                    ui: 'action',
                                    action: 'saveUsersAction',
                                    itemId: 'btn-save-user'
                                },
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.cancel', 'USR', 'Cancel'),
                                    href: '#/administration/userdirectories',
                                    itemId: 'btn-cancel-save-user',
                                    ui: 'link'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

