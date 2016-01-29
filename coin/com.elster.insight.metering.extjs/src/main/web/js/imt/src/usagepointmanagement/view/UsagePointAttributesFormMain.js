Ext.define('Imt.usagepointmanagement.view.UsagePointAttributesFormMain', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagePointAttributesFormMain',
    itemId: 'usagePointAttributesFormMain',

    requires: [
        'Uni.form.field.Duration',
        'Imt.util.TitleWithEditButton'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'title-with-edit-button',
                title: Uni.I18n.translate('usagepoint.general.attributes', 'IMT', 'General'),
                editHandler: function(){
                    me.down('#usage-point-general-attributes').hide();
                    me.down('#editable-form-general').show();
                    me.down('#bottom-buttons-general').show();
                }

            },
            {
                xtype: 'fieldcontainer',
                //fieldLabel: Uni.I18n.translate('usagepoint.general.attributes', 'IMT', 'General'),
                itemId: 'usage-point-general-attributes',
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250,
                    width: 600
                },
                items: [

                    {
                        name: 'mRID',
                        itemId: 'fld-up-mRID',
                        fieldLabel: Uni.I18n.translate('general.label.mRID', 'IMT', 'MRID'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'name',
                        itemId: 'fld-up-name',
                        fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    //{
                    //    name: 'aliasName',
                    //    itemId: 'fld-up-aliasName',
                    //    fieldLabel: Uni.I18n.translate('general.label.aliasName', 'IMT', 'Alias name'),
                    //    renderer: function (value) {
                    //        return value ? value : '-';
                    //    }
                    //},
                    //{
                    //    name: 'description',
                    //    itemId: 'fld-up-description',
                    //    fieldLabel: Uni.I18n.translate('general.label.description', 'IMT', 'Description'),
                    //    renderer: function (value) {
                    //        return value ? value : '-';
                    //    }
                    //},
                    {
                        name: 'serviceCategory',
                        itemId: 'fld-up-serviceCategory',
                        fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category')
                    },
                    {
                        name: 'isSdp',
                        itemId: 'fld-up-sdp',
                        fieldLabel: Uni.I18n.translate('general.label.sdp', 'IMT', 'SDP'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                        }
                    },
                    {
                        name: 'isVirtual',
                        itemId: 'fld-up-virtual',
                        fieldLabel: Uni.I18n.translate('general.label.virtual', 'IMT', 'Virtual'),
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                        }
                    },
                    //{
                    //    name: 'version',
                    //    itemId: 'fld-up-version',
                    //    fieldLabel: Uni.I18n.translate('general.label.version', 'IMT', 'Version')
                    //},
                    //{
                    //    name: 'readCycle',
                    //    itemId: 'fld-up-version',
                    //    fieldLabel: Uni.I18n.translate('general.label.readCycle', 'IMT', 'Read cycle'),
                    //    renderer: function (value) {
                    //        return value ? value : '-';
                    //    }
                    //},
                    //{
                    //    name: 'checkBilling',
                    //    itemId: 'fld-up-checkBilling',
                    //    fieldLabel: Uni.I18n.translate('general.label.checkBilling', 'IMT', 'Check billing'),
                    //    renderer: function (value) {
                    //        return value ? value : '-';
                    //    }
                    //},
                    //{
                    //    name: 'amiBillingReady',
                    //    itemId: 'fld-up-amiBillingReady',
                    //    fieldLabel: Uni.I18n.translate('general.label.amiBillingReady', 'IMT', 'AMI billing ready'),
                    //    renderer: function (value) {
                    //        return value ? value : '-';
                    //    }
                    //},
                    //{
                    //    name: 'outageRegion',
                    //    itemId: 'fld-up-outageRegion',
                    //    fieldLabel: Uni.I18n.translate('general.label.outageRegion', 'IMT', 'Outage region'),
                    //    renderer: function (value) {
                    //        return value ? value : '-';
                    //    }
                    //},
                    //{
                    //    name: 'readRoute',
                    //    itemId: 'fld-up-readRoute',
                    //    fieldLabel: Uni.I18n.translate('general.label.readRoute', 'IMT', 'Read route'),
                    //    renderer: function (value) {
                    //        return value ? value : '-';
                    //    }
                    //},
                    //{
                    //    name: 'servicePriority',
                    //    itemId: 'fld-up-servicePriority',
                    //    fieldLabel: Uni.I18n.translate('general.label.servicePriority', 'IMT', 'Service priority'),
                    //    renderer: function (value) {
                    //        return value ? value : '-';
                    //    }
                    //},
                    //{
                    //    name: 'serviceDeliveryRemark',
                    //    itemId: 'fld-up-serviceDeliveryRemark',
                    //    fieldLabel: Uni.I18n.translate('general.label.serviceDeliveryRemark', 'IMT', 'Service delivery remark'),
                    //    renderer: function (value) {
                    //        return value ? value : '-';
                    //    }
                    //},
                    {
                        name: 'connectionState',
                        itemId: 'fld-up-connectionState',
                        fieldLabel: Uni.I18n.translate('general.label.connectionState', 'IMT', 'Connection state'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    //{
                    //    name: 'created',
                    //    itemId: 'fld-up-created',
                    //    fieldLabel: Uni.I18n.translate('general.label.created', 'IMT', 'Created')
                    //},
                    //{
                    //    name: 'updated',
                    //    itemId: 'fld-up-updated',
                    //    fieldLabel: Uni.I18n.translate('general.label.lastUpdate', 'IMT', 'Last update'),
                    //    renderer: function (value) {
                    //        return value ? value : '-';
                    //    }
                    //}
                ]
            },
            {
                xtype: 'form',
                itemId: 'editable-form-general',
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
                itemId: 'bottom-buttons-general',
                dock: 'bottom',
                margin: '20 0 0 265',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'button',
                        itemId: 'channelCustomAttributesSaveBtn',
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
                            me.down('#usage-point-general-attributes').show();
                            me.down('#editable-form-general').hide();
                            me.down('#bottom-buttons-general').hide();
                        }
                    }
                ]
            }
        ];
        me.callParent();
    }
});