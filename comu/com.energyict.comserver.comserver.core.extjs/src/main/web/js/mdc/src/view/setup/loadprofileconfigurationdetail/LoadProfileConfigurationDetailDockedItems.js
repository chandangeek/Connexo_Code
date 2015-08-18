Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailDockedItems', {
    extend: 'Uni.view.toolbar.PagingTop',
    border: 0,
    alias: 'widget.loadProfileConfigurationDetailDockedItems',
    aling: 'left',
    router: null,

    store: 'LoadProfileConfigurationDetailChannels',
    displayMsg: '{2} channel configurations',
    displayMoreMsg: '{0} - {1} of more than {2} channel configurations',
    emptyMsg: '0 channel configurations',
    usesExactCount: true,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('loadprofileconfiguration.loadprofilechaneelconfiguationsadd', 'MDC', 'Add channel configuration'),
                privileges: Mdc.privileges.DeviceType.admin,
                action: 'addchannelconfiguration',
                href: me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels/add').buildUrl()
            }
        ];

        me.callParent(this);
    }
});