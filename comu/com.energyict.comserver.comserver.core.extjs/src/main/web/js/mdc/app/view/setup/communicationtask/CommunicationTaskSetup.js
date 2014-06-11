Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationTaskSetup',
    itemId: 'communicationTaskSetup',
    deviceTypeId: null,
    deviceConfigurationId: null,

    requires: [
        'Mdc.view.setup.communicationtask.CommunicationTaskGrid',
        'Mdc.view.setup.communicationtask.CommunicationTaskPreview',
        'Uni.view.container.PreviewContainer'
    ],

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceConfigurationMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: me.deviceTypeId,
                        deviceConfigurationId: me.deviceConfigurationId,
                        toggle: 7
                    }
                ]
            }
        ];
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('communicationtasks.communicationtasks', 'MDC', 'Communication tasks'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'communicationTaskGrid',
                            deviceTypeId: me.deviceTypeId,
                            deviceConfigurationId: me.deviceConfigurationId
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
                                            html: '<b>' + Uni.I18n.translate('communicationtasks.empty.title', 'MDC', 'No communication task configurations found') + '</b><br>' +
                                                Uni.I18n.translate('communicationtasks.empty.detail', 'MDC', 'There are no communication task configurations. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('communicationtasks.empty.list.item1', 'MDC', 'No communication task configurations have been defined yet.') + '</li><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('communicationtasks.empty.list.item2', 'MDC', 'No communication task configurations comply to the filter.') + '</li></lv><br>' +
                                                Uni.I18n.translate('communicationtasks.empty.steps', 'MDC', 'Possible steps:')
                                        },
                                        {
                                            xtype: 'button',
                                            margin: '10 0 0 0',
                                            text: Uni.I18n.translate('communicationtasks.add', 'MDC', 'Add communication task'),
                                            hrefTarget: '',
                                            href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/comtaskenablements/create'
                                        }
                                    ]
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'communicationTaskPreview'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


