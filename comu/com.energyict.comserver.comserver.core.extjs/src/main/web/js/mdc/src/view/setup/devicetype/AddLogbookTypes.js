/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.AddLogbookTypes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-logbook-types',
    deviceTypeId: null,
    store: 'AvailableLogbookTypes',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.devicetype.AddLogbookTypesGrid',
        'Uni.view.container.EmptyGridContainer'
    ],
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'addLogbookPanel',
                layout: {
                    type: 'vbox',
                    align: 'left'
                },
                items: [
                    {
                        itemId: 'add-logbook-type-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true
                    },
                    {
                        xtype: 'emptygridcontainer',
                        width: '100%',
                        grid: {
                            xtype: 'add-logbook-types-grid',
                            itemId: 'logbook-type-add-grid'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'logbook-type-add-empty-msg',
                            title: Uni.I18n.translate('logbooktypes.empty.title', 'MDC', 'No logbook types found'),
                            reasons: [
                                Uni.I18n.translate('logbooktypes.empty.list.item1', 'MDC', 'No logbook types are defined yet'),
                                Uni.I18n.translate('logbooktypes.empty.list.item2', 'MDC', 'All logbook types are already added to the device type.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('logbooktype.managelogbooktypes', 'MDC', 'Manage logbook types'),
                                    itemId: 'empty-msg-logbook-type-add-btn',
                                    href: me.router.getRoute('administration/logbooktypes').buildUrl()
                                }
                            ]
                        },
                        onLoad: function (store, records) {
                            this.up('#addLogbookPanel').down('button[action=add]').setVisible(records && records.length);
                            this.getLayout().setActiveItem(records && records.length ? this.getGridCt() : this.getEmptyCt());
                        }
                    },
                    {
                        xtype: 'container',
                        itemId: 'add-logbook-type-selection-error',
                        hidden: true,
                        html: '<span style="color: #eb5642">' + Uni.I18n.translate('logbooktypes.no.logbooktype.selected', 'MDC', 'Select at least 1 logbook type') + '</span>'
                    },
                    {
                        xtype: 'toolbar',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        width: '100%',
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                itemId: 'logbook-type-add-btn',
                                xtype: 'button',
                                action: 'add',
                                ui: 'action'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                action: 'cancel',
                                ui: 'link',
                                listeners: {
                                    click: {
                                        fn: function () {
                                            window.location.href = '#/administration/devicetypes/' + this.up('add-logbook-types').deviceTypeId + '/logbooktypes';
                                        }
                                    }
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }

});
