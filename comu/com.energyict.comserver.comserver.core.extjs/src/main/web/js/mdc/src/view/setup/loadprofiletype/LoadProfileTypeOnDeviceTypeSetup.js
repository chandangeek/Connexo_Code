Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeOnDeviceTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeOnDeviceTypeSetup',
    intervalStore: null,
    deviceTypeId: null,

//    side: [
//        {
//            xtype: 'loadProfileTypeSideFilter'
//        }
//    ],


    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'loadProfileTypes',
            title: Uni.I18n.translate('general.loadProfileTypes', 'MDC', 'Load profile types'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'loadProfileTypeFiltering'
                },
                {
                    xtype: 'menuseparator'
                },
                {
                    xtype: 'loadProfileTypeSorting'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileTypeDockedItemsContainer'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileTypeGridContainer'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileTypeEmptyListContainer'
                },
                {
                    xtype: 'menuseparator'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileTypePreviewContainer'
                }

            ]
        }
    ],


    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeMenu',
                        deviceTypeId: this.deviceTypeId,
                        toggle: 2
                    }
                ]
            }
        ];
        this.callParent(arguments);
        Ext.suspendLayouts();
        this.down('#loadProfileTypeGridContainer').add(
            {
                xtype: 'loadProfileTypeGrid',
                intervalStore: this.intervalStore,
                store: 'LoadProfileTypesOnDeviceType',
                deleteActionName: 'deleteloadprofiletypeondevicetype'
            }
        );
        this.down('#loadProfileTypePreviewContainer').add(
            {
                xtype: 'loadProfileTypePreview',
                intervalStore: this.intervalStore,
                deleteActionName: 'deleteloadprofiletypeondevicetype'
            }
        );
        this.down('#loadProfileTypeDockedItemsContainer').add(
            {
                xtype: 'loadProfileTypeDockedItems',
                actionHref: '#/administration/devicetypes/' + this.deviceTypeId + '/loadprofiles/add'
            }
        );
        Ext.resumeLayouts();
    }

});