Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationDetailSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationDetailSetup',
    intervalStore: null,
    deviceTypeId: null,
    deviceConfigId: null,
    loadProfileConfigurationId: null,

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
                    itemId: 'loadProfileConfigurationDetailTitle'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileConfigurationDetailInfoContainer'
                },
                {
                    xtype: 'menuseparator'
                },
                {
                    xtype: 'component',
                    margins: '10 10 10 10',
                    itemId: 'loadProfileConfigurationDetailChannelConfigurationTitle'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileConfigurationDetailDockedItems'
                },
//                {
//                    xtype: 'container',
//                    itemId: 'loadProfileConfigurationEmptyListContainer'
//                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileConfigurationDetailChannelGridContainer'
                },
                {
                    xtype: 'menuseparator'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileConfigurationDetailChannelPreviewContainer'
                }
            ]
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
        this.down('#loadProfileConfigurationDetailInfoContainer').add(
            {
                xtype: 'loadProfileConfigurationDetailInfo',
                intervalStore: this.intervalStore
            }
        );
        this.down('#loadProfileConfigurationDetailDockedItems').add(
            {
                xtype: 'loadProfileConfigurationDetailDockedItems',
                deviceTypeId: this.deviceTypeId,
                deviceConfigurationId: this.deviceConfigId,
                loadProfileConfigurationId: this.loadProfileConfigurationId
            }
        );
        this.down('#loadProfileConfigurationDetailChannelGridContainer').add(
            {
                xtype: 'loadProfileConfigurationDetailChannelGrid',
                editActionName: 'editloadprofileconfigurationdetailchannel',
                deleteActionName: 'deleteloadprofileconfigurationdetailchannel'
            }
        );
        this.down('#loadProfileConfigurationDetailChannelPreviewContainer').add(
            {
                xtype: 'loadProfileConfigurationDetailChannelPreview',
                editActionName: 'editloadprofileconfigurationdetailchannel',
                deleteActionName: 'deleteloadprofileconfigurationdetailchannel'
            }
        );
    }
});