Ext.define('Sam.view.licensing.Details', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.property.form.Property'
    ],
    alias: 'widget.licensing-details',
    frame: true,
    title: '&nbsp;',
    items: [
        {
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5
            },
            items: [
                {
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('licensing.application', 'SAM', 'Application'),
                            name: 'applicationname'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('licensing.type', 'SAM', 'Type'),
                            name: 'type'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('licensing.description', 'SAM', 'Description'),
                            name: 'description'
                        }
                    ]
                },
                {
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('general.status', 'SAM', 'Status'),
                            name: 'status'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('licensing.activationDate', 'SAM', 'Activation date'),
                            name: 'validfrom',
                            renderer: function (value) {
                                return value ? Uni.DateTime.formatDateLong(value) : '';
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('licensing.expirationDate', 'SAM', 'Expiration date'),
                            name: 'expires',
                            renderer: function (value) {
                                return value ? Uni.DateTime.formatDateLong(value) : '';
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('licensing.gracePeriod', 'SAM', 'Grace period'),
                            name: 'graceperiod',
                            renderer: function (value) {
                                return value ? Uni.I18n.translatePlural('licensing.days', value, 'SAM', '0 days', '{0} day', '{0} days') : '';
                            }
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('licensing.licenseCoverage', 'SAM', 'License coverage'),
            itemId: 'license-coverage-container',
            labelAlign: 'top',
            layout: 'form',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 250
            },
            hidden: true
        }
    ]
});

