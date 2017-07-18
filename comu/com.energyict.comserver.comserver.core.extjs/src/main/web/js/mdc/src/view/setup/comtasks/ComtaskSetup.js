/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comtaskSetup',
    itemId: 'comtaskSetup',
    router: null,

    requires: [
        'Mdc.view.setup.comtasks.ComtaskGrid',
        'Mdc.view.setup.comtasks.ComtaskPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'comtaskGrid',
                        itemId: 'communication-task-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'empty-grid-comtasks',
                        title: Uni.I18n.translate('comtask.empty.title', 'MDC', 'No communication tasks found'),
                        reasons: [
                            Uni.I18n.translate('comtask.empty.list.item1', 'MDC', 'No communication tasks have been added yet.')
                        ],
                        stepItems: [
                            {
                                itemId: 'comtaskCreateActionButton',
                                text: Uni.I18n.translate('comtask.create', 'MDC', 'Add communication task'),
                                privileges: Mdc.privileges.Communication.admin,
                                href: '#/administration/communicationtasks/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'comtaskPreview',
                        itemId: 'communication-task-preview'
                    }
                }
            ]
        };
        this.callParent(arguments);
    }
});


