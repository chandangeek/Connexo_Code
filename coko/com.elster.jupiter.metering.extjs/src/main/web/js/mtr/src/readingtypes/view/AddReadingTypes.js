/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.AddReadingTypes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-reading-types',
    itemId: 'add-reading-types',

    requires: [
        'Mtr.readingtypes.view.AddReadingTypesForm'
    ],

    addCount: 0,

    loadRecord: function (record) {
        var me = this;
        me.down('add-reading-types-form').loadRecord(record)
    },

    setAddCount: function (count) {
        this.addCount = count;
        this.down('#add-reading-types-count').setValue(Uni.I18n.translatePlural('readingtypesmanagment.addreadingtypes.countMsg',
                count, 'MTR', 'No reading types will be added',
                'You are going to add {0} reading type. 1000 is the limit', 'You are going to add {0} reading types. 1000 is the limit')
        );
    },

    updateAddCount: function (data) {
        var me = this,
            count = 0;
        delete data.mRID;
        delete data.aliasName;
        delete data.specifyBy;
        for (key in data) {
            if (data[key] instanceof Array) {
                if (!Ext.isEmpty(data[key][0])) {
                    !count && (count = 1);
                    count = count * data[key].length;
                }
            }
        }
        me.setAddCount(count);
    },

    getFormData: function (form) {
        var record = form.updateRecord().getRecord();
        return record.getData();
    },

    initComponent: function () {
        var me = this;
        me.content = {
            xtype: 'form',
            ui: 'large',
            title: Uni.I18n.translate('readingtypesmanagment.addreadingtypes.title', 'MTR', 'Add reading types'),
            items: [
                {
                    xtype: 'add-reading-types-form',
                    listeners: {
                        change: function (form) {
                            me.updateAddCount(me.getFormData(form))
                        },
                        switchmode: function (mode, form) {
                            if (mode == 'cim') {
                                me.setAddCount(1)
                            } else if (mode == 'form') {
                                me.updateAddCount(me.getFormData(form))
                            }
                        }
                    }
                },
                {
                    xtype: 'displayfield',
                    labelWidth: 250,
                    fieldLabel: '&nbsp',
                    itemId: 'add-reading-types-description-of-attributes-info',
                    value: Uni.I18n.translate('readingtypesmanagment.addreadingtypes.cimFormDescriptionOfAttributesInfo', 'MTR', 'Description of attributes can be found in CIM documentation'),
                    renderer: function(value){
                        return '<i>' + value + '</i>'
                    }
                },
                {
                    xtype: 'displayfield',
                    labelWidth: 250,
                    fieldLabel: '&nbsp',
                    fieldCls: 'x-panel-body-form-error',
                    itemId: 'add-reading-types-count',
                    value: Uni.I18n.translate('readingtypesmanagment.addreadingtypes.defaultCountMsg', 'MTR', 'You are going to add 1 reading type. 1000 is the limit')
                },
                {
                    xtype: 'fieldcontainer',
                    labelWidth: 250,
                    fieldLabel: '&nbsp',
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'MTR', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'add',
                            itemId: 'add-reading-types-add-button'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MTR', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'add-reading-type-cancel-button',
                            href: ''
                        }
                    ]
                }
            ]
        };
        me.callParent(arguments);
    }
});