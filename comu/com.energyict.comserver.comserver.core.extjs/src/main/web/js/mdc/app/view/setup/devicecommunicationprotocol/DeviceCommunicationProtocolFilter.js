Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationProtocolFilter',
    title: 'Filter',
    cls: 'filter-form',

    items: [
        {
            xtype: 'form',
            items: [
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: 'Name'
                }
            ]
        }
    ],

    buttons: [
        {
            text: 'Apply',
            action: 'filter'
        },
        {
            text: 'Reset',
            action: 'reset'
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});