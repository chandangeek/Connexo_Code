Ext.define('Ddv.view.ValidationOverview', {
    extend: 'Ext.container.Container',
    requires: [
        'Dsh.view.widget.HeaderSection',
        'Ddv.view.Setup',
        'Uni.util.FormEmptyMessage'
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
                        xtype: 'uni-form-empty-message',
                        itemId: 'ctr-no-group-selected',
                        hidden: me.hiddenNoGroup,
                        text: Uni.I18n.translate('validation.validationOverview.noGroup', 'DDV', 'No device group has been selected yet.')
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});