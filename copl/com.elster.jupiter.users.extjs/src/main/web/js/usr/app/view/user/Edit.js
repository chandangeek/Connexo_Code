Ext.define('Usr.view.user.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.userEdit',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    width: 600,
    height: 450,
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
        'Ext.grid.*',
        'Ext.util.Point', // Required for the drag and drop.
        'Usr.store.Groups',
        'Usr.model.Group'
    ],

    initComponent: function () {
        var columns = [
            {
                xtype: 'checkcolumn',
                dataIndex: 'selected',
                sortable: false,
                hideable: false,
                flex: 0.1
            },
            {
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                flex: 2
            }
        ];

        this.content = [
            {
                xtype: 'container',
                cls: 'content-container',
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    //anchor: '100%',
                    margins: '0 0 10 0'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<h1></h1>',
                        id: 'els_usm_userEditHeader'
                    },
                    {
                        xtype: 'form',
                        id: 'els_usm_userEditForm',
                        border: false,
                        //padding: '10 10 0 10',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        //defaults: {
                        //    anchor: '100%',
                        //    margins: '0 0 5 0'
                        //},

                        items: [
                            {
                                xtype: 'textfield',
                                name: 'authenticationName',
                                fieldLabel: Uni.I18n.translate('user.name', 'USM', 'Name'),
                                readOnly: true,
                                disabled: true,
                                labelWidth: 100,
                                labelPad: 0,
                                maxWidth: 500
                            },
                            {
                                xtype: 'textfield',
                                name: 'description',
                                fieldLabel: Uni.I18n.translate('user.description', 'USM', 'Description'),
                                disabled: true,
                                labelWidth: 100,
                                labelPad: 0,
                                maxWidth: 500
                            },
                            {
                                xtype: 'textfield',
                                name: 'domain',
                                fieldLabel: Uni.I18n.translate('user.domain', 'USM', 'Domain'),
                                readOnly: true,
                                disabled: true,
                                labelWidth: 100,
                                labelPad: 0,
                                maxWidth: 500
                            },
                            {
                                xtype: 'container',
                                maxWidth: 500,
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        width: 100,
                                        fieldLabel: Uni.I18n.translate('user.roles', 'USM', 'Roles')
                                    },
                                    {
                                        xtype: 'gridpanel',
                                        flex: 1,
                                        itemId: 'selectRoles',
                                        bodyBorder: true,
                                        columnLines: false,
                                        enableColumnHide: false,
                                        enableColumnMove: false,
                                        enableColumnResize: false,
                                        hideHeaders: true,
                                        sortableColumns: false,
                                        store: new Ext.data.Store({
                                            model: 'Usr.model.Group'
                                        }),
                                        data: [],
                                        columns: columns
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                layout: {
                                    type: 'hbox',
                                    align: 'center'
                                },
                                maxWidth: 500,
                                items: [
                                    {
                                        xtype: 'container',
                                        layout: {
                                            type: 'vbox',
                                                align: 'center',
                                                pack: 'center'
                                        },
                                        minHeight: 50,
                                        margin: '10 0 0 100 ',
                                        items: [
                                            {
                                                xtype: 'button',
                                                action: 'save',
                                                text: Uni.I18n.translate('general.save', 'USM', 'Save')
                                            }
                                        ]
                                    },
                                    {
                                            xtype: 'container',
                                            layout: {
                                            type: 'vbox',
                                                align: 'center',
                                                pack: 'center'
                                            },
                                            minHeight: 50,
                                            margins: '10 0 0 20',
                                            items: [
                                                 {
                                                     xtype: 'box',
                                                     itemId: 'cancelLink',
                                                     autoEl: {
                                                         tag: 'a',
                                                         href: '#/users',
                                                         html: Uni.I18n.translate('general.cancel', 'USM', 'Cancel')
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
        ];

        this.callParent(arguments);
    }
});

