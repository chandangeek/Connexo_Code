Ext.define('Ddv.view.ValidationOverview', {
    extend: 'Ext.container.Container',
    requires: [
        'Dsh.view.widget.HeaderSection',
        'Ddv.view.Setup'
    ],
    alias: 'widget.ddv-validation-overview',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        padding: '0 20px'
    },
    defaults: {
        style: {
            marginBottom: '20px',
            padding: 0
        }
    },
    hiddenGrid: true,
    hiddenNoGroup: true,
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'header-section',
                router: me.router,
                style: 'none'
            },
            {
                xtype: 'panel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'ddv-validationoverview-setup',
                        hidden: me.hiddenGrid
                    },
                    {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-group-selected',
                        hidden: me.hiddenNoGroup,
                        style: {
                            marginRight: '17px',
                            padding: '17px'
                        },
                        title: Uni.I18n.translate('validation.validationOverview.noGroupSelected', 'MDC', 'No group selected'),
                        reasons: [
                            Uni.I18n.translate('validation.validationOverview.noGroup.list.item', 'MDC', 'No device group has been selected yet.')
                        ]
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});