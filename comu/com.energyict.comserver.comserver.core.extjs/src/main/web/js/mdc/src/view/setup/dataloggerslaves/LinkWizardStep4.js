Ext.define('Mdc.view.setup.dataloggerslaves.LinkWizardStep4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataloggerslave-link-wizard-step4',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.model.Register',
        'Uni.form.field.DateTime'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'mdc-dataloggerslave-link-wizard-step4-errors',
                xtype: 'uni-form-error-message',
                width: 1050,
                hidden: true
            },
            {
                xtype: 'form',
                itemId: 'mdc-dataloggerslave-link-wizard-step4-form',
                fieldLabel: '',
                layout: {
                    type: 'vbox'
                },
                items: [
                    {
                        xtype: 'date-time',
                        layout: 'hbox',
                        itemId: 'mdc-step4-linking-date',
                        required: true,
                        margin: '20 0 20 0',
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('general.linkingDate', 'MDC', 'Linking date'),
                        width: 1050
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    initialize: function(minimalArrivalDateInMillis, arrivalDateToSuggest) {
        var me = this;
        if (me.rendered) {
            me.doConfigureArrivalDate(minimalArrivalDateInMillis, arrivalDateToSuggest);
        } else {
            me.on('afterrender', function() {
                me.doConfigureArrivalDate(minimalArrivalDateInMillis, arrivalDateToSuggest);
            }, me, {single:true});
        }
    },

    doConfigureArrivalDate: function(minimalLinkingDateInMillis, linkingDateToSuggest) {
        var me = this;

        me.down('#mdc-step4-linking-date').setMinValue(minimalLinkingDateInMillis);
        me.down('#mdc-step4-linking-date').setValue(linkingDateToSuggest);
    }
});
