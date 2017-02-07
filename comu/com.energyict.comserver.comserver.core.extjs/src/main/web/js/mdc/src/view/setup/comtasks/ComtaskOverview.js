/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comTaskOverview',
    requires: [
        'Mdc.view.setup.comtasks.SideMenu',
        'Mdc.view.setup.comtasks.ComtaskActionMenu',
        'Mdc.view.setup.comtasks.ComtaskPreview'
    ],
    router: null,
    communicationTask: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'mdc-comtask-overview-panel',
                    title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                    flex: 1,
                    items: {
                        xtype: 'comtaskpreviewform',
                        itemId: 'mdc-comtask-overview-form',
                        margin: '0 0 0 100'
                    }
                },
                {
                    xtype: 'uni-button-action',
                    privileges: Mdc.privileges.Communication.admin,
                    margin: '20 0 0 0',
                    menu: {
                        xtype: 'comtaskActionMenu',
                        communicationTask: me.communicationTask
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
                        itemId: 'mdc-comtask-overview-sidemenu',
                        toggle: 0
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
