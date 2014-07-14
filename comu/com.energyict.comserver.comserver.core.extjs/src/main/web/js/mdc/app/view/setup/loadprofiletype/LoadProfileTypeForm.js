Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeForm',
    loadProfileTypeHeader: null,
    loadProfileTypeAction: null,
    loadProfileTypeActionHref: null,
    loadButtonAction: null,
    requires: [
        'Uni.form.field.Obis'
    ],
    content: [
        {
            items: [
                {
                    xtype: 'container',
                    itemId: 'LoadProfileTypeHeader'
                },
                {
                    xtype: 'form',
                    width: '100%',
                    itemId: 'LoadProfileTypeFormId',
                    defaults: {
                        labelWidth: 250,
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
                            emptyText: '0 minutes',
                            name: 'timeDuration',
                            displayField: 'name',
                            valueField: 'id',
                            queryMode: 'local',
                            forceSelection: true,
                            required: true,
                            editable: false
                        },
                        {
                            xtype: 'obis-field',
                            fieldLabel: 'OBIS code',
                            name: 'obisCode'
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: 'Measurement types',
                            required: true,
                            hidehead: true,
                            anchor: '100%',
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
                                    height: 220,
                                    rbar: [
                                        {
                                            xtype: 'container',
                                            itemId: 'LoadProfileTypeAddMeasurementTypeAction'
                                        }
                                    ]
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
                action: this.loadButtonAction,
                ui: 'action'
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                href: '#/administration/loadprofiletypes',
                ui: 'link'
            }
        );
    }
});

