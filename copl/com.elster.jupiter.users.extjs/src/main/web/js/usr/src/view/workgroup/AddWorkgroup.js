/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.workgroup.AddWorkgroup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usr-add-workgroup',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.grid.column.RemoveAction'
    ],

    edit: false,
    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#btn-add').setText(Uni.I18n.translate('general.save', 'USR', 'Save'));
            this.down('#btn-add').action = 'edit';
        } else {
            this.edit = edit;
            this.down('#btn-add').setText(Uni.I18n.translate('general.add', 'USR', 'Add'));
            this.down('#btn-add').action = 'add';
        }
        this.down('#btn-cancel-link').href = returnLink;
    },

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                itemId: 'frm-add-workgroup',
                ui: 'large',
                defaults: {
                    labelWidth: 250,
                    width: 600,
                    enforceMaxLength: true
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        width: 400,
                        margin: '0 0 10 0',
                        hidden: true
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'txt-name',
                        required: true,
                        maxLength: 80,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('general.name', 'USR', 'Name'),
                        listeners: {
                            afterrender: function (field) {
                                if (!me.edit) {
                                    field.focus(false, 200);
                                }
                            }
                        }
                    },
                    {
                        xtype: 'textfield',
                        name: 'description',
                        maxLength: 80,
                        itemId: 'txt-description',
                        fieldLabel: Uni.I18n.translate('workgroups.description', 'USR', 'Description')
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'cnt-users',
                        fieldLabel: Uni.I18n.translate('workgroups.users', 'USR', 'Users'),
                        layout: 'hbox',
                        labelWidth: 250,
                        width: 1100,
                        items: [
                            {
                                xtype: 'component',
                                html: Uni.I18n.translate('workgroups.noUsersAdded', 'USR', 'No users have been added'),
                                itemId: 'cpn-no-users',
                                style: {
                                    'font': 'italic 13px/17px Lato',
                                    'color': '#686868',
                                    'margin-top': '6px',
                                    'margin-right': '10px'
                                }
                            },
                            {
                                xtype: 'gridpanel',
                                itemId: 'grd-users',
                                hidden: true,
                                hideHeaders: true,
                                padding: 0,
                                scroll: 'vertical',
                                viewConfig: {
                                    disableSelection: true
                                },
                                columns: [
                                    {
                                        dataIndex: 'name',
                                        flex: 1
                                    },
                                    {
                                        xtype: 'uni-actioncolumn-remove',
                                        align: 'right',
                                        handler: function (grid, rowIndex) {
                                            grid.getStore().removeAt(rowIndex);
                                            if (grid.getStore().count() === 0) {
                                                me.updateGrid();
                                            }
                                        }
                                    }
                                ],
                                maxHeight: 292,
                                width: 335,
                                autoHeight: true

                            },
                            {
                                xtype: 'button',
                                itemId: 'btn-add-users',
                                text: Uni.I18n.translate('workgroups.selectUsers', 'USR', 'Select users'),
                                margin: '0 0 0 10'
                            }
                        ]
                    },

                    {
                        xtype: 'container',
                        margin: '0 0 0 265',
                        layout: 'hbox',
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'USR', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                itemId: 'btn-add'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'USR', 'Cancel'),
                                xtype: 'button',
                                //href: '#/administration/workgroups',
                                itemId: 'btn-cancel-link',
                                ui: 'link'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
        me.setEdit(me.edit, me.returnLink);
    },

    updateGrid: function () {
        var me = this,
            count = me.down('#grd-users').getStore().count();

        me.down('#grd-users').setVisible(count != 0);
        me.down('#cpn-no-users').setVisible(count == 0);
    }

});

