Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationForm',
    loadProfileConfigurationAction: null,
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Uni.form.field.Obis',
        'Uni.form.field.ObisDisplay'
    ],
    content: [
        {
            xtype: 'form',
            ui: 'large',
            width: '100%',
            itemId: 'LoadProfileConfigurationFormId',
            defaults: {
                labelWidth: 150,
                validateOnChange: false,
                validateOnBlur: false,
                anchor: '50%'
            },
            items: [
                {
                    xtype: 'uni-form-error-message',
                    name: 'errors',
                    hidden: true,
                    margin: '0 0 32 0'
                },
                {
                    xtype: 'displayfield',
                    required: true,
                    fieldLabel: 'Load profile type',
                    name: 'loadprofiletype',
                    value: 'LoadProfileType',
                    hidden: true
                },
                {
                    xtype: 'combobox',
                    required: true,
                    allowBlank: false,
                    fieldLabel: 'Load profile type',
                    emptyText: Uni.I18n.translate('loadprofileconfiguration.selectLoadProfileType','MDC','Select a load profile type'),
                    name: 'id',
                    displayField: 'name',
                    valueField: 'id',
                    queryMode: 'local'
                },
                {
                    xtype: 'obis-displayfield',
                    name: 'obisCode',
                    value: 'Select a load profile type first'
                },
                {
                    xtype: 'obis-field',
                    required: false,
                    fieldLabel: Uni.I18n.translate('channelConfig.channelConfigForm.overruledObisCodeLabel', 'MDC', 'Overruled OBIS code'),
                    name: 'overruledObisCode'
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    layout: {
                        type: 'hbox'
                    },
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
    ],

    initComponent: function () {
        this.callParent(this);
        Ext.suspendLayouts();
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
                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles',
                ui: 'link'
            }
        );
        Ext.resumeLayouts();
    }
});

