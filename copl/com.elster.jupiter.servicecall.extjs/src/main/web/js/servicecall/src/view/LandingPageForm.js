Ext.define('Scs.view.LandingPageForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.scs-landing-page-form',

    requires: [
        'Uni.property.form.Property',
        'Uni.form.field.Duration',
        'Uni.property.form.GroupedPropertyForm'
    ],
    layout: {
        type: 'column'
    },
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('servicecalls.topLevelServiceCall', 'SCS', 'Top level service call'),
                        name: 'toplevelservicecall'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('servicecalls.parentServiceCall', 'SCS', 'Parent service call'),
                        name: 'parentlevelservicecall'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('servicecalls.affectedObject', 'SCS', 'Affected object'),
                        name: 'affectedobject'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('servicecalls.serviceCallID', 'SCS', 'Service call ID'),
                        name: 'servicecallid'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('servicecalls.externalReference', 'SCS', 'Top level service call'),
                        name: 'externalreference'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.type', 'SCS', 'Type'),
                        name: 'type'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.version', 'SCS', 'Version'),
                        name: 'versionName'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.origin', 'SCS', 'Origin'),
                        name: 'origin'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.status', 'SCS', 'Status'),
                        name: 'status'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.receivedDate', 'SCS', 'Received date'),
                        name: 'receivedDate'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.modificationDate', 'SCS', 'Modification date'),
                        name: 'modificationDate'
                    }
                ]
            },
            {
                xtype: 'grouped-property-form',
                isEdit: false,
                frame: false,
                defaults: {
                    xtype: 'container',
                    resetButtonHidden: true,
                    labelWidth: 200
                }
            }
        ];
        me.callParent(arguments);
    }
});