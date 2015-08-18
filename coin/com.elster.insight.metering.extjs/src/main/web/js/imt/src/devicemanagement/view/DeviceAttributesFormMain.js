Ext.define('Imt.devicemanagement.view.DeviceAttributesFormMain', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceAttributesFormMain',
    itemId: 'deviceAttributesFormMain',
//    title: Uni.I18n.translate('deviceManagement.deviceAttributes', 'IMT', 'Device Attributes'),
//    router: null,
//    ui: 'tile',
    
//    requires: [
//        'Uni.form.field.Duration'
//    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'fieldcontainer',
//                fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes', 'IMT', 'General attributes'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 150
                },
                items: [
                    {
                        name: 'mRID',
                        itemId: 'fld-up-mRID',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.mrid', 'IMT', 'mRID')
                    },
                    {
                        name: 'name',
                        itemId: 'fld-up-name',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.name', 'IMT', 'Name'),
//                        renderer: function (value) {
//                            return value ? value : '-';
//                        }
                    },
                    {
                        name: 'description',
                        itemId: 'fld-up-description',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.description', 'IMT', 'Description')
                    },
                    {
                        name: 'aliasName',
                        itemId: 'fld-up-aliasName',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.aliasName', 'IMT', 'AliasName')
                    },
                    {
                        name: 'serialNumber',
                        itemId: 'fld-up-serialNumber',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.serialNumber', 'IMT', 'Serial Number')
                    },
                    {
                        name: 'utcNumber',
                        itemId: 'fld-up-utcNumber',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.utcNumber', 'IMT', 'UTC Number')
                    },
                    {
                        name: 'version',
                        itemId: 'fld-up-version',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.version', 'IMT', 'Version')
                    },
                    {
                        name: 'installedDate',
                        itemId: 'fld-up-installedDate',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.installedDate', 'IMT', 'Installed Date'),
                        renderer: function (value) {
                       		if (value) {
                       			this.show();
                   				return  Uni.DateTime.formatDateTimeShort(new Date(value*1000));
                      		} else {
 //                      			this.hide(); 
                       			return '-';
                       		}
                        },
                    },
                    {
                        name: 'removedDate',
                        itemId: 'fld-up-removedDate',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.removedDate', 'IMT', 'Removed Date'),
                        renderer: function (value) {
                       		if (value) {
                       			this.show();
                   				return  Uni.DateTime.formatDateTimeShort(new Date(value*1000));
                      		} else {
                      			this.hide(); //return '-';
                       		}
                        },
                    },
                    {
                        name: 'retiredDate',
                        itemId: 'fld-up-retiredDate',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.retiredDate', 'IMT', 'Retired Date'),
                         renderer: function (value) {
                       		if (value) {
                       			this.show();
                   				return  Uni.DateTime.formatDateTimeShort(new Date(value*1000));
                      		} else {
                      			this.hide(); //return '-';
                       		}
                        },
                    },

                    {
                        name: 'eMail1',
                        itemId: 'fld-up-eMail',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.eMail1', 'IMT', 'E-Mail 1')
                    },
                    {
                        name: 'amrSystemName',
                        itemId: 'fld-up-amrSystemName',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.amrSystemName', 'IMT', 'AMR System Name')
                    },
                    {
                        name: 'usagePointMRId',
                        itemId: 'fld-up-usagePointMRId',
                        fieldLabel: Uni.I18n.translate('deviceManagement.generalAttributes.usagePointName', 'IMT', 'Usage Point'),
                        renderer: function (value) {
                       		if (value) {
                       		    var url = me.router.getRoute('administration/usagepoint').buildUrl({mRID: value});
                   				return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                      		} else {
                       			return '-';
                       		}
                        }
                    }
                 ]
            }
        ];
        me.callParent();
    }
});