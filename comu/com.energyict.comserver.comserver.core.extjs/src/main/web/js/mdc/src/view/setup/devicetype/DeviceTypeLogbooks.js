Ext.define('Mdc.view.setup.devicetype.DeviceTypeLogbooks', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-type-logbooks',

    deviceTypeId: null,

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceconfiguration.ActionMenu',
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.form.field.ObisDisplay'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'deviceTypeLogbookPanel',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'grid',
                        itemId: 'device-type-logbook-types-grid',
                        store: 'LogbookTypesOfDeviceType',
                        columns: {
                            defaults: {
                                sortable: false,
                                menuDisabled: true
                            },
                            items: [
                                {
                                    header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                    dataIndex: 'name',
                                    flex: 3
                                },
                                {
                                    xtype: 'obis-column',
                                    dataIndex: 'obisCode',
                                    flex:2
                                },
                                {
                                    xtype: 'actioncolumn',
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    align: 'center',
                                    items: [{
                                        iconCls: 'uni-icon-delete',
                                        tooltip: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                                        handler: function (grid, rowIndex, colIndex, item, e, record) {
                                            var store = grid.getStore(),
                                                gridPanel = grid.up(),
                                                emptyMsg = gridPanel.up().down('displayfield');

                                            this.fireEvent('removeLogbook', record);
                                            if (!store.getCount()) {
                                                Ext.suspendLayouts();
                                                gridPanel.hide();
                                                emptyMsg.show();
                                                Ext.resumeLayouts(true);
                                            }
                                        }
                                    }]
                                }
                            ]
                        },
                        dockedItems: [
                            {
                                xtype: 'pagingtoolbartop',
                                store: 'LogbookTypesOfDeviceType',
                                dock: 'top',
                                displayMsg: Uni.I18n.translate('logbooktype.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} logbook types'),
                                displayMoreMsg: Uni.I18n.translate('logbooktype.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} logbook types'),
                                emptyMsg: Uni.I18n.translate('logbooktype.pagingtoolbartop.emptyMsg', 'MDC', 'There are no logbook types to display'),
                                items: [
                                    {
                                        xtype: 'button',
                                        itemId: 'add-logbook-types-btn',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('logbooktype.addLogbookType', 'MDC', 'Add logbook types'),
                                        privileges: Mdc.privileges.DeviceType.admin,
                                        action: 'add',
                                        listeners: {
                                            click: {
                                                fn: function () {
                                                    window.location.href = '#/administration/devicetypes/' + this.up('device-type-logbooks').deviceTypeId + '/logbooktypes/add';
                                                }
                                            }
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'pagingtoolbarbottom',
                                store: 'LogbookTypesOfDeviceType',
                                dock: 'bottom',
                                itemsPerPageMsg: Uni.I18n.translate('logbooktype.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Logbook types per page')
                            }
                        ]
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'device-type-logbook-types-empty-msg',
                        title: Uni.I18n.translate('logbooktype.empty.title', 'MDC', 'No logbook types found'),
                        reasons: [
                            Uni.I18n.translate('logbooktype.empty.list.item1', 'MDC', 'No logbook types have been defined yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('logbooktype.addLogbookType', 'MDC', 'Add logbook types'),
                                privileges: Mdc.privileges.DeviceType.admin,
                                itemId: 'empty-msg-add-logbook-types-btn',
                                action: 'add',
                                listeners: {
                                    click: {
                                        fn: function () {
                                            window.location.href = '#/administration/devicetypes/' + this.up('device-type-logbooks').deviceTypeId + '/logbooktypes/add';
                                        }
                                    }
                                }
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'panel',
                        height: 110,
                        title: Uni.I18n.translate('general.details','MDC','Details'),
                        name: 'details',
                        frame: true,
                        hidden: true,
                        //tools: [
                        //    {
                        //        xtype: 'button',
                        //        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        //        iconCls: 'x-uni-action-iconD',
                        //        privileges: Mdc.privileges.DeviceType.admin,
                        //        menu: {
                        //            xtype: 'device-type-logbook-action-menu'
                        //        }
                        //    }
                        //],
                        items: [
                            {
                                xtype: 'form',
                                name: 'logbookTypeDetails',
                                layout: 'column',
                                defaults: {
                                    xtype: 'container',
                                    layout: 'form',
                                    columnWidth: 0.5
                                },
                                items: [
                                    {
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                                name: 'name'
                                            }
                                        ]
                                    },
                                    {
                                        items: [
                                            {
                                                xtype: 'obis-displayfield',
                                                name: 'obisCode'
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId
                    }
                ]
            }
        ];
        me.callParent(arguments);

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(me.deviceTypeId, {
            success: function (deviceType) {
                if (deviceType.get('deviceTypePurpose') === 'DATALOGGER_SLAVE') {
                    me.down('#empty-msg-add-logbook-types-btn').setDisabled(true);
                }
            }
        });

    }
});
