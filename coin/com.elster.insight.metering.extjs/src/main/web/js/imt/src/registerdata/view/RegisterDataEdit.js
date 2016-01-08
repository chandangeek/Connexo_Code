Ext.define('Imt.registerdata.view.RegisterDataEdit', {
    extend: 'Imt.registerdata.view.RegisterDataMainEdit',
    alias: 'widget.registerDataEdit',
    itemId: 'registerDataEdit',

    requires: [
        'Uni.form.field.DateTime'
    ],

    setEdit: function (edit, returnLink) {
        var me = this;
        me.callParent(arguments);

        if (me.isEdit()) {
            me.down('#timeStampDisplayField').setDisabled(false);
            me.down('#timeStampDisplayField').show();
            me.down('#timeStampContainer').hide();
            me.down('#timeStampContainer').setDisabled(true);
        } else {
            me.down('#timeStampDisplayField').hide();
            me.down('#timeStampDisplayField').setDisabled(true);
            me.down('#timeStampContainer').setDisabled(false);
            me.down('#timeStampContainer').show();
        }
    },

    setValues: function (record) {
        var me = this;
        if (!Ext.isEmpty(record.get("unitOfMeasure"))) {
            me.down('#valueUnitDisplayField').setValue(record.get("unitOfMeasure"));
        }
    },

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                itemId: 'registerDataEditForm',
                defaults: {
                    labelWidth: 200,
                    labelAlign: 'right'
                },
                items: [
                    {
                        name: 'errors',
                        ui: 'form-error-framed',
                        itemId: 'registerDataEditFormErrors',
                        layout: 'hbox',
                        margin: '0 0 10 0',
                        hidden: true,
                        defaults: {
                            xtype: 'container'
                        }
                    },
                    {
                        xtype: 'displayfield',
                        name: 'readingTime',
                        fieldLabel: Uni.I18n.translate('registerdata.measurementTime', 'IMT', 'Measurement time'),
                        itemId: 'timeStampDisplayField',
                        format: 'M j, Y \\a\\t G:i',
                        renderer: function (value) {
                            if(!Ext.isEmpty(value)) {
                                return Ext.util.Format.date(new Date(value), this.format);
                            }
                        },
                        submitValue: true,
                        hidden: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'timeStampContainer',
                        required: true,
                        fieldLabel: Uni.I18n.translate('registerdata.measurementTime', 'IMT', 'Measurement time'),
                        defaults: {
                            width: '100%'
                        },
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'timeStampEditField',
                                name: 'readingTime',
                                layout: 'hbox',
                                valueInMilliseconds: true
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'valueContainer',
                        required: true,
                        fieldLabel: Uni.I18n.translate('registerdata.value', 'IMT', 'Value'),
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'value',
                                maskRe: /[0-9\.]+/,
                                itemId: 'valueTextField',
                                margin: '0 5 0 0',
                                allowBlank: false
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'valueUnitDisplayField'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                action: 'addRegisterDataAction',
                                itemId: 'addEditButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelLink',
                                href: '#/search?restore=true'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
        me.setEdit(me.isEdit(), me.returnLink);
    }
});

