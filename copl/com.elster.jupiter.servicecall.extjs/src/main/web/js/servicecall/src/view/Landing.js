Ext.define('Scs.view.Landing', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.scs-landing-page',
    requires: [
        'Scs.view.LandingPageForm',
        'Scs.view.ActionMenu'
    ],
    serviceCallId: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    title: me.title === 'none' ? '' : me.serviceCallId,
                    flex: 1,
                    items: {
                        xtype: 'scs-landing-page-form',
                        margin: '0 0 0 100'
                    }
                },
                {
                    xtype: 'button',
                    //privileges: Apr.privileges.AppServer.admin,
                    text: Uni.I18n.translate('general.actions', 'SCS', 'Actions'),
                    iconCls: 'x-uni-action-iconD',
                    margin: '20 0 0 0',
                    menu: {
                        xtype: 'scs-action-menu'
                    }
                }
            ]
        };
        this.callParent(arguments);
    }
});