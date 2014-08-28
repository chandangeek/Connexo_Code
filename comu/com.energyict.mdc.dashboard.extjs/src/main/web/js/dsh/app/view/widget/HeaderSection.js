Ext.define('Dsh.view.widget.HeaderSection', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.header-section',
    itemId: 'header-section',
    wTitle: Uni.I18n.translate('overview.widget.headerSection.title', 'DSH', 'Overview'),
    layout: 'column',
    initComponent: function () {
        var me = this;
        this.items = [
            {
                itemId: 'headerTitle',
                baseCls: 'x-panel-header-text-container-large',
                html: me.wTitle
            },
            {
                xtype: 'combobox',
                style: {
                    float: 'left',
                    marginTop: '18px'
                },
                fieldLabel: Uni.I18n.translate('overview.widget.headerSection.deviceGroupLabel', 'DSH', 'for device group'),
                labelWidth: 150,
                displayField: 'group',
                valueField: 'id',
                value: 1,
                store: Ext.create('Ext.data.Store', {
                    fields: ['id', 'group'],
                    data: [ //TODO: set real store
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
                style: {
                    float: 'right',
                    marginTop: '18px'
                },
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
                        iconCls: 'fa fa-refresh fa-lg' //TODO: set real img,
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});