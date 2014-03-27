Ext.define('Usr.view.group.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.groupEdit',
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
        'Usr.store.Privileges',
        'Usr.model.Privilege'
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
                        id: 'els_usm_groupEditHeader'
                    },
                    {
                        xtype: 'form',
                        id: 'els_usm_groupEditForm',
                        border: false,
                        //padding: '10 10 0 10',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        //defaults: {
                        //    anchor: '100%',
                        //    margins: '0 0 10 0'
                        //},

                        items: [
                            {
                                xtype: 'textfield',
                                //id: 'els_usm_groupEditFormName',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('group.name', 'USM', 'Name') + '*',
                                labelWidth: 100,
                                labelPad: 0,
                                maxWidth: 500
                            },
                            {
                                xtype: 'textfield',
                                //id: 'els_usm_groupEditFormDescription',
                                name: 'description',
                                fieldLabel: Uni.I18n.translate('group.description', 'USM', 'Description'),
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
                                        fieldLabel: Uni.I18n.translate('group.privileges', 'USM', 'Privileges')
                                    },
                                    {
                                        xtype: 'gridpanel',
                                        flex: 1,
                                        itemId: 'selectPrivileges',
                                        bodyBorder: true,
                                        columnLines: false,
                                        enableColumnHide: false,
                                        enableColumnMove: false,
                                        enableColumnResize: false,
                                        hideHeaders: true,
                                        sortableColumns: false,
                                        store: new Ext.data.Store({
                                            model: 'Usr.model.Privilege'
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
                                                    href: '#/roles',
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