/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskAddPrivileges', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comtaskAddPrivileges',
    itemId: 'mdc-comtask-addPrivileges',
    title: Uni.I18n.translate('general.addPrivileges', 'MDC', 'Add privileges'),
    ui: 'large',
    margin: '0 20',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.comtasks.AddPrivilegesGrid'
    ],

    router: null,
    communicationTask: null,
    store: null,
    cancelRoute: null,

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'preview-container',
                selectByDefault: false,
                grid: {
                    itemId: 'mdc-comtask-addPrivileges-grid',
                    xtype: 'addPrivilegesGrid',
                    store: me.store,
                    maxHeight: 600
                },
                emptyComponent: {
                    xtype: 'uni-form-empty-message',
                    margin: '15 0 20 0',
                    text: Uni.I18n.translate('comtask.addPrivileges.empty', 'MDC', 'All privileges have already been added to the communication task.')
                }
            },
            {
                xtype: 'container',
                itemId: 'buttonsContainer',
                defaults: {
                    xtype: 'button'
                },
                items: [
                    {
                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                        ui: 'action',
                        itemId: 'mdc-comtask-addPrivileges-add',
                        name: 'add'
                    },
                    {
                        name: 'cancel',
                        ui: 'link',
                        itemId: 'mdc-comtask-addPrivileges-cancel',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});