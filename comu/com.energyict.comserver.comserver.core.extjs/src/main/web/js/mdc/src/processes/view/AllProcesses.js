/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.AllProcesses', {
    alias: 'widget.all-flow-processes',
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'allProcesses',
    requires: [
        'Mdc.processes.view.AllProcessesGrid',
        'Mdc.processes.view.ProcessPreview',
        'Uni.view.container.PreviewContainer',
        'Mdc.processes.view.AllProcessesTopFilter',
        'Mdc.processes.store.AllProcessesStore',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    router: null,

    properties: {},

    initComponent: function () {

        var me = this;

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'processesSetupPanel',
                title: Uni.I18n.translate('processesSetupPanel.title', 'MDC', 'Processes'),
                items: [
                    {
                        xtype: 'view-all-processes-topfilter',
                        itemId: 'view-all-processes-topfilter'
                    },
                    {
                        xtype: 'filter-toolbar',
                        title: Uni.I18n.translate('mdc.process.filter.sort', 'MDC', 'Sort'),
                        name: 'sortprocessespanel',
                        itemId: 'processes-sorting-toolbar',
                        emptyText: Uni.I18n.translate('mdc.process.filter.none','MDC','None'),
                        tools: [
                            {
                                xtype: 'button',
                                action: 'addSort',
                                itemId: 'add-sort-btn',
                                text: Uni.I18n.translate('mdc.process.filter.addSort', 'MDC', 'Add sort'),
                                menu: {
                                    xtype: 'processes-sorting-menu',
                                    itemId: 'processes-sorting-menu-id',
                                    name: 'addsortprocessmenu'
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'processesGridGontainer',
                        grid: {
                            xtype: 'processesGrid',
                            itemId: 'processesGrid',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'all-procesess-no-processes-panel',
                            title: Uni.I18n.translate('processesGrid.empty.title', 'MDC', 'No processes are found'),
                            reasons: [
                                    Uni.I18n.translate('processesGrid.empty.list.item1', 'MDC', 'No processes have been configured yet.'),
                                    Uni.I18n.translate('processesGrid.empty.list.item2', 'MDC', 'No processes match filter settings.')
                            ]
                        },
                        previewComponent: {
                            xtype: 'tabpanel',
                            deferredRender: false,
                            ui: 'large',
                            itemId: 'tab-processes-preview',
                            activeTab: 0,
                            items: [
                                {
                                    margin: '10 0 0 0',
                                    title: Uni.I18n.translate('processes.processDetails.title', 'MDC', 'Details'),
                                    itemId: 'details-process-tab',
                                    items: [
                                        {
                                            xtype: 'processPreview',
                                            itemId: 'processPreview',
                                            router: me.router
                                        }
                                    ]
                                },
                                {
                                    margin: '10 0 0 0',
                                    title: Uni.I18n.translate('processes.processStatus.title', 'MDC', 'Status overview'),
                                    itemId: 'status-process-tab',
                                    items: [
                                        {
                                            xtype: 'bpm-status-process-preview',
                                            itemId: 'all-process-status-preview'
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                ]
            }
        ],

        this.callParent(arguments);
   }
});

