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
                                text: Uni.I18n.translate('relativeperiod.form.startdate', 'TME', 'Define the start of the relative period'),
                                margin: '8 0 12 0'
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
                                text: Uni.I18n.translate('relativeperiod.form.enddate', 'TME', 'Define the end of the relative period'),
                                margin: '8 0 12 0'
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
                                text: Uni.I18n.translate('relativeperiod.form.preview', 'TME', 'Preview'),
                                margin: '8 0 12 0'
                            },
                            {
                                xtype: 'uni-form-relativeperiodpreview'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);

        me.on('afterrender', me.onAfterRender, me);
    },

    onAfterRender: function () {
        var me = this;

        me.getStartRelativePeriodField().on('periodchange', me.updatePreview, me);
        me.getEndRelativePeriodField().on('periodchange', me.updatePreview, me);
        me.updatePreview();
    },

    updatePreview: function () {
        var me = this;

        me.getRelativePeriodPreview().updateStartPeriodValue(me.getStartRelativePeriodField().getValue());
        me.getRelativePeriodPreview().updateEndPeriodValue(me.getEndRelativePeriodField().getValue());
        me.getRelativePeriodPreview().updatePreview();
    },

    getStartRelativePeriodField: function () {
        return this.down('uni-form-relativeperiod:first');
    },

    getEndRelativePeriodField: function () {
        return this.down('uni-form-relativeperiod:last');
    },

    getRelativePeriodPreview: function () {
        return this.down('uni-form-relativeperiodpreview');
    }
});
