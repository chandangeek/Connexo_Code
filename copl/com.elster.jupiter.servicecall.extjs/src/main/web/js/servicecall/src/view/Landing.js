Ext.define('Scs.view.LandingOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.scs-landing-page',
    requires: [
        'Scs.view.LandingPageForm'
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
                    title: me.serviceCallId,
                    flex: 1,
                    items: {
                        xtype: 'scs-landing-page-form',
                        margin: '0 0 0 100'
                    }
                }
            ]
        };
        this.callParent(arguments);
    }
});