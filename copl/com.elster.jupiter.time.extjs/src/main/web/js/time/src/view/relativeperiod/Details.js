Ext.define('Tme.view.relativeperiod.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.relative-periods-details',
    requires: [
        'Tme.view.relativeperiod.Menu',
        'Tme.view.relativeperiod.PreviewForm',
        'Tme.view.relativeperiod.ActionMenu',
        'Uni.form.RelativePeriodPreview'
    ],

    router: null,

    content: {
        xtype: 'container',
        layout: 'hbox',
        items: [
            {
                ui: 'large',
                title: Uni.I18n.translate('general.overview', 'TME', 'Overview'),
                flex: 1,
                items: [
                    {
                        xtype: 'relative-periods-preview-form'
                    },
                    {
                        xtype: 'uni-form-relativeperiodpreview'
                    }
                ]
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'TME', 'Actions'),
                privileges : Tme.privileges.Period.admin,
                iconCls: 'x-uni-action-iconD',
                margin: '20 0 0 0',
                menu: {
                    xtype: 'relative-periods-action-menu'
                }
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'relative-periods-menu',
                        router: me.router,
                        toggle: 0
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


