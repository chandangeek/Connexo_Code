Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationTaskSetup',
    itemId: 'communicationTaskSetup',
    deviceTypeId: null,
    deviceConfigurationId: null,

    requires: [
        'Mdc.view.setup.communicationtask.CommunicationTaskGrid',
        'Mdc.view.setup.communicationtask.CommunicationTaskPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
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
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('communicationtasks.empty.title', 'MDC', 'No communication task configurations found'),
                            reasons: [
                                Uni.I18n.translate('communicationtasks.empty.list.item1', 'MDC', 'No communication task configurations have been defined yet.'),
                                Uni.I18n.translate('communicationtasks.empty.list.item2', 'MDC', 'No communication task configurations comply to the filter.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('communicationtasks.add', 'MDC', 'Add communication task'),
                                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/comtaskenablements/create'
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


