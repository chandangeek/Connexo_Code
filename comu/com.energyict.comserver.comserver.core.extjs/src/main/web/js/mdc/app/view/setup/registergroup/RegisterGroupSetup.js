Ext.define('Mdc.view.setup.registergroup.RegisterGroupSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerGroupSetup',
    itemId: 'registerGroupSetup',

    requires: [
        'Mdc.view.setup.registergroup.RegisterGroupGrid',
        'Mdc.view.setup.registergroup.RegisterGroupPreview',
        'Ext.layout.container.Card',
        'Uni.view.container.EmptyGridContainer'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('registerGroup.registerGroups', 'USM', 'Register groups'),
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
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'left'
                        },
                        minHeight: 20,
                        items: [
                            {
                                xtype: 'image',
                                margin: '0 10 0 0',
                                src: "../mdc/resources/images/information.png",
                                height: 20,
                                width: 20
                            },
                            {
                                xtype: 'container',
                                items: [
                                    {
                                        xtype: 'component',
                                        html: '<b>' + Uni.I18n.translate('registerGroup.empty.title', 'MDC', 'No register groups found') + '</b><br>' +
                                            Uni.I18n.translate('registerGroup.empty.detail', 'MDC', 'There are no register groups. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('registerGroup.empty.list.item1', 'MDC', 'No register groups have been defined yet.') + '</li></lv><br>' +
                                            Uni.I18n.translate('registerGroup.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('registerGroup.add', 'MDC', 'Add register group'),
                                        action: 'createRegisterGroup'
                                    }
                                ]
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


