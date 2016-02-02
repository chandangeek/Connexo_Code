Ext.define('Imt.usagepointmanagement.view.landingpage.GeneralAttributesForm', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.general-attributes-form',


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

    //panelTitle: null,
    //record: null,

    items : [
            {
                xtype: 'form',
                itemId: 'view-form',
                //hidden: true,
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250,
                    //width: 600
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

                    {
                        name: 'serviceCategory',
                        itemId: 'fld-up-serviceCategory',
                        fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category')
                    },

                    {
                        name: 'connectionState',
                        itemId: 'fld-up-connectionState',
                        fieldLabel: Uni.I18n.translate('general.label.connectionState', 'IMT', 'Connection state'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },

                ]
            },
            {
                xtype: 'form',
                itemId: 'edit-form',
                hidden: true,
                defaults: {
                    //xtype: 'displayfield',
                    labelWidth: 250,
                    //width: 600
                },
                items: [
                    {
                        xtype: 'textfield',
                        disabled: true,
                        name: 'mRID',
                        //itemId: 'fld-up-mRID',
                        fieldLabel: Uni.I18n.translate('general.label.mRID', 'IMT', 'MRID'),
                        //renderer: function (value) {
                        //    return value ? value : '-';
                        //}
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'fld-up-name',
                        fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                        //renderer: function (value) {
                        //    return value ? value : '-';
                        //}
                    },
                    {
                        xtype: 'combobox',
                        disabled: true,
                        name: 'serviceCategory',
                        itemId: 'fld-up-serviceCategory',
                        fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category')
                    },

                    {
                        xtype: 'combobox',
                        disabled: true,
                        name: 'connectionState',
                        itemId: 'fld-up-connectionState',
                        fieldLabel: Uni.I18n.translate('general.label.connectionState', 'IMT', 'Connection state'),
                        //renderer: function (value) {
                        //    return value ? value : '-';
                        //}
                    },
                ]
            },
        ]
});