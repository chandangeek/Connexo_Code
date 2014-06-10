Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailDockedItems', {
    extend: 'Uni.view.toolbar.PagingTop',
    border: 0,
    alias: 'widget.loadProfileConfigurationDetailDockedItems',
    aling: 'left',
    deviceTypeId: null,
    deviceConfigurationId: null,
    loadProfileConfigurationId: null,

    store: 'LoadProfileConfigurationDetailChannels',
    displayMsg: '{0} - {1} of {2} channel configurations',
    displayMoreMsg: '{0} - {1} of more than {2} channel configurations',
    emptyMsg: '0 channel configurations',
    items: [
        '->'
    ],
    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'button',
                text: Uni.I18n.translate('loadprofileconfiguration.loadprofilechaneelconfiguationsadd', 'MDC', 'Add channel configuration'),
                action: 'addchannelconfiguration',
                margin: '0 5',
                hrefTarget: '',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles/' + this.loadProfileConfigurationId + '/channels/add'
            }
        )
    }
});