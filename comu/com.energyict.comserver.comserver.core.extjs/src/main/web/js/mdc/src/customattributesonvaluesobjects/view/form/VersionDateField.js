/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.view.form.VersionDateField', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.custom-attributes-version-date-field',
    required: false,
    groupName: 'default',
    width: 800,
    labelWidth: 250,

    dateTimeSeparator: Uni.I18n.translate('general.lowercase.at', 'MDC', 'at'),
    hoursMinutesSeparator: ':',

    requires: [
        'Uni.form.field.DateTime'
    ],

    initComponent: function () {
        var me = this;

        me.previousValue = null;

        me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'right'
                },
                items: [
                    {
                        itemId: 'version-date-radiogroup',
                        xtype: 'radiogroup',
                        columns: 1,
                        required: true,
                        vertical: true,
                        defaults: {
                            name: me.groupName,
                            submitValue: false
                        },
                        items: [
                            {
                                itemId: 'none-target',
                                boxLabel: Uni.I18n.translate('general.none', 'MDC', 'None'),
                                name: me.groupName,
                                inputValue: true,
                                checked: true
                            },
                            {
                                itemId: 'on-target',
                                boxLabel: Uni.I18n.translate('general.on', 'MDC', 'On'),
                                name: me.groupName,
                                inputValue: false
                            }
                        ],
                        listeners: {
                            change: function (field, newValue) {
                                var uploadFileDateContainer = me.down('#version-date-time-container');

                                if (newValue[me.groupName]) {
                                    uploadFileDateContainer.disable();
                                } else {
                                    uploadFileDateContainer.enable();
                                    if (!me.getValue()) {
                                        uploadFileDateContainer.setValue(moment().startOf('day').add('days', 1));
                                    }
                                }
                                me.fireEvent('change');
                            }
                        }
                    },
                    {
                        xtype: 'date-time',
                        itemId: 'version-date-time-container',
                        layout: 'hbox',
                        disabled: true,
                        margin: '30 0 0 0',
                        dateConfig: {
                            width: 120
                        },
                        hoursConfig: {
                            width: 60
                        },
                        minutesConfig: {
                            width: 60
                        },
                        separatorConfig: {
                            html: '<span style="color: #686868;">' + this.hoursMinutesSeparator + '</span>'
                        },
                        dateTimeSeparatorConfig: {
                            html: '<span style="color: #686868;">' + this.dateTimeSeparator + '</span>'
                        }
                    }
                ]
            },
            {
                xtype: 'container',
                itemId: 'version-date-field-error-container',
                padding: '10 0 0 0',
                hidden: true,
                cls: 'x-form-invalid-under'
            }
        ];

        me.callParent(arguments);

        me.down('date-time').on('blur', function () {
            if (me.previousValue !== me.down('date-time').getValue()) {
                me.previousValue = me.down('date-time').getValue();
                me.fireEvent('change');
            }
        });
    },

    disableWithText: function () {
        this.disable();
        this.down('#date-time-separator').update('<span style="color: #686868; opacity: 0.3;">' + this.dateTimeSeparator + '</span>');
        this.down('#hours-minutes-separator').update('<span style="color: #686868; opacity: 0.3;">' + this.hoursMinutesSeparator + '</span>');
    },

    enableWithText: function () {
        this.enable();
        this.down('#date-time-separator').update('<span style="color: #686868;">' + this.dateTimeSeparator + '</span>');
        this.down('#hours-minutes-separator').update('<span style="color: #686868;">' + this.hoursMinutesSeparator + '</span>');
    },

    getValue: function () {
        var me = this,
            radiogroup = me.down('#version-date-radiogroup'),
            dateField = me.down('#version-date-time-container');

        if (radiogroup.getValue()[me.groupName]) {
            return null;
        } else {
            if (dateField.getValue()) {
                return dateField.getValue().getTime();
            } else {
                return null;
            }

        }
    },

    setValue: function (value) {
        var me = this,
            radiogroup = me.down('#version-date-radiogroup'),
            dateField = me.down('#version-date-time-container'),
            onTarget = radiogroup.down('#on-target'),
            noneTarget = radiogroup.down('#none-target');

        if (!Ext.isEmpty(value)) {
            onTarget.setValue(true);
            dateField.setValue(value);
        } else {
            noneTarget.setValue(true);
        }
    },

    getErrorContainer: function () {
        return this.down('#version-date-field-error-container')
    },

    markInvalid: function (msg) {
        Ext.suspendLayouts();
        this.getErrorContainer().update(msg);
        this.getErrorContainer().show();
        Ext.resumeLayouts(true);
    },

    clearInvalid: function () {
        this.getErrorContainer().hide();
    }
});