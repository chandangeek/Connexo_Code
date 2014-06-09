Ext.define('Mdc.view.setup.logbooktype.LogbookTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.logbookTypeSetup',
    itemId: 'logbookTypeSetup',
    requires: [
        'Mdc.view.setup.logbooktype.LogbookTypeGrid',
        'Mdc.view.setup.logbooktype.LogbookTypePreview',
        'Uni.view.container.PreviewContainer'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('logbooktype.logbookTypes', 'MDC', 'Logbook types'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'logbookTypeGrid'
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
                                        itemId: 'logbookTypeEmptyCollectionComponent',
                                        html: '<b>' + Uni.I18n.translate('logbooktype.empty.title', 'MDC', 'No logbook types found') + '</b><br>' +
                                            Uni.I18n.translate('logbooktype.empty.detail', 'MDC', 'There are no logbook types. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('logbooktype.empty.list.item1', 'MDC', 'No logbook types have been added yet') + '</li>' +
                                            Uni.I18n.translate('logbooktype.empty.list.item2', 'MDC', 'No logbook types comply to the filter') + '</li></lv><br>' +
                                            Uni.I18n.translate('logbooktype.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'logbookTypeCreateActionButton',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('logbooktype.add', 'MDC', 'Add logbook type'),
                                        hrefTarget: '',
                                        href: '#/administration/logbooktypes/create'
                                    }
                                ]
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'logbookTypePreview'
                    }
                }
            ]
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});


