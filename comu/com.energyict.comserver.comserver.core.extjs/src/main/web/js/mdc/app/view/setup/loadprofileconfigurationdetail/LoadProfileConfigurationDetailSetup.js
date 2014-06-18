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
                    xtype: 'component',
                    margins: '10 10 10 10',
                    itemId: 'loadProfileConfigurationDetailChannelConfigurationTitle'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileConfigurationDetailDockedItems'
                },
                {
                    xtype: 'panel',
                    itemId: 'emptyPanel',
                    hidden: true,
                    height: 200,
                    items: [
                        {
                            xtype: 'panel',
                            html: "<h3>No channel configurations found</h3><br>\
          There are no channel configurations. This could be because:<br>\
          &nbsp;&nbsp; - No channel configurations have been defined yet.<br>\
          &nbsp;&nbsp; - No channel configurations comply to the filter.<br><br>\
          Possible steps:<br><br>"
                        }
                    ]
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
                    xtype: 'menuseparator',
                    itemId: 'separator'
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
        this.down('#emptyPanel').add(
            {
                xtype: 'button',
                text: Uni.I18n.translate('loadprofileconfiguration.loadprofilechaneelconfiguationsadd', 'MDC', 'Add channel configuration'),
                action: 'addchannelconfiguration',
                hrefTarget: '',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigId + '/loadprofiles/' + this.loadProfileConfigurationId + '/channels/add'
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