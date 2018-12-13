/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailSetup', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationActionMenu'
    ],
    xtype: 'loadProfileConfigurationDetailSetup',
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'loadProfileConfigurationDetailInfo',
                        itemId: 'loadProfileConfigurationDetailInfo',
                        ui: 'large',
                        flex: 1,
                        title: ''
                    },
                    {
                        xtype: 'uni-button-action',
                        margin: '20 0 0 0',
                        menu: {
                            xtype: 'load-profile-configuration-action-menu'
                        }
                    }
                ]
            },
            {
                xtype: 'panel',
                itemId: 'loadProfileConfigurationDetailChannelConfigurationTitle',
                title: Uni.I18n.translate('loadprofileconfiguration.loadprofilechannelconfiguations', 'MDC', 'Channel configurations'),
                ui: 'medium',
                padding: 0
            },
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'loadProfileConfigurationDetailChannelGrid',
                    editActionName: 'editloadprofileconfigurationdetailchannel',
                    deleteActionName: 'deleteloadprofileconfigurationdetailchannel',
                    dockedItems: [
                        {
                            xtype: 'loadProfileConfigurationDetailDockedItems',
                            itemId: 'loadProfileConfigurationDetailDockedItems',
                            dock: 'top',
                            router: me.router
                        }
                    ]
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'emptyPanel',
                    title: Uni.I18n.translate('channelConfigurations.empty.title', 'MDC', 'No channel configurations found'),
                    reasons: [
                        Uni.I18n.translate('channelConfigurations.empty.list.item1', 'MDC', 'No channel configurations have been defined yet.')
                    ],
                    stepItems: [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('loadprofileconfiguration.loadprofilechaneelconfiguationsadd', 'MDC', 'Add channel configuration'),
                            itemId: 'empty-msg-add-channel-configuration-to-load-profile-configuration-btn',
                            privileges: Mdc.privileges.DeviceType.admin,
                            action: 'addchannelconfiguration',
                            href: me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels/add').buildUrl()
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'loadProfileConfigurationDetailChannelPreview',
                    itemId: 'loadProfileConfigurationDetailChannelPreview',
                    editActionName: 'editloadprofileconfigurationdetailchannel',
                    deleteActionName: 'deleteloadprofileconfigurationdetailchannel'
                }
            },
            {
                xtype: 'panel',
                ui: 'medium',
                padding: '32 0 0 0',
                itemId: 'rulesForChannelConfig'
            },
            {
                xtype: 'container',
                itemId: 'validationrulesContainer'
            }
        ];

        me.callParent(arguments);
    }
});