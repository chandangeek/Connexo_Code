Ext.define('Imt.usagepointmanagement.view.metrologyconfiguration.MeterRolesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.meter-roles-grid',
    store: null,
    ui: 'medium',
    title: Uni.I18n.translate('general.meterRoles', 'IMT', 'Meter roles'),
    maxHeight: 408,
    router: null,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                text: Uni.I18n.translate('general.meterRole', 'IMT', 'Meter role'),
                dataIndex: 'name',
                flex: 1
            },
            {
                text: Uni.I18n.translate('general.required', 'IMT', 'Required'),
                dataIndex: 'required',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            },
            {
                text: Uni.I18n.translate('general.meter', 'IMT', 'Meter'),
                dataIndex: 'meter',
                flex: 1,
                renderer: function (value) {
                    var result = '';

                    if (value) {
                        if(value.url){
                            result = '<a href="' + value.url + '">' + Ext.String.htmlEncode(value.mRID) + '</a>';
                        } else {
                            result = value.mRID;
                        }
                        //if (Mdc.privileges.Device.canViewDeviceCommunication()) {
                        //    url = me.router.getRoute('devices/device').buildUrl({mRID: value});
                        //    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                        /*} else {
                            result = Ext.String.htmlEncode(value);
                        }*/
                    } else {
                        result = '-';
                    }

                    return result;
                }
            },
            {
                text: Uni.I18n.translate('general.activationDate', 'IMT', 'Activation date'),
                dataIndex: 'activationDate',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('metrologyConfigurationDetails.meterRolesCount', 'IMT', '{0} meter role(s)', me.store.getCount()),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.editMeters', 'IMT', 'Edit meters'),
                        itemId: 'edit-meters',
                        privileges: Imt.privileges.UsagePoint.canAdministrate,
                        href: me.router.getRoute('usagepoints/view/metrologyconfiguration').buildUrl(),
                        hidden: true
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});

