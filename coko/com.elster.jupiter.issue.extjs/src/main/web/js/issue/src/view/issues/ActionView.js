Ext.define('Isu.view.issues.ActionView', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.util.FormErrorMessage'
    ],
    alias: 'widget.issue-action-view',
    router: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                itemId: 'issue-action-view-form',
                title: '&nbsp;',
                ui: 'large',
                defaults: {
                    labelWidth: 150,
                    width: 700
                },
                items: [
                    {
                        itemId: 'issue-action-view-form-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true
                    }
                ],
                buttons: [
                    {
                        itemId: 'issue-action-apply',
                        ui: 'action',
                        text: Uni.I18n.translate('general.apply', 'ISU', 'Apply'),
                        action: 'applyAction'
                    },
                    {
                        itemId: 'issue-action-cancel',
                        text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
                        ui: 'link',
                        action: 'cancelAction',
                        href: me.router.getRoute(me.router.currentRoute.replace('/action', '')).buildUrl()
                    }
                ],
                updateRecord: function (record) {
                    var basic = this.getForm();

                    record = record || basic._record;
                    if (!record) {
                        //<debug>
                        Ext.Error.raise("A record is required.");
                        //</debug>
                        return basic;
                    }

                    record.set('parameters', this.getValues());

                    return basic;
                }
            }
        ];

        me.callParent(arguments);
    }
});