Ext.define('Mdc.view.devicecommunicationprotocol.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceCommunicationProtocolList',
    itemId: 'deviceCommunicationProtocolList',
    title: 'All device protocols',
    store: 'DeviceCommunicationProtocols',
    columns: {
        defaults: {
            flex: 1
        },
        items: [
            { header: 'Id', dataIndex: 'databaseId'},
            { header: 'Name', dataIndex: 'name'},
            { header: 'Java class name', dataIndex: 'javaClassName'}
        ]
    },
    initComponent: function() {
        this.buttons = [
            {
                text: 'Save',
                action: 'save'
            }
        ];
        this.dockedItems = [
            {
                xtype: 'pagingtoolbar',
                store: this.store,
                dock: 'bottom',
                displayInfo: true,
                afterPageText: '',
                displayMsg: 'Displaying {0} - {1}'
            }
        ];
        this.listeners = {
            'afterrender': function (component) {
                component.down('#last').hide()
            },
            single: true
        };
        this.callParent(arguments);
    }
});