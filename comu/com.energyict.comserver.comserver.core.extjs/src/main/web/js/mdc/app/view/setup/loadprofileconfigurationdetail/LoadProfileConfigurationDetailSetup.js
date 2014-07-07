Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationDetailSetup', {
    extend: 'Ext.container.Container',
    xtype: 'loadProfileConfigurationDetailSetup',

    intervalStore: null,
    deviceTypeId: null,
    deviceConfigId: null,
    loadProfileConfigurationId: null,
    autoDestroy: false,
    padding: '0 10 0 10',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

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
            xtype: 'container',
            itemId: 'emptyPanel',
            hidden: true,
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
                            html: '<b>' + Uni.I18n.translate('channelConfigurations.empty.title', 'MDC', 'No channel configurations found') + '</b><br>' +
                                Uni.I18n.translate('channelConfigurations.empty.detail', 'MDC', 'There are no channel configurations. This could be because:') + '<ul><li>' +
                                Uni.I18n.translate('channelConfigurations.empty.list.item1', 'MDC', 'No channel configurations have been defined yet.') + '</li><li>' +
                                Uni.I18n.translate('channelConfigurations.empty.list.item2', 'MDC', 'No channel configurations comply to the filter.') + '</li></ul><br>' +
                                Uni.I18n.translate('channelConfigurations.empty.steps', 'MDC', 'Possible steps:')
                        },
                        {
                            xtype: 'container',
                            itemId: 'addchannelconfigurationcontainer'
                        }
                    ]
                }
            ]
        },
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

        this.down('#addchannelconfigurationcontainer').add(
            {
                xtype: 'button',
                margin: '10 0 0 0',
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