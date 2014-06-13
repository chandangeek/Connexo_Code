Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationSetup',
    itemId: 'loadProfileConfigurationSetup',
    requires: [
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationGrid',
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationPreview',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Uni.view.container.PreviewContainer'
    ],
    side: {
        xtype: 'panel',
        ui: 'medium',
        items: [
            {
                xtype: 'deviceConfigurationMenu',
                toggle: 2
            }
        ]
    },
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('loadProfileConfigurations.title', 'MDC', 'Load profile configurations'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'loadProfileConfigurationGrid'
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
                                        html: '<b>' + Uni.I18n.translate('loadProfileConfigurations.empty.title', 'MDC', 'No load profile configurations found') + '</b><br>' +
                                            Uni.I18n.translate('loadProfileConfigurations.empty.detail', 'MDC', 'There are no load profile configurations. This could be because:') + '<lv><li>' +
                                            Uni.I18n.translate('loadProfileConfigurations.empty.list.item1', 'MDC', 'No load profile configurations have been defined yet.') + '</li><li>' +
                                            Uni.I18n.translate('loadProfileConfigurations.empty.list.item2', 'MDC', 'No load profile configurations comply to the filter.') + '</li></lv><br>' +
                                            Uni.I18n.translate('loadProfileConfigurations.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('loadProfileConfigurations.add', 'MDC', 'Add load profile configuration'),
                                        action: 'addloadprofileconfiguration',
                                        href: '#',
                                        hrefTarget: ''
                                    }
                                ]
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'loadProfileConfigurationPreview'
                    }
//                    previewComponent: null
                }
            ]
        }
    ],

    initComponent: function () {
        var config = this.config,
            previewContainer = this.content[0].items[0],
            addButtons;

        config && config.gridStore && (previewContainer.grid.store = config.gridStore);
        config && config.deviceTypeId && (this.side.deviceTypeId = config.deviceTypeId);
        config && config.deviceConfigurationId && (this.side.deviceConfigurationId = config.deviceConfigurationId);

        this.callParent(arguments);

        addButtons = this.query('button[action=addloadprofileconfiguration]');

        config && config.deviceTypeId && config.deviceConfigurationId && Ext.Array.each(addButtons, function (button) {
            button.href = '#/administration/devicetypes/' + config.deviceTypeId + '/deviceconfigurations/' + config.deviceConfigurationId + '/loadprofiles/add';
        });
    }
});