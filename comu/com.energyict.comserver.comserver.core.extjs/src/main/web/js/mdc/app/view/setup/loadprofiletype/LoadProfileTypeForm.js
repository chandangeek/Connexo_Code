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
                        labelWidth: 250,
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
                            regex: /[a-zA-Z0-9]+/,
                            allowBlank: false,
                            required: true,
                            fieldLabel: 'Name',
                            msgTarget: 'under'
                        },
                        {
                            xtype: 'combobox',
                            allowBlank: false,
                            fieldLabel: 'Interval',
                            emptyText: '1 minute',
                            name: 'timeDuration',
                            displayField: 'name',
                            valueField: 'id',
                            queryMode: 'local',
                            forceSelection: true,
                            required: true,
                            editable: false
                        },
                        {
                            xtype: 'textfield',
                            allowBlank: false,
                            required: true,
                            fieldLabel: 'OBIS code',
                            emptyText: 'x.x.x.x.x.x',
                            name: 'obisCode',
                            maskRe: /[\d.]+/,
                            vtype: 'obisCode',
                            afterSubTpl: 'Provide the value for the 6 attributes of the OBIS code. Separate each value with a "."',
                            msgTarget: 'under'
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: 'Measurement types',
                            required: true,
                            hidehead: true,
                            items: [
                                {
                                    xtype: 'gridpanel',
                                    hideHeaders: true,
                                    store: 'SelectedMeasurementTypesForLoadProfileType',
                                    itemId: 'MeasurementTypesGrid',
                                    columns: [
                                        {
                                            text: 'Name',
                                            dataIndex: 'name',
                                            flex: 1
                                        },
                                        {
                                            xtype: 'actioncolumn',
                                            iconCls: 'icon-delete',
                                            align: 'right'
                                        }
                                    ],
                                    height: 220
                                },
                                {
                                    xtype: 'container',
                                    itemId: 'LoadProfileTypeAddMeasurementTypeAction'
                                },
                                {
                                    name: 'measurementTypesErrors',
                                    layout: 'hbox',
                                    hidden: true,
                                    defaults: {
                                        xtype: 'container'
                                    }
                                }
                            ]
                        }
                    ],
                    buttons: [
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
    ],

    initComponent: function () {
        this.callParent(this);
        this.down('#LoadProfileTypeHeader').add(
            {
                xtype: 'container',
                html: '<h1>' + this.loadProfileTypeHeader + '</h1>'
            }
        );
        this.down('#LoadProfileTypeAddMeasurementTypeAction').add(
            {
                xtype: 'button',
                text: 'Add measurement types',
                margin: 10,
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

