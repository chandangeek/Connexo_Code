/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskActions', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comTaskActions',
    itemId: 'mdc-comtask-actions-view',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.comtasks.SideMenu',
        'Mdc.view.setup.comtasks.ComtaskActionPreview',
        'Mdc.view.setup.comtasks.ComtaskActionsGrid'
    ],
    router: null,
    communicationTask: null,
    actionsStore: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            items: [
                {
                    xtype: 'preview-container',
                    itemId: 'mdc-comtask-actions-previewContainer',
                    grid: {
                        xtype: 'comtaskActionsGrid',
                        store: me.actionsStore,
                        itemId: 'mdc-comtask-actions-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'mdc-comtask-actions-empty-grid',
                        title: Uni.I18n.translate('comtask.actions.empty.title', 'MDC', 'No actions found'),
                        reasons: [
                            Uni.I18n.translate('comtask.actions.empty.list.item1', 'MDC', 'No actions have been added yet.')
                        ],
                        stepItems: [
                            {
                                itemId: 'comtaskCreateActionButton',
                                text: Uni.I18n.translate('comtask.action.create', 'MDC', 'Add action'),
                                privileges: Mdc.privileges.Communication.admin,
                                href: me.router.getRoute('administration/communicationtasks/view/actions/add').buildUrl()
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'comtaskActionPreview',
                        itemId: 'mdc-comtask-action-preview'
                    }
                }
            ]
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'comTaskSideMenu',
                        router: me.router,
                        itemId: 'mdc-comtask-actions-sidemenu',
                        toggle: 0
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
