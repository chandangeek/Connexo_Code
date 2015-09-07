Ext.define('Dsh.view.widget.HeaderSection', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.header-section',
    itemId: 'header-section',
    layout: 'fit',
    router: null,
    ui: 'large',

    initComponent: function () {
        var me = this;
        me.title = me.router.getRoute().title;
        this.items = [
            {
                xtype: 'toolbar',
                items: [
                    {
                        xtype: 'device-group-filter',
                        router: me.router
                    },
                    '->',
                    {
                        xtype: 'displayfield',
                        itemId: 'last-updated-field',
                        style: 'margin-right: 10px'
                    },
                    {
                        xtype: 'button',
                        itemId: 'refresh-btn',
                        style: {
                            'background-color': '#71adc7'
                        },
                        text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                        icon: '/apps/sky/build/resources/images/form/restore.png'
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});