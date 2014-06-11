Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileConfigurationForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationForm',
    loadProfileConfigurationAction: null,
    deviceTypeId: null,
    deviceConfigurationId: null,


    content: [
        {
            items: [
                {
                    xtype: 'container',
                    itemId: 'LoadProfileConfigurationHeader'
                },
                {
                    xtype: 'form',
                    width: '50%',
                    itemId: 'LoadProfileConfigurationFormId',
                    defaults: {
                        labelWidth: 150,
                        labelAlign: 'right',
                        margin: '0 0 20 0',
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            name: 'errors',
                            layout: 'hbox',
                            margin: '0 0 20 100',
                            hidden: true,
                            defaults: {
                                xtype: 'container'
                            }
                        },
                        {
                            xtype: 'combobox',
                            labelSeparator: ' *',
                            allowBlank: false,
                            fieldLabel: 'Load profile type',
                            emptyText: 'Select a load profile type',
                            name: 'id',
                            displayField: 'name',
                            valueField: 'id',
                            queryMode: 'local'
                        },
                        {
                            xtype: 'displayfield',
                            labelSeparator: ' ',
                            fieldLabel: 'OBIS code',
                            name: 'obisCode',
                            value: 'Select a load profile type first'
                        },
                        {
                            xtype: 'textfield',
                            labelSeparator: ' ',
                            fieldLabel: 'Overruled OBIS code',
                            name: 'overruledObisCode',
                            maskRe: /[\d.]+/,
                            vtype: 'overruledObisCode',
                            msgTarget: 'under',
                            afterSubTpl:'<div class="x-form-display-field"><i>' + 'Provide the value for the 6 attributes of the OBIS code. Separate each value with a "."' + '</i></div>'
                        }
                    ],
                    dockedItems: [
                        {
                            xtype: 'toolbar',
                            dock: 'bottom',
                            border: false,
                            margin: '0 0 0 100',
                            items: [
                                {
                                    xtype: 'container',
                                    itemId: 'LoadProfileConfigurationActionContainer'
                                },
                                {
                                    xtype: 'container',
                                    itemId: 'LoadProfileConfigurationCancelContainer'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(this);
        this.down('#LoadProfileConfigurationActionContainer').add(
            {
                xtype: 'button',
                name: 'loadprofileconfigurationaction',
                action: this.loadProfileConfigurationAction,
                text: this.loadProfileConfigurationAction,
                ui: 'action'
            }
        );
        this.down('#LoadProfileConfigurationCancelContainer').add(
            {
                xtype: 'button',
                text: 'Cancel',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles',
                ui: 'link'
            }
        );
        Ext.apply(Ext.form.VTypes, {
            overruledObisCode: function (val, field) {
                var obis = /^(0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]))$/;
                return obis.test(val);
            },
            overruledObisCodeText: 'OBIS code is wrong'
        });
    }
});

