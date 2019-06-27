/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.AllProcessesInsight', {
    alias: 'widget.all-flow-processes-insight',
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'allProcessesInsight',
    requires: [
        'Imt.processes.view.InsightProcessesGrid',
        'Imt.processes.view.ProcessPreview',
        'Uni.view.container.PreviewContainer',
        'Imt.processes.view.InsightProcessesTopFilter',
        'Imt.processes.store.InsightProcessesStore',
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
                title: Uni.I18n.translate('processesSetupPanel.title', 'IMT', 'Processes'),
                items: [
                    {
                        xtype: 'view-insight-processes-topfilter',
                        itemId: 'view-insight-processes-topfilter'
                    },
                    {
                        xtype: 'filter-toolbar',
                        title: Uni.I18n.translate('imt.process.filter.sort', 'IMT', 'Sort'),
                        name: 'sortprocessespanel',
                        itemId: 'processes-sorting-toolbar',
                        emptyText: Uni.I18n.translate('imt.process.filter.none','IMT','None'),
                        tools: [
                            {
                                xtype: 'button',
                                action: 'addSort',
                                itemId: 'add-sort-btn',
                                text: Uni.I18n.translate('imt.process.filter.addSort', 'IMT', 'Add sort'),
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
                            title: Uni.I18n.translate('processesGrid.empty.title', 'IMT', 'No processes are found'),
                            reasons: [
                                    Uni.I18n.translate('processesGrid.empty.list.item1', 'IMT', 'No processes have been configured yet.'),
                                    Uni.I18n.translate('processesGrid.empty.list.item2', 'IMT', 'No processes match filter settings.')
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
                                    title: Uni.I18n.translate('processes.processDetails.title', 'IMT', 'Details'),
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
                                    title: Uni.I18n.translate('processes.processStatus.title', 'IMT', 'Status overview'),
                                    itemId: 'status-process-tab',
                                    items: [
                                        {
                                            xtype: 'bpm-status-process-preview-extended',
                                            itemId: 'all-process-status-preview-extended'
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
