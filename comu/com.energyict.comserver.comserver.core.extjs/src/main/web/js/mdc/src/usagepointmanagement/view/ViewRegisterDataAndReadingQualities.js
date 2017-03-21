/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.ViewRegisterDataAndReadingQualities', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.view-register-data-and-reading-qualities',
    requires: [
        'Uni.grid.FilterPanelTop',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.container.PreviewContainer',
        'Mdc.usagepointmanagement.view.UsagePointSideMenu',
        'Mdc.usagepointmanagement.view.RegisterDataGrid',
        'Mdc.usagepointmanagement.view.RegisterDataPreview'
    ],
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    store: 'Mdc.usagepointmanagement.store.RegisterData',

    router: null,
    register: null,
    usagePointId: null,
    filter: null,
    idProperty: 'interval_end',

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'view-register-data-and-reading-qualities-panel',
                title: me.register.get('readingType').fullAliasName,
                items: [
                    {
                        xtype: 'previous-next-navigation-toolbar',
                        itemId: 'tabbed-device-registers-view-previous-next-navigation-toolbar',
                        store: 'Mdc.usagepointmanagement.store.Registers',
                        router: me.router,
                        routerIdArgument: 'registerId',
                        itemsName: Ext.String.format('<a href="{0}">{1}</a>',
                            me.router.getRoute('usagepoints/usagepoint/registers').buildUrl(),
                            Uni.I18n.translate('general.registers', 'MDC', 'Registers').toLowerCase()),
                        isFullTotalCount: true
                    },
                    {
                        xtype: 'uni-grid-filterpaneltop',
                        itemId: 'register-data-top-filter',
                        store: 'Mdc.usagepointmanagement.store.RegisterData',
                        hasDefaultFilters: true,
                        filters: [
                            {
                                type: 'duration',
                                text: Uni.I18n.translate('general.startDate', 'MDC', 'Start date'),
                                itemId: 'register-data-top-filter-duration',
                                dataIndex: 'interval',
                                dataIndexFrom: 'intervalStart',
                                dataIndexTo: 'intervalEnd',
                                defaultFromDate: me.filter.defaultFromDate || new Date(),
                                defaultDuration: me.filter.defaultDuration,
                                durationStore: me.filter.durationStore,
                                loadStore: false
                            }
                        ]
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'readings-preview-container',
                        grid: {
                            xtype: 'register-data-grid',
                            itemId: 'register-data-grid',
                            store: me.store,
                            register: me.register,
                            viewConfig: {
                                loadMask: false,
                                doFocus: Ext.emptyFn // workaround to avoid page jump during row selection
                            },
                            listeners: {
                                select: function (grid, record) {
                                    me.down('#readings-preview-container').fireEvent('rowselect', record);
                                },
                                itemclick: function (dataView, record) {
                                    if (me.down('register-data-grid').getSelectionModel().isSelected(record)) {
                                        me.down('#readings-preview-container').fireEvent('rowselect', record);
                                    }
                                }                                
                            }
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('usagePointRegisterData.empty.title', 'MDC', 'No data is available'),
                            reasons: [
                                Uni.I18n.translate('usagePointRegisterData.empty.list.item1', 'MDC', 'No data has been collected yet'),
                                Uni.I18n.translate('usagePointRegisterData.empty.list.item2', 'MDC', 'No devices have been linked to this usage point in specified period of time')
                            ]
                        },
                        previewComponent: {
                            xtype: 'register-data-preview',
                            itemId: 'register-data-preview',
                            router: me.router,
                            register: me.register
                        },
                        listeners: {
                            rowselect: Ext.bind(me.onRowSelect, me)
                        }
                    }
                ]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePointId: me.usagePointId
                    }
                ]
            }
        ];

        me.callParent(arguments);
        me.bindStore(me.store || 'ext-empty-store', true);
        me.on('beforedestroy', me.onBeforeDestroy, me);
    },

    getStoreListeners: function () {
        return {
            beforeload: this.onBeforeLoad,
            load: this.onLoad
        };
    },

    onBeforeLoad: function () {
        this.setLoading(true);
    },

    onLoad: function () {
        this.setLoading(false);
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    }

});