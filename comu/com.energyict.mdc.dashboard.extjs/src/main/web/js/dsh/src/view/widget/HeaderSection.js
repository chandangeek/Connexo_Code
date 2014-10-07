Ext.define('Dsh.view.widget.HeaderSection', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.header-section',
    itemId: 'header-section',
    layout: 'column',
    router: null,

    initComponent: function () {
        var me = this;
        this.items = [
            {
                itemId: 'headerTitle',
                baseCls: 'x-panel-header-text-container-large',
                html: me.router ? me.router.getRoute().title : ''
            },
//            {
//                xtype: 'combobox',
//                style: {
//                    float: 'left',
//                    marginTop: '18px'
//                },
//                fieldLabel: Uni.I18n.translate('overview.widget.headerSection.deviceGroupLabel', 'DSH', 'for device group'),
//                labelWidth: 150,
//                displayField: 'group',
//                valueField: 'id',
//                value: 1,
//                store: Ext.create('Ext.data.Store', {
//                    fields: ['id', 'group'],
//                    data: [ //TODO: set real store
//                        { 'id': 1, 'group': 'North region' },
//                        { 'id': 2, 'group': 'South region' },
//                        { 'id': 3, 'group': 'West region' },
//                        { 'id': 4, 'group': 'East region' }
//                    ]
//                })
//            },
            {
                xtype: 'fieldcontainer',
                layout: 'hbox',
                style: 'float: right; margin-top: 18px',
                items: [
                    {
                        xtype: 'displayfield',
                        itemId: 'last-updated-field',
                        style: 'margin-right: 10px'
                    },
                    {
                        xtype: 'button',
                        itemId: 'refresh-btn',
                        text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                        style: {
                            lineHeight: 'none'
                        },
                        icon: '/apps/sky/build/resources/images/form/restore.png'
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});