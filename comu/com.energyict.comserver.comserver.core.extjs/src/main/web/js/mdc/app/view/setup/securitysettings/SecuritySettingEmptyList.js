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
                html: "<h3>No security settings found</h3><br>\
          There are no security settings. This could be because:\
              <ul>\
                  <li>No security settings have been defined yet.</li>\
                  <li>No security settings comply to the filter.</li>\
              </ul>\
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