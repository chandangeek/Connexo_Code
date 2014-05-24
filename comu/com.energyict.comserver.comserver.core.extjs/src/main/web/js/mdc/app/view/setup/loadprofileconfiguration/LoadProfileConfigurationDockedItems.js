Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationDockedItems', {
    extend: 'Ext.toolbar.Toolbar',
    border: 0,
    alias: 'widget.loadProfileConfigurationDockedItems',
    aling: 'left',
    actionHref: null,
    deviceTypeId: null,
    deviceConfigurationId: null,

    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'container',
                itemId: 'loadProfileConfigurationCountContainer',
                flex: 1
            },
            {
                xtype: 'button',
                text: 'Add load profile configuration',
                action: 'addloadprofileconfiguration',
                margin: '0 5',
                hrefTarget: '',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles/add'
            }
        )
    }
});