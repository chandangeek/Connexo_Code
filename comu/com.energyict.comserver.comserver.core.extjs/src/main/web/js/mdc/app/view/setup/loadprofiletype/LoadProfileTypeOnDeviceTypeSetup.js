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
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            padding: '0 10 0 10',
            items: [
                {
                    xtype: 'component',
                    margins: '10 10 10 10',
                    itemId: 'loadProfileTypesTitle'
                },
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
                xtype: 'deviceTypeMenu',
                deviceTypeId: this.deviceTypeId,
                toggle: 2
            }
        ];
        this.callParent(arguments);
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
    }

});