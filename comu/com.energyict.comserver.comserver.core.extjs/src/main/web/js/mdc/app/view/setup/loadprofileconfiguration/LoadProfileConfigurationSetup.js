Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationSetup',
    itemId: 'loadProfileConfigurationSetup',

    requires: [
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationGrid',
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationPreview',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigAndRulesPreviewContainer'
    ],

    side: {
        xtype: 'panel',
        ui: 'medium',
        items: [
            {
                xtype: 'device-configuration-menu',
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
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('loadProfileConfigurations.empty.title', 'MDC', 'No load profile configurations found'),
                        reasons: [
                            Uni.I18n.translate('loadProfileConfigurations.empty.list.item1', 'MDC', 'No load profile configurations have been defined yet.'),
                            Uni.I18n.translate('loadProfileConfigurations.empty.list.item2', 'MDC', 'No load profile configurations comply to the filter.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('loadProfileConfigurations.add', 'MDC', 'Add load profile configuration'),
                                action: 'addloadprofileconfiguration'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'loadProfileConfigAndRulesPreviewContainer',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigId: this.deviceConfigId
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        var config = this.config,
            previewContainer = this.content[0].items[0],
            menu = this.side.items[0],
            addButtons;

        if (config) {
            previewContainer.grid.store = config.gridStore ? config.gridStore : null;
            menu.deviceTypeId = config.deviceTypeId ? config.deviceTypeId : null;
            menu.deviceConfigurationId = config.deviceConfigurationId ? config.deviceConfigurationId : null;
        }

        this.callParent(arguments);

        addButtons = this.query('button[action=addloadprofileconfiguration]');

        config && config.deviceTypeId && config.deviceConfigurationId && Ext.Array.each(addButtons, function (button) {
            button.href = '#/administration/devicetypes/' + config.deviceTypeId + '/deviceconfigurations/' + config.deviceConfigurationId + '/loadprofiles/add';
        });
    }
});