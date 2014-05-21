Ext.define('Mdc.view.setup.securitysettings.SecuritySettingDockedItems', {
    extend: 'Ext.toolbar.Toolbar',
    border: 0,
    alias: 'widget.securitySettingDockedItems',
    aling: 'left',
    deviceTypeId: null,
    deviceConfigurationId: null,

    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'container',
                name: 'SecurityCount',
                flex: 1
            },
            {
                xtype: 'button',
                text: 'Add security setting',
                action: 'addsecurityaction',
                margin: '0 5',
                hrefTarget: '',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings/create'
            }
        )
    }
});