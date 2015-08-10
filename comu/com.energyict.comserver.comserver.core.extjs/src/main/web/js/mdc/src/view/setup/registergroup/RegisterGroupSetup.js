Ext.define('Mdc.view.setup.registergroup.RegisterGroupSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerGroupSetup',
    itemId: 'registerGroupSetup',

    requires: [
        'Mdc.view.setup.registergroup.RegisterGroupGrid',
        'Mdc.view.setup.registergroup.RegisterGroupPreview',
        'Ext.layout.container.Card',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('registerGroup.registerGroups', 'MDC', 'Register groups'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'registerGroupGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-register-group',
                        title: Uni.I18n.translate('registerGroup.empty.title', 'MDC', 'No register groups found'),
                        reasons: [
                            Uni.I18n.translate('registerGroup.empty.list.item1', 'MDC', 'No register groups have been defined yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('registerGroup.add', 'MDC', 'Add register group'),
                                privileges: Mdc.privileges.MasterData.admin,
                                action: 'createRegisterGroup'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'registerGroupPreview'
                    }
                }
            ]
        }
    ]
});


