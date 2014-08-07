Ext.define('Dsh.view.widget.OverviewHeader', {
    extend: 'Ext.container.Container',
    alias: 'widget.overview-header',
    itemId: 'overview-header',
    colspan: 4,
    layout: 'column',
    headerTitle: null,
    items: [
        {
            itemId: 'headerTitle',
            ui: 'large',
            minWidth: 500
        },
        {
            xtype: 'combobox',
            fieldLabel: 'for device group', //TODO: localize
            labelWidth: 150,
            displayField: 'group',
            valueField: 'id',
            value: 1,
            style: 'margin-top: 35px; float: left',
            store: Ext.create('Ext.data.Store', {
                fields: ['id', 'group'],
                data: [ //TODO: set to real store
                    { 'id': 1, 'group': 'North region' },
                    { 'id': 2, 'group': 'South region' },
                    { 'id': 3, 'group': 'West region' },
                    { 'id': 4, 'group': 'East region' }
                ]
            })
        },
        {
            xtype: 'fieldcontainer',
            layout: 'hbox',
            style: 'margin-top: 35px; float: right',
            items: [
                {
                    xtype: 'displayfield',
                    value: 'Last updated at 15:01', //TODO: set to real & localize
                    style: 'margin-right: 10px'
                },
                {
                    xtype: 'button',
                    text: 'Refresh', //TODO: localize
                    iconCls: 'fa fa-refresh fa-lg' //TODO: set real img
                }
            ]
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
        this.down('#headerTitle').setTitle(this.headerTitle);
    }
});