Ext.define('Isu.view.issues.ActionForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property'
    ],
    alias: 'widget.issue-action-form',
    router: null,
    defaults: {
        labelWidth: 260,
        width: 595
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'issue-action-view-form-errors',
                xtype: 'uni-form-error-message',
                hidden: true
            },
            {
                itemId: 'property-form',
                xtype: 'property-form',
                defaults: {
                    labelWidth: me.defaults.labelWidth,
                    width: 320,
                    resetButtonHidden: true
                }
            },
            {
                xtype: 'fieldcontainer',
                ui: 'actions',
                fieldLabel: ' ',
                defaultType: 'button',
                items: [
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
                ]
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;

        me.callParent(arguments);
        me.setTitle(record.get('name'));
        me.down('property-form').loadRecord(record);
    },

    updateRecord: function () {
        var me = this,
            propertyForm = me.down('property-form'),
            record;

        me.callParent(arguments);
        record = me.getRecord();
        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            record.propertiesStore = propertyForm.getRecord().properties();
        }
    }
});