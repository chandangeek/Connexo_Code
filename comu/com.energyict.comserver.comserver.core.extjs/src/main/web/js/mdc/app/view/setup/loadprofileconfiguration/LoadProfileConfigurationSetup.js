Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationSetup',
    intervalStore: null,
    deviceTypeId: null,
    deviceConfigId: null,

//    side: [
//        {
//            xtype: 'loadProfileTypeSideFilter'
//        }
//    ],


    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            padding: '0 10 0 10',
            items: [
                {
                    xtype: 'component',
                    margins: '10 10 10 10',
                    itemId: 'loadProfileConfigurationTitle'
                },
                {
                    xtype: 'loadProfileConfigurationFiltering'
                },
                {
                    xtype: 'menuseparator'
                },
                {
                    xtype: 'loadProfileConfigurationSorting'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileConfigurationDockedItemsContainer'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileConfigurationEmptyListContainer'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileConfigurationGridContainer'
                },
                {
                    xtype: 'menuseparator'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileConfigurationPreviewContainer'
                }
            ]
        }
    ],


    initComponent: function () {
        this.side = [
            {
                xtype: 'deviceConfigurationMenu',
                deviceTypeId: this.deviceTypeId,
                deviceConfigurationId: this.deviceConfigId,
                toggle: 2
            }
        ];
        this.callParent(arguments);
        this.down('#loadProfileConfigurationDockedItemsContainer').add(
            {
                xtype: 'loadProfileConfigurationDockedItems',
                deviceTypeId: this.deviceTypeId,
                deviceConfigurationId: this.deviceConfigId
            }
        );
        this.down('#loadProfileConfigurationGridContainer').add(
            {
                xtype: 'loadProfileConfigurationGrid',
                intervalStore: this.intervalStore,
                deviceTypeId: this.deviceTypeId,
                deviceConfigurationId: this.deviceConfigId,
                deleteActionName: 'deleteloadprofileconfigurationondeviceonfiguration',
                editActionName: 'editloadprofileconfigurationondeviceconfiguration'
            }
        );
        this.down('#loadProfileConfigurationPreviewContainer').add(
            {
                xtype: 'loadProfileConfigurationPreview',
                intervalStore: this.intervalStore,
                deviceTypeId: this.deviceTypeId,
                deviceConfigurationId: this.deviceConfigId,
                deleteActionName: 'deleteloadprofileconfigurationondeviceonfiguration',
                editActionName: 'editloadprofileconfigurationondeviceconfiguration'
            }
        );
    }

});