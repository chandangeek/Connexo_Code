/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskAddCommandCategories', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comtaskAddCommandCategories',
    itemId: 'mdc-comtask-addCommandCategories',
    title: Uni.I18n.translate('general.addCommandCategories', 'MDC', 'Add command categories'),
    ui: 'large',
    margin: '0 20',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.comtasks.AddCommandCategoriesGrid'
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
                    itemId: 'mdc-comtask-addCommandCategories-grid',
                    xtype: 'addCommandCategoriesGrid',
                    store: me.store,
                    maxHeight: 600
                },
                emptyComponent: {
                    xtype: 'uni-form-empty-message',
                    margin: '15 0 20 0',
                    text: Uni.I18n.translate('comtask.addCommandCategories.empty', 'MDC', 'All command categories have already been added to the communication task.')
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
                        itemId: 'mdc-comtask-addCommandCategories-add',
                        name: 'add'
                    },
                    {
                        name: 'cancel',
                        ui: 'link',
                        itemId: 'mdc-comtask-addCommandCategories-cancel',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});