Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeSetup',
    itemId: 'loadProfileTypeSetup',
    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeGrid',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypePreview',
        'Uni.view.container.PreviewContainer'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('loadProfileTypes.title', 'MDC', 'Load profile types'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'loadProfileTypeGrid'
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
                                        html: '<b>' + Uni.I18n.translate('loadProfileTypes.empty.title', 'MDC', 'No load profile types found') + '</b><br>' +
                                            Uni.I18n.translate('loadProfileTypes.empty.detail', 'MDC', 'There are no load profile types. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('deviceType.empty.list.item1', 'MDC', 'No load profile types have been defined yet.') + '</li><li>' +
                                            Uni.I18n.translate('deviceType.empty.list.item2', 'MDC', 'No load profile types comply to the filter.') + '</li></lv><br>' +
                                            Uni.I18n.translate('loadProfileTypes.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('loadProfileTypes.add', 'MDC', 'Add load profile type'),
                                        action: 'addloadprofiletypeaction'
                                    }
                                ]
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'loadProfileTypePreview'
                    }
                }
            ]
        }
    ]
});