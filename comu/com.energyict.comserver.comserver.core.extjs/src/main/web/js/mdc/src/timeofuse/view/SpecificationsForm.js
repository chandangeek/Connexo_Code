/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                        name: 'isAllowed',
                        renderer: function (value) {
                            if (value) {
                                return Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                            } else {
                                return Uni.I18n.translate('general.no', 'MDC', 'No')
                            }
                        }

                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseOptions', 'MDC', 'Time of use options'),
                        itemId: 'optionsField'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    fillOptions: function (optionsRecord) {
        var me = this;
        me.loadRecord(optionsRecord);
        me.down('#optionsField').removeAll();
        if (optionsRecord.get('isAllowed')) {
            optionsRecord.allowedOptions().each(function (record) {
                me.down('#optionsField').show();
                me.down('#optionsField').add(
                    {
                        xtype: 'displayfield',
                        fieldLabel: undefined,
                        value: record.get('name'),
                        margin: '0 0 -10 0'
                    }
                );
            });
        } else {
            me.down('#optionsField').hide()
        }


    }
});
