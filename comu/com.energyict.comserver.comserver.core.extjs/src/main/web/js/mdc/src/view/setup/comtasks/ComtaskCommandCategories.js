/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskCommandCategories', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comTaskCommandCategories',
    itemId: 'mdc-comtask-commandCategories-view',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.comtasks.SideMenu',
        'Mdc.view.setup.comtasks.ComtaskCommandCategoriesGrid'
    ],
    router: null,
    communicationTask: null,
    categoriesStore: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('comtask.message.categories', 'MDC', 'Command categories'),
            items: [
                {
                    xtype: 'preview-container',
                    itemId: 'mdc-comtask-commandCategories-previewContainer',
                    grid: {
                        xtype: 'comtaskCommandCategoriesGrid',
                        store: me.categoriesStore,
                        itemId: 'mdc-comtask-commandCategories-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'mdc-comtask-commandCategories-empty-grid',
                        title: Uni.I18n.translate('comtask.commandCategories.empty.title', 'MDC', 'No command categories found'),
                        reasons: [
                            Uni.I18n.translate('comtask.commandCategories.empty.list.item1', 'MDC', 'No command categories have been added yet.')
                        ],
                        stepItems: [
                            {
                                itemId: 'comtaskCreateCommandCategoryButton',
                                text: Uni.I18n.translate('comtask.commandCategories.add', 'MDC', 'Add command categories'),
                                privileges: Mdc.privileges.Communication.admin,
                                href: me.router.getRoute('administration/communicationtasks/view/commandcategories/add').buildUrl()
                            }
                        ]
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
                        itemId: 'mdc-comtask-cmdcategories-sidemenu',
                        toggle: 0
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
