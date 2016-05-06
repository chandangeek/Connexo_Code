Ext.define('Mdc.timeofuse.view.SpecificationsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.tou-devicetype-specifications-form',
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
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseAllowed', 'MDC', 'Time of use allowed'),
                        name: 'timeOfUseAllowed',
                        renderer: function (value) {
                            if (value) {
                                return Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                            } else {
                                return Uni.I18n.translate('general.no', 'MDC', 'No')
                            }
                        }

                    },
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseOptions', 'MDC', 'Time of use options'),
                        itemId: 'optionsField'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    fillOptions: function (record) {
        var me = this;
        me.loadRecord(record);
    }
});
