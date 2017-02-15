/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.EditSpecificationsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.tou-devicetype-edit-specs-form',
    requires: [
        'Uni.property.form.Property',
        'Uni.util.FormErrorMessage'
    ],
    layout: {
        type: 'form'
    },
    defaults: {
        labelWidth: 250
    },
    deviceTypeId: null,
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
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true,
                        width: 750
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'tou-allowed-radio-group',
                        fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseAllowed', 'MDC', 'Time of use allowed'),
                        defaultType: 'radiofield',
                        required: true,
                        layout: 'vbox',
                        items: [
                            {
                                boxLabel: Uni.I18n.translate('general.yes', 'MDC', 'Yes'),
                                name: 'isAllowed',
                                inputValue: 'true',
                                itemId: 'tou-allowed-radio-field',
                            }, {
                                boxLabel: Uni.I18n.translate('general.no', 'MDC', 'No'),
                                name: 'isAllowed',
                                inputValue: 'false',
                                checked: true
                            }
                        ]
                    },
                    {
                        xtype: 'checkboxgroup',
                        layout: 'vbox',
                        fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseOptions', 'MDC', 'Time of use options'),
                        itemId: 'tou-specs-options-form',
                        required: true
                    },
                    {
                        xtype: 'label',
                        cls: 'x-form-invalid-under',
                        itemId: 'no-checkboxes-time-of-use-selected',
                        margin: '0 0 0 275',
                        hidden: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'tou-save-specs-button',
                                text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'tou-edit-cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                ui: 'link',
                                href: '#/administration/devicetypes/' + me.deviceTypeId + '/timeofuse'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    fillOptions: function (record) {
        var me = this;
        me.loadRecord(record);

        Ext.suspendLayouts();

        me.down('#tou-specs-options-form').removeAll();
        record.supportedOptions().each(function (option) {
            me.down('#tou-specs-options-form').add(
                {
                    boxLabel: option.get('name'),
                    inputValue: option.get('id'),
                    checked: record.allowedOptions().findExact('id', option.get('id')) >= 0
                }
            );
        });

        me.down('#tou-specs-options-form').setDisabled(!record.get('isAllowed'));
        me.doComponentLayout();
        Ext.resumeLayouts(true);
        me.updateLayout();
        me.doLayout();
    }
});
