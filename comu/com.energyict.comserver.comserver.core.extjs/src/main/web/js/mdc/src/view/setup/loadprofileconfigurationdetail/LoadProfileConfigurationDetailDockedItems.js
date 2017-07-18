/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailDockedItems', {
    extend: 'Uni.view.toolbar.PagingTop',
    border: 0,
    alias: 'widget.loadProfileConfigurationDetailDockedItems',
    aling: 'left',
    router: null,

    store: 'LoadProfileConfigurationDetailChannels',
    displayMsg:  Uni.I18n.translate('channelConfig.channelConfigurations.display', 'MDC', '{0} - {1} of {2} channel configurations'),
    displayMoreMsg: Uni.I18n.translate('channelConfig.channelConfigurations.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} channel configurations'),
    emptyMsg: Uni.I18n.translate('channelConfig.channelConfigurations.emptyMsg', 'MDC', 'There are no channel configurations to display'),
    usesExactCount: true,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('loadprofileconfiguration.loadprofilechaneelconfiguationsadd', 'MDC', 'Add channel configuration'),
                itemId: 'add-channel-configuration-to-load-profile-configuration-btn',
                privileges: Mdc.privileges.DeviceType.admin,
                action: 'addchannelconfiguration',
                href: me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels/add').buildUrl()
            }
        ];

        me.callParent(this);
    }
});