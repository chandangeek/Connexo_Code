Ext.define('Scs.view.Landing', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.scs-landing-page',
    requires: [
        'Scs.view.PreviewForm',
        'Scs.view.ActionMenu',
        'Scs.view.log.Setup'
    ],
    serviceCallId: null,
    router: null,
    record: null,

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: me.serviceCallId,
            items: [
                {
                    tools: [
                        {
                            xtype: 'button',
                            privileges: Scs.privileges.ServiceCall.admin,
                            text: Uni.I18n.translate('general.actions', 'SCS', 'Actions'),
                            iconCls: 'x-uni-action-iconD',
                            disabled: !me.record.get('canCancel'),
                            margin: '20 0 0 0',
                            menu: {
                                xtype: 'scs-action-menu',
                                record: me.record,
                            }
                        }
                    ],
                    flex: 1,
                    items: [
                        {
                            xtype: 'servicecalls-preview-form',
                            itemId: 'service-call-landing-page-form',
                            router: me.router,
                            detailed: true
                        },
                        {
                            xtype: 'scs-log-setup',
                            itemId: 'serviceCallLog'
                        }
                    ]
                }

            ]
        };
        this.callParent(arguments);
    },

    updateLandingPage: function (record) {
        var me = this,
            previewForm = me.down('#service-call-landing-page-form');
        previewForm.updatePreview(record);
    }
});