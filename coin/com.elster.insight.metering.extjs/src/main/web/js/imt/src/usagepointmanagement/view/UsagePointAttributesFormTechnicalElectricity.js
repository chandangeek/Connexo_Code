Ext.define('Imt.usagepointmanagement.view.UsagePointAttributesFormTechnicalElectricity', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagePointAttributesFormTechnicalElectricity',

    
    requires: [
        'Uni.form.field.Duration'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        labelWidth: 150,
        xtype: 'displayfield'
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {xtype: 'usagePointAttributesFormMain'},
            {
                xtype: 'title-with-edit-button',
                title: Uni.I18n.translate('usagepoint.technical.information', 'IMT', 'Technical information'),
                editHandler: function(){
                    me.down('#usagePointTechnicalAttributes').hide();
                    me.down('#editable-form-electricity').show();
                    me.down('#bottom-buttons-electricity').show();
                    Imt.customattributesonvaluesobjects.service.ActionMenuManager.setdisabledAllEditBtns(true);
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'usagePointTechnicalAttributes',
                labelAlign: 'top',
                layout: 'vbox',
                margin: '0',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250,
                    width: 600
                },
                items: [
                    {
                        name: 'grounded',
                        itemId: 'fld-up-grounded',
                        fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                        }
                    },
                    {
                        name: 'nominalServiceVoltage',
                        itemId: 'fld-up-service-voltage',
                        fieldLabel: Uni.I18n.translate('general.label.voltage', 'IMT', 'Nominal voltage'),
                        renderer: function (data) {
                            return me.renderValue(data);
                        }
                    },
                    {
                        name: 'phaseCode',
                        itemId: 'fld-up-phase',
                        fieldLabel: Uni.I18n.translate('general.label.phaseCode', 'IMT', 'Phase code'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'ratedCurrent',
                        itemId: 'fld-up-rated-current',
                        fieldLabel: Uni.I18n.translate('general.label.ratedCurrent', 'IMT', 'Rated current'),
                        renderer: function (data) {
                            return me.renderValue(data);
                        }
                    },
                    {
                        name: 'ratedPower',
                        itemId: 'fld-up-rated-power',
                        fieldLabel: Uni.I18n.translate('general.label.ratedPower', 'IMT', 'Rated power'),
                        renderer: function (data) {
                            return me.renderValue(data);
                        }
                    },
                    {
                        name: 'estimatedLoad',
                        itemId: 'fld-up-estimated-load',
                        fieldLabel: Uni.I18n.translate('general.label.estimatedLoad', 'IMT', 'Estimated load'),
                        renderer: function (data) {
                            return me.renderValue(data);
                        }
                    }
                ]
            },
            {
                xtype: 'form',
                itemId: 'editable-form-electricity',
                hidden: true,
                defaults: {
                    //xtype: 'displayfield',
                    labelWidth: 250,
                    //width: 600
                },
                items: [
                    {
                        xtype: 'combobox',
                        fieldLabel: 'ad'
                    }
                ]
            },
            {
                xtype: 'container',
                hidden: true,
                itemId: 'bottom-buttons-electricity',
                dock: 'bottom',
                margin: '20 0 0 265',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'button',
                        //itemId: 'channelCustomAttributesSaveBtn',
                        ui: 'action',
                        text: Uni.I18n.translate('general.save', 'IMT', 'Save'),
                        //handler: function () {
                        //    me.down('property-form').updateRecord();
                        //
                        //    me.record.save({
                        //        callback: function(record, response, success){
                        //            if(success){
                        //                me.down('property-form').makeNotEditable(me.record);
                        //                me.down('#bottom-buttons').hide();
                        //            } else {
                        //                var responseText = Ext.decode(response.response.responseText, true);
                        //                if (responseText && Ext.isArray(responseText.errors)) {
                        //                    me.down('property-form').markInvalid(responseText.errors);
                        //                }
                        //            }
                        //
                        //        }
                        //        //failure: function(record, response){
                        //        //
                        //        //        var responseText = Ext.decode(response.response.responseText, true);
                        //        //    console.log(responseText);
                        //        //
                        //        //        if (responseText && Ext.isArray(responseText.errors)) {
                        //        //            me.down('property-form').markInvalid(responseText.errors);
                        //        //
                        //        //        }
                        //        //    //me.down('property-form').markInvalid(errors);
                        //        //}
                        //    });
                        //}
                    },
                    //{
                    //    xtype: 'button',
                    //    text: Uni.I18n.translate('general.restoretodefaults', 'IMT', 'Restore to defaults'),
                    //    icon: '../sky/build/resources/images/form/restore.png',
                    //    //handler: function () {
                    //    //    me.down('property-form').restoreAll();
                    //    //}
                    //    //itemId: 'channelCustomAttributesRestoreBtn'
                    //},
                    {
                        xtype: 'button',
                        ui: 'link',
                        //itemId: 'channelCustomAttributesCancelBtn',
                        text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                        handler: function () {
                            me.down('#usagePointTechnicalAttributes').show();
                            me.down('#editable-form-electricity').hide();
                            me.down('#bottom-buttons-electricity').hide();
                            Imt.customattributesonvaluesobjects.service.ActionMenuManager.setdisabledAllEditBtns(false);
                        }
                    }
                ]
            }
        ];
        me.callParent();
    },
    renderValue: function (data) {
        if (data) {
            if (data.multiplier == 0)
                return data.value + ' ' + data.unit;
            else
                return data.value + '*10<sup style="vertical-align: top; position: relative; top: -0.5em;">' + data.multiplier + '</sup> ' + data.unit;

        } else return '-';
    }
});