/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskPrivileges', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comTaskPrivileges',
    itemId: 'mdc-comtask-privileges-view',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.comtasks.SideMenu',
        'Mdc.view.setup.comtasks.ComtaskPrivilegesGrid'
    ],
    router: null,
    communicationTask: null,
    privilegesStore: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('comtask.message.privileges', 'MDC', 'Privileges'),
            items: [
                {
                    xtype: 'preview-container',
                    itemId: 'mdc-comtask-privileges-previewContainer',
                    grid: {
                        xtype: 'comtaskPrivilegesGrid',
                        store: me.privilegesStore,
                        itemId: 'mdc-comtask-privileges-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'mdc-comtask-privileges-empty-grid',
                        title: Uni.I18n.translate('comtask.privileges.empty.title', 'MDC', 'No privileges found'),
                        reasons: [
                            Uni.I18n.translate('comtask.privileges.empty.list.item1', 'MDC', 'No privileges have been added yet.')
                        ],
                        stepItems: [
                            {
                                itemId: 'comtaskCreatePrivilegeButton',
                                text: Uni.I18n.translate('comtask.privileges.add', 'MDC', 'Add privileges'),
                                privileges: Mdc.privileges.Communication.admin,
                                href: me.router.getRoute('administration/communicationtasks/view/privileges/add').buildUrl()
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
                        itemId: 'mdc-comtask-privileges-sidemenu',
                        toggle: 0
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
