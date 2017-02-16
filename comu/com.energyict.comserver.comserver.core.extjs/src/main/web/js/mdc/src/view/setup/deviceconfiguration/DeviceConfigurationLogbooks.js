/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationLogbooks', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-configuration-logbooks',

    deviceConfigurationId: null,
    deviceTypeId: null,

    requires: [
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
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
            title: Uni.I18n.translate('general.logbookConfigurations', 'MDC', 'Logbook configurations'),
            ui: 'large',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'grid',
                        itemId: 'device-configuration-logbooks-grid',
                        store: 'LogbookConfigurations',
                        columns: {
                            defaults: {
                                sortable: false,
                                menuDisabled: true
                            },
                            items: [
                                {
                                    header: Uni.I18n.translate('general.name','MDC','Name'),
                                    dataIndex: 'name',
                                    flex: 5
                                },
                                {
                                    xtype: 'obis-column',
                                    dataIndex: 'overruledObisCode',
                                    flex: 1
                                },
                                {
                                    xtype: 'uni-actioncolumn',
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    menu: {xtype: 'device-logbook-action-menu'}
                                }
                            ]
                        },
                        dockedItems: [
                            {
                                xtype: 'pagingtoolbartop',
                                store: 'LogbookConfigurations',
                                dock: 'top',
                                displayMsg: Uni.I18n.translate('deviceconfiguration.logbookConfiguration.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} logbook configurations'),
                                displayMoreMsg: Uni.I18n.translate('deviceconfiguration.logbookConfiguration.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} logbook configurations'),
                                emptyMsg: Uni.I18n.translate('deviceconfiguration.logbookConfiguration.pagingtoolbartop.emptyMsg', 'MDC', 'There are no logbook configurations to display'),
                                items: [
                                    {
                                        xtype: 'button',
                                        itemId: 'add-logbook-configuration-to-device-configuration-btn',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('deviceconfiguration.addLogbookConfiguration', 'MDC', 'Add logbook configuration'),
                                        privileges: Mdc.privileges.DeviceType.admin,
                                        action: 'add',
                                        listeners: {
                                            click: {
                                                fn: function () {
                                                    window.location.href = '#/administration/devicetypes/' + this.up('device-configuration-logbooks').deviceTypeId + '/deviceconfigurations/' + this.up('device-configuration-logbooks').deviceConfigurationId + '/logbookconfigurations/add';
                                                }
                                            }
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'pagingtoolbarbottom',
                                dock: 'bottom',
                                store: 'LogbookConfigurations',
                                itemsPerPageMsg: Uni.I18n.translate('logbookconfiguration.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Logbook configurations per page')
                            }
                        ]
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'device-configuration-logbooks-empty-msg',
                        title: Uni.I18n.translate('deviceconfiguration.logbookConfiguration.empty.title', 'MDC', 'No logbook configurations found'),
                        reasons: [
                            Uni.I18n.translate('deviceconfiguration.logbookConfiguration.empty.list.item1', 'MDC', 'No logbook configurations have been defined yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('deviceconfiguration.addLogbookConfiguration', 'MDC', 'Add logbook configuration'),
                                privileges: Mdc.privileges.DeviceType.admin,
                                action: 'add',
                                itemId: 'empty-msg-add-logbook-configuration-to-device-configuration-btn',
                                listeners: {
                                    click: {
                                        fn: function () {
                                            window.location.href = '#/administration/devicetypes/' + this.up('device-configuration-logbooks').deviceTypeId + '/deviceconfigurations/' + this.up('device-configuration-logbooks').deviceConfigurationId + '/logbookconfigurations/add';
                                        }
                                    }
                                }
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'panel',
                        title: Uni.I18n.translate('general.details','MDC','Details'),
                        itemId: 'device-configuration-logbooks-preview',
                        name: 'details',
                        frame: true,
                        tools: [
                            {
                                xtype: 'uni-button-action',
                                privileges: Mdc.privileges.DeviceType.admin,
                                menu: {
                                    xtype: 'device-logbook-action-menu'
                                }
                            }
                        ],
                        items: [
                            {
                                xtype: 'form',
                                name: 'logbookConfigurationDetails',
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
                                                fieldLabel: Uni.I18n.translate('general.name','MDC','Name'),
                                                name: 'name',
                                                labelWidth: 160
                                            },
                                            {
                                                xtype: 'obis-displayfield',
                                                fieldLabel: Uni.I18n.translate('general.obisCode', 'MDC', 'OBIS code'),
                                                name: "overruledObisCode",
                                                labelWidth: 160
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
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: me.deviceTypeId,
                        deviceConfigurationId: me.deviceConfigurationId
                    }
                ]
            }
        ];
        me.callParent(arguments);
        Ext.data.StoreManager.lookup('LogbookConfigurations').load();

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(me.deviceTypeId, {
            success: function (deviceType) {
                if (deviceType.get('deviceTypePurpose') === 'DATALOGGER_SLAVE') {
                    me.down('#empty-msg-add-logbook-configuration-to-device-configuration-btn').setDisabled(true);
                }
            }
        });

    }
});

