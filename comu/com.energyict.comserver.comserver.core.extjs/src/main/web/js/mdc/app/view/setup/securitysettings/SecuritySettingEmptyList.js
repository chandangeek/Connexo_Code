Ext.define('Mdc.view.setup.securitysettings.SecuritySettingEmptyList', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.securitySettingEmptyList',
    height: 395,
    deviceTypeId: null,
    deviceConfigurationId: null,

    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'panel',
                html: "<h3>No securities found</h3><br>\
          There are no securities. This could be because:<br>\
          &nbsp;&nbsp; - No securities have been defined yet.<br>\
          &nbsp;&nbsp; - No securities comply to the filter.<br><br>\
          Possible steps:<br><br>"
            },
            {
                xtype: 'button',
                text: 'Add security setting',
                action: 'addsecurityaction',
                hrefTarget: '',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings/create'
            }

        )
    }
});