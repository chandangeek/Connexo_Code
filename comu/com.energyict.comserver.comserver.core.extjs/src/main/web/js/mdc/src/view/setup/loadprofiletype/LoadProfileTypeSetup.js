Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeSetup',
    itemId: 'loadProfileTypeSetup',

    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeGrid',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    side: [
        {
            xtype: 'panel',
            ui: 'medium',
            items: []
        }
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
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('loadProfileTypes.empty.title', 'MDC', 'No load profile types found'),
                        reasons: [
                            Uni.I18n.translate('loadProfileTypes.empty.list.item1', 'MDC', 'No load profile types have been defined yet.'),
                            Uni.I18n.translate('loadProfileTypes.empty.list.item2', 'MDC', 'No load profile types comply to the filter.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('loadProfileTypes.add', 'MDC', 'Add load profile type'),
                                action: 'addloadprofiletypeaction',
                                href: '#/administration/loadprofiletypes/create'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'loadProfileTypePreview'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        var config = this.config,
            previewContainer = this.content[0].items[0],
            addButtons;

        config && config.gridStore && (previewContainer.grid.store = config.gridStore);

        this.callParent(arguments);

        addButtons = this.query('button[action=addloadprofiletypeaction]');

        if (config) {
            if (config.deviceTypeId) {
                this.getWestContainer().down('panel').add({
                    xtype: 'deviceTypeMenu',
                    deviceTypeId: config.deviceTypeId,
                    toggle: 2
                });
                Ext.Array.each(addButtons, function (button) {
                    button.href = '#/administration/devicetypes/' + config.deviceTypeId + '/loadprofiles/add';
                });
            }
        }
    }
});