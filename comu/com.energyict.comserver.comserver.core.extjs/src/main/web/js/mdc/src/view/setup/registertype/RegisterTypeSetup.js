Ext.define('Mdc.view.setup.registertype.RegisterTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeSetup',
    itemId: 'registerTypeSetup',

    requires: [
        'Mdc.view.setup.registertype.RegisterTypeGrid',
        'Mdc.view.setup.registertype.RegisterTypePreview',
        'Mdc.view.setup.registertype.RegisterTypeFilter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.registerTypes', 'MDC', 'Register types'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'registerTypeGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-register-type',
                        title: Uni.I18n.translate('registerType.empty.title', 'MDC', 'No register types found'),
                        reasons: [
                            Uni.I18n.translate('registerType.empty.list.item1', 'MDC', 'No register types have been created yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('registerType.add', 'MDC', 'Add register type'),
                                privileges: Mdc.privileges.MasterData.admin,
                                action: 'createRegisterType'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'registerTypePreview'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});


