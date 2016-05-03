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
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseAllowed', 'MDC', 'Time of use allowed'),
                        name: 'timeOfUseAllowed',
                        defaultType: 'radiofield',
                        layout: 'vbox',
                        items: [
                            {
                                boxLabel: Uni.I18n.translate('general.yes', 'MDC', 'Yes'),
                                name: 'timeOfUseAllowed',
                                inputValue: true
                            }, {
                                boxLabel: Uni.I18n.translate('general.no', 'MDC', 'No'),
                                name: 'timeOfUseAllowed',
                                inputValue: false
                            }
                        ]
                    },
                    {
                        xtype: 'property-form',
                        fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseOptions', 'MDC', 'Time of use options'),
                        itemId: 'tou-specs-options-form'
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'tou-edit-button',
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
        //TODO: code to fill optionsfield
    }
});
