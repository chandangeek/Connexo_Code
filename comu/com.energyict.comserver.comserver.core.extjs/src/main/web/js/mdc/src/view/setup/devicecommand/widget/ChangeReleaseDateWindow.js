Ext.define('Mdc.view.setup.devicecommand.widget.ChangeReleaseDateWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.device-command-change-release-date',
    closable: false,
    requires: [
        'Mdc.widget.DateTimeField'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    record: null,
    draggable: false,
    modal: true,

    getDate: function () {
        return this.down('dateTimeField[name=releaseDate]').getValue()
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
                        xtype: 'dateTimeField',
                        name: 'releaseDate',
                        required: true,
                        fieldLabel: Uni.I18n.translate('deviceCommand.add.releaseDate', 'MDC', 'Release date'),
                        dateCfg: {
                            width: 155
                        },
                        hourCfg: {
                            width: 60
                        },
                        minuteCfg: {
                            width: 60
                        }
                    }
                ],
                buttons: [
                    {
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
                        text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                        ui: 'link',
                        handler: function () {
                            me.close()
                        }
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





