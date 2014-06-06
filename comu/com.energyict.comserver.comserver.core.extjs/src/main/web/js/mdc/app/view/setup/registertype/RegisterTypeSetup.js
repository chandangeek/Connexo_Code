Ext.define('Mdc.view.setup.registertype.RegisterTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeSetup',
    itemId: 'registerTypeSetup',

    requires: [
        'Mdc.view.setup.registertype.RegisterTypeGrid',
        'Mdc.view.setup.registertype.RegisterTypePreview',
        'Mdc.view.setup.registertype.RegisterTypeFilter',
        'Uni.view.container.PreviewContainer'
    ],

    /*   side: [
     {
     xtype: 'registerTypeFilter',
     name: 'filter'
     }
     ],
     */

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('registerType.registerTypes', 'MDC', 'Register types'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'registerTypeGrid'
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
                                src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                height: 20,
                                width: 20
                            },
                            {
                                xtype: 'container',
                                items: [
                                    {
                                        xtype: 'component',
                                        html: '<b>' + Uni.I18n.translate('registerType.empty.title', 'MDC', 'No register types found') + '</b><br>' +
                                            Uni.I18n.translate('registerType.empty.detail', 'MDC', 'There are no register types. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('registerType.empty.list.item1', 'MDC', 'No register types have been created yet.') + '</li></lv><br>' +
                                            Uni.I18n.translate('registerType.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('registerType.add', 'MDC', 'Add register type'),
                                        action: 'createRegisterType'
                                    }
                                ]
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


