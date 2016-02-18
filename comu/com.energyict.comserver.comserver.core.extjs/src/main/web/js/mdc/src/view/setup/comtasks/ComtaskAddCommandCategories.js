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
        'Uni.view.notifications.NoItemsFoundPanel',
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
                    xtype: 'no-items-found-panel',
                    margin: '15 0 20 0',
                    title: Uni.I18n.translate('comtask.commandCategories.empty.title', 'MDC', 'No command categories found.'),
                    reasons: [
                        Uni.I18n.translate('comtask.commandCategories.empty.list.item1', 'MDC', 'All command categories have already been added to the communication task.')
                    ]
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