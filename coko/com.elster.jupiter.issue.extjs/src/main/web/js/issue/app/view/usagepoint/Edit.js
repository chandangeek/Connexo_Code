Ext.define('Mtr.view.usagepoint.Edit', {
    extend: 'Ext.window.Window',
    alias: 'widget.usagePointEdit',
    title: 'Edit usage point',
    modal: true,
    constrain: true,
    autoDestroy: false,

    activeIndex: 0,

    cls: 'edit-window',
    layout: 'fit',

    width: 500,

    requires: [
        'Mtr.store.mock.Countries',
        'Mtr.store.AmiBillingReady',
        'Mtr.widget.QuantityField',
        'Ext.form.FieldSet'
    ],

    initComponent: function () {
        var me = this,
            countries = Ext.create('Mtr.store.mock.Countries'),
            billing = Ext.create('Mtr.store.AmiBillingReady');

        me.items = [
            {
                xtype: 'form',
                itemId: 'editform',
                layout: 'card',
                items: [
                    {
                        xtype: 'container',
                        layout: 'anchor',
                        anchor: '100%',
                        defaultType: 'textfield',
                        defaults: {
                            labelSeparator: '',
                            msgTarget: 'side',
                            anchor: '100%'
                        },
                        items: [
                            {
                                name: 'name',
                                fieldLabel: 'Name'
                            },
                            {
                                name: 'mRID',
                                fieldLabel: 'mRID'
                            },
                            {
                                xtype: 'textarea',
                                name: 'description',
                                fieldLabel: 'Description'
                            },
                            {
                                name: 'serviceCategory',
                                fieldLabel: 'Service category'
                            },
                            {
                                xtype: 'fieldset',
                                title: 'Location (changes here are not saved)',
                                defaultType: 'textfield',
                                items: [
                                    {
                                        xtype: 'container',
                                        layout: 'anchor',
                                        anchor: '100%',
                                        defaultType: 'textfield',
                                        defaults: {
                                            labelWidth: '50%',
                                            anchor: '100%'
                                        },
                                        items: [
                                            {
                                                name: 'street',
                                                fieldLabel: 'Street'
                                            },
                                            {
                                                name: 'number',
                                                fieldLabel: 'Number'
                                            },
                                            {
                                                name: 'zip',
                                                fieldLabel: 'Zip'
                                            },
                                            {
                                                name: 'town',
                                                fieldLabel: 'Town'
                                            },
                                            {
                                                name: 'state',
                                                fieldLabel: 'State'
                                            },
                                            {
                                                xtype: 'combobox',
                                                name: 'country',
                                                fieldLabel: 'Country',
                                                store: countries,
                                                queryMode: 'local',
                                                displayField: 'name',
                                                valueField: 'code'
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: 'anchor',
                        anchor: '100%',
                        defaultType: 'textfield',
                        defaults: {
                            labelWidth: '50%',
                            anchor: '100%'
                        },
                        items: [
                            {
                                xtype: 'checkbox',
                                name: 'grounded',
                                fieldLabel: 'Grounded'
                            },
                            {
                                xtype: 'checkbox',
                                name: 'minimalUsageExpected',
                                fieldLabel: 'Minimal usage expected'
                            },
                            {
                                xtype: 'quantityfield',
                                name: 'nominalServiceVoltage',
                                fieldLabel: 'Nominal service voltage'
                            },
                            {
                                xtype: 'quantityfield',
                                name: 'estimatedLoad',
                                fieldLabel: 'Estimated load'
                            },
                            {
                                xtype: 'quantityfield',
                                name: 'ratedCurrent',
                                fieldLabel: 'Rated current'
                            },
                            {
                                xtype: 'quantityfield',
                                name: 'ratedPower',
                                fieldLabel: 'Rated power'
                            },
                            {
                                name: 'readCycle',
                                fieldLabel: 'Read cycle'
                            },
                            {
                                name: 'readRoute',
                                fieldLabel: 'Read route'
                            }
                        ]
                    },
                    {

                        xtype: 'container',
                        layout: 'anchor',
                        anchor: '100%',
                        defaultType: 'textfield',
                        defaults: {
                            labelWidth: '50%',
                            anchor: '100%'
                        },
                        items: [
                            {
                                name: 'version',
                                fieldLabel: 'Version'
                            },
                            {
                                xtype: 'combobox',
                                name: 'amiBillingReady',
                                fieldLabel: 'Billing ready',
                                store: billing,
                                queryMode: 'local',
                                displayField: 'display',
                                valueField: 'value'
                            },
                            {
                                xtype: 'checkbox',
                                name: 'isSdp',
                                fieldLabel: 'Service delivery point'
                            },
                            {
                                xtype: 'checkbox',
                                name: 'isVirtual',
                                fieldLabel: 'Virtual'
                            },
                            {
                                name: 'createTime',
                                fieldLabel: 'Creation time'
                            },
                            {
                                name: 'modTime',
                                fieldLabel: 'Modification time'
                            }
                        ]
                    }
                ]
            }
        ];

        me.bbar = [
            {
                text: '&laquo; Previous',
                action: 'prev',
                scope: me,
                handler: me.prevStep
            },
            {
                text: 'Next &raquo;',
                action: 'next',
                scope: me,
                handler: me.nextStep
            },
            {
                xtype: 'component',
                flex: 1
            },
            {
                text: 'Cancel',
                scope: me,
                handler: me.close
            },
            {
                text: 'Save',
                action: 'update'
            }
        ];

        me.callParent(arguments);
    },

    prevStep: function () {
        var editForm = this.getEditForm(),
            layout = editForm.getLayout();

        if (this.activeIndex - 1 >= 0) {
            --this.activeIndex;
        }
        this.checkNavButtons();

        layout.setActiveItem(this.activeIndex);
        this.activeIndex = editForm.items.indexOf(layout.getActiveItem());
    },
    nextStep: function () {
        var editForm = this.getEditForm(),
            layout = editForm.getLayout();

        if (this.activeIndex + 1 < editForm.items.length) {
            ++this.activeIndex;
        }
        this.checkNavButtons();

        layout.setActiveItem(this.activeIndex);
        this.activeIndex = editForm.items.indexOf(layout.getActiveItem());
    },
    checkNavButtons: function () {
        var editForm = this.getEditForm(),
            prevButton = this.down('button[action=prev]'),
            nextButton = this.down('button[action=next]');

        if (this.activeIndex - 1 >= 0) {
            prevButton.enable();
        } else {
            prevButton.disable();
        }

        if (this.activeIndex + 1 < editForm.items.length) {
            nextButton.enable();
        } else {
            nextButton.disable();
        }
    },

    showUsagePoint: function (usagePoint) {
        var me = this,
            editForm = this.getEditForm(),
            layout = editForm.getLayout();

        editForm.loadRecord(usagePoint);

        layout.setActiveItem(0);
        me.activeIndex = editForm.items.indexOf(layout.getActiveItem());
        me.checkNavButtons();

        this.show();
        this.center();
    },

    showGeneralInfo: function () {
        this.showInfoCard(0);
    },
    showTechInfo: function () {
        this.showInfoCard(1);
    },
    showOtherInfo: function () {
        this.showInfoCard(2);
    },
    showInfoCard: function (index) {
        var editForm = this.getEditForm(),
            layout = editForm.getLayout();

        layout.setActiveItem(index);
        this.activeIndex = editForm.items.indexOf(layout.getActiveItem());
        this.checkNavButtons();
    },

    getEditForm: function () {
        return this.down('#editform');
    },
    getUsagePoint: function () {
        return this.getEditForm().getRecord();
    },
    getValues: function () {
        return this.getEditForm().getValues(false, false, false, true);
    }
});

