Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskDockedItems', {
    extend: 'Ext.toolbar.Toolbar',
    border: 0,
    alias: 'widget.communicationTaskDockedItems',
    align: 'left',
    deviceTypeId: null,
    deviceConfigurationId: null,

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        me.add(
            {
                xtype: 'container',
                name: 'CommunicationTasksCount',
                flex: 1
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('communicationtasks.add', 'MDC', 'Add communication task'),
                action: 'addcommunicationtaskaction',
                margin: '0 5',
                hrefTarget: '',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/comtaskenablements/create'
            }
        )
    }
});