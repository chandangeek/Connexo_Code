Ext.define('Tme.view.relativeperiod.Edit', {
    extend: 'Ext.container.Container',
    xtype: 'tme-relativeperiod-edit',
    overflowY: 'auto',

    requires: [
        'Uni.form.RelativePeriod',
        'Uni.form.RelativePeriodPreview',
        'Tme.store.RelativePeriodCategories',
        'Uni.form.field.DateTime'
    ],

    edit: false,

    isEdit: function () {
        return this.edit;
    },

    categoryStore: undefined,

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'TME', 'Save'));
            this.down('#createEditButton').action = 'editRelativePeriod';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'TME', 'Add'));
            this.down('#createEditButton').action = 'addRelativePeriod';
        }
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        var me = this;

        me.categoryStore = Ext.getStore('Tme.store.RelativePeriodCategories');

        me.items = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('relativeperiod.add', 'TME', 'Add relative period'),
                ui: 'large',
                margin: '0px 16px 16px 16px',
                layout: {
                    type: 'vbox'
                },
                items: [
                    {
                        xtype: 'form',
                        defaults: {
                            labelWidth: 160,
                            validateOnChange: false,
                            validateOnBlur: false,
                            anchor: '100%'
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('relativeperiod.name', 'TME', 'Name')
                            },
                            {
                                xtype: 'combobox',
                                itemId: 'categorise-combo-box',
                                width: 600,
                                name: 'category',
                                store: me.categoryStore,
                                editable: false,
                                multiSelect: true,
                                required: true,
                                allowBlank: false,
                                queryMode: 'local',
                                triggerAction: 'all',
                                fieldLabel: Uni.I18n.translate('relativeperiod.category', 'TME', 'Category'),
                                emptyText: Uni.I18n.translate('relativeperiod.form.selectcategory', 'TME', 'Select 1 or more categories'),
                                displayField: 'name',
                                valueField: 'id',
                                listConfig: {
                                    getInnerTpl: function () {
                                        return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {' + this.displayField + '}</div>';
                                    }
                                }
                            },
                            {
                                xtype: 'label',
                                text: Uni.I18n.translate('relativeperiod.form.startdate', 'TME', 'Define the start of the relative period')
                            },
                            {
                                xtype: 'uni-form-relativeperiod',
                                startPeriodCfg: {
                                    fieldLabel: 'Start',
                                    showOptionNow: false
                                },
                                defaults: {
                                    labelWidth: 160
                                }
                            },
                            {
                                xtype: 'label',
                                text: Uni.I18n.translate('relativeperiod.form.enddate', 'TME', 'Define the end of the relative period')
                            },
                            {
                                xtype: 'uni-form-relativeperiod',
                                startPeriodCfg: {
                                    fieldLabel: 'End',
                                    showOptionDate: false
                                },
                                defaults: {
                                    labelWidth: 160
                                }
                            },
                            {
                                xtype: 'label',
                                text: Uni.I18n.translate('relativeperiod.form.preview', 'TME', 'Preview')
                            },
                            // TODO
//                            ,
//                            {
//                                xtype: 'uni-form-relativeperiodpreview'
//                            }
                            {
                                xtype: 'fieldcontainer',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'label',
                                        itemId: 'reference-date-preview-label',
                                        text: '',
                                        margin: '10 10 10 40',
                                        cls: Ext.baseCSSPrefix + 'form-cb-label'
                                    }
                                ]
                            },

                            {
                                xtype: 'fieldcontainer',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'label',
                                        itemId: 'preview-label-before',
                                        text: Uni.I18n.translate('relativeperiod.form.referencedete.setberore', 'TME', 'This relative period is defined using'),
                                        margin: '10 20 30 40',
                                        cls: Ext.baseCSSPrefix + 'form-cb-label'
                                    },
                                    {
                                        xtype: 'date-time',
                                        itemId: 'start-on',
                                        layout: 'hbox',
                                        name: 'start-on',
                                        dateConfig: {
                                            allowBlank: true
                                        },
                                        hoursConfig: {
                                            fieldLabel: Uni.I18n.translate('general.at', 'DXP', 'at'),
                                            labelWidth: 10,
                                            margin: '0 0 0 10'
                                        },
                                        minutesConfig: {
                                            width: 55
                                        }
                                    },
                                    {
                                        xtype: 'label',
                                        itemId: 'preview-label-after',
                                        text: Uni.I18n.translate('relativeperiod.form.referencedete.setafter', 'TME', 'as reference'),
                                        margin: '10 30 20 10',
                                        cls: Ext.baseCSSPrefix + 'form-cb-label'
                                    },
                                    {
                                        xtype: 'button',
                                        tooltip: Uni.I18n.translate('relativeperiod.form.referencedete.tooltip', 'TME', 'You can change the reference to define another relative period'),
                                        iconCls: 'icon-info-small',
                                        ui: 'blank',
                                        itemId: 'latestReadingHelp',
                                        shadow: false,
                                        margin: '6 0 0 10',
                                        width: 16
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
