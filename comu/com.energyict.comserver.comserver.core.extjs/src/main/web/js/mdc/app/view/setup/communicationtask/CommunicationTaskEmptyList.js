Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskEmptyList', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communicationTaskEmptyList',
    deviceTypeId: null,
    deviceConfigurationId: null,

    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                xtype: 'panel',
                html: "<h3>No communication task configurations found</h3><br>\
          There are no communication task configurations. This could be because:<br>\
          &nbsp;&nbsp; - No communication task configurations have been defined yet.<br>\
          &nbsp;&nbsp; - No communication task configurations comply to the filter.<br><br>\
          Possible steps:<br><br>"
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('communicationtasks.add', 'MDC', 'Add communication task'),
                action: 'addcommunicationtaskaction',
                hrefTarget: '',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/comtaskenablements/create'
            }

        )
    }
});