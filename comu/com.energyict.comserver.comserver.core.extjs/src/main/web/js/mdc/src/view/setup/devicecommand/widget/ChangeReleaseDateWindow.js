/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommand.widget.ChangeReleaseDateWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.device-command-change-release-date',
    closable: false,
    resizable: false,
    shrinkWrapDock: true,
    requires: [
        'Uni.form.field.DateTime'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    record: null,
    draggable: false,
    modal: true,

    getDate: function () {
        return this.down('date-time[name=releaseDate]').getValue()
    },

    getRecord: function () {
        return this.down('form').getRecord()
    },

    updateRecord: function () {
        return this.down('form').updateRecord()
    },

    loadRecord: function (record) {
        var me = this;
        me.oldDate = record.get('releaseDate');
        return me.down('form').loadRecord(record)
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                margins: '32 0 0 0',
                items: [
                    {
                        xtype: 'date-time',
                        fieldLabel: Uni.I18n.translate('general.releaseDate', 'MDC', 'Release date'),
                        layout: 'hbox',
                        name: 'releaseDate',
                        valueInMilliseconds: true,
                        required: true,
                        dateConfig: {
                            width: 155,
                            editable: false,
                            minValue: new Date(),
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        },
                        hoursConfig: {
                            width: 60
                        },
                        minutesConfig: {
                            width: 60
                        },
                        dateTimeSeparatorConfig: {
                            html: Uni.I18n.translate('general.lowercase.at', 'MDC', 'at'),
                            margin: '0 6 0 6'
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: ' ',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.save','MDC','Save'),
                                ui: 'action',
                                handler: function () {
                                    if (me.record) {
                                        me.updateRecord();
                                        me.fireEvent('save', me.getDate(), me.getRecord(), me.oldDate);
                                        me.close()
                                    }
                                }
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                ui: 'link',
                                handler: function () {
                                    me.close()
                                }
                            }
                        ]
                    }
                ]
            }
        ];
        this.callParent(arguments);
        if (this.record) {
            this.loadRecord(this.record)
        }
    }
});





