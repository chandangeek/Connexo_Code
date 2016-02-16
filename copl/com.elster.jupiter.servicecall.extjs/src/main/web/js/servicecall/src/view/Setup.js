Ext.define('Scs.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.servicecalls-setup',
    router: null,

    requires: [
        'Scs.view.ServiceCallPreviewContainer',
        'Scs.view.ServiceCallFilter'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls'),
            items: [
                {
                    xtype: 'service-call-filter'
                },
                {
                    xtype: 'service-call-preview-container',
                    router: me.router
                }
            ]
        };


        me.callParent(arguments);
    }
});