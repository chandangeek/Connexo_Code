Ext.define('Mdc.timeofuse.view.EditSpecificationsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.tou-devicetype-edit-specs-form',
    requires: [
        'Uni.property.form.Property'
    ],
    layout: {
        type: 'form'
    },
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseAllowed', 'MDC', 'Time of use allowed'),
                        name: 'touAllowed',
                        defaultType: 'radiofield',
                        layout: 'vbox',
                        items: [
                            {
                                boxLabel: Uni.I18n.translate('general.yes', 'MDC', 'Yes'),
                                name: 'touAllowed',
                                inputValue: true
                            }, {
                                boxLabel: Uni.I18n.translate('general.no', 'MDC', 'No'),
                                name: 'touAllowed',
                                inputValue: false
                            }
                        ]
                    },
                    {
                        xtype: 'property-form',
                        fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseOptions', 'MDC', 'Time of use options'),
                        itemId: 'tou-specs-options-form'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    fillOptions: function (record) {
        //TODO: code to fill optionsfield
    }
});
