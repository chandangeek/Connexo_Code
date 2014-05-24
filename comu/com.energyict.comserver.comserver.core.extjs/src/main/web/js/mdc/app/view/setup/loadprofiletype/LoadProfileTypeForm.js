Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeForm',
    loadProfileTypeHeader: null,
    loadProfileTypeAction: null,
    loadProfileTypeActionHref: null,
    content: [
        {
            items: [
                {
                    xtype: 'container',
                    itemId: 'LoadProfileTypeHeader'
                },
                {
                    xtype: 'form',
                    width: '50%',
                    itemId: 'LoadProfileTypeFormId',
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
                            xtype: 'textfield',
                            name: 'name',
                            labelSeparator: ' *',
                            regex: /[a-zA-Z0-9]+/,
                            allowBlank: false,
                            fieldLabel: 'Name',
                            msgTarget: 'under'
                        },
                        {
                            xtype: 'combobox',
                            labelSeparator: ' *',
                            allowBlank: false,
                            fieldLabel: 'Interval',
                            emptyText: '0 minutes',
                            name: 'timeDuration',
                            displayField: 'name',
                            valueField: 'id',
                            queryMode: 'local'
                        },
                        {
                            xtype: 'textfield',
                            labelSeparator: ' *',
                            allowBlank: false,
                            fieldLabel: 'OBIS code',
                            name: 'obisCode',
                            maskRe: /[\d.]+/,
                            vtype: 'obisCode',
                            msgTarget: 'under'
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: 'Measurment types',
                            labelSeparator: ' *',
                            hidehead: true,
                            width: 1000,

                            items: [
                                {
                                    xtype: 'gridpanel',
                                    store: 'SelectedMeasurementTypesForLoadProfileType',
                                    columns: [
                                        { text: 'Name', dataIndex: 'name', flex: 1 }
                                    ],
                                    height: 200,
                                    margin: 10
                                },
                                {
                                    xtype: 'container',
                                    itemId: 'LoadProfileTypeAddMeasurementTypeAction'
                                },
                                {
                                    name: 'measurementTypesErrors',
                                    layout: 'hbox',
                                    margin: 10,
                                    hidden: true,
                                    defaults: {
                                        xtype: 'container'
                                    }
                                }
                            ]
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
                                    itemId: 'LoadProfileTypeAction'
                                },
                                {
                                    xtype: 'button',
                                    text: 'Cancel',
                                    href: '#/administration/loadprofiletypes',
                                    ui: 'link'
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
        this.down('#LoadProfileTypeHeader').add(
            {
                xtype: 'container',
                html: '<h2>' + this.loadProfileTypeHeader + '</h2>'
            }
        );
        this.down('#LoadProfileTypeAddMeasurementTypeAction').add(
            {
                xtype: 'button',
                text: 'Add measurement types',
                href: '#/administration/loadprofiletypes/' + this.loadProfileTypeActionHref + '/addmeasurementtypes',
                ui: 'action'
            }
        );

        this.down('#LoadProfileTypeAction').add(
            {
                xtype: 'button',
                name: 'loadprofiletypeaction',
                text: this.loadProfileTypeAction,
                ui: 'action'
            }
        );
        Ext.apply(Ext.form.VTypes, {
            obisCode: function (val, field) {
                var obis = /^(0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]))$/;
                return obis.test(val);
            },
            obisCodeText: 'OBIS code is wrong'
        });
    }
});

