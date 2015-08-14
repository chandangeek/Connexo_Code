Ext.define('Mdc.view.setup.comportpool.ComPortPoolEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comPortPoolEdit',
    itemId: 'comPortPoolEdit',

    edit: false,

    content: [
        {
            xtype: 'form',
            itemId: 'comPortPoolEditForm',
            ui: 'large',
            width: '100%',
            defaults: {
                labelWidth: 150,
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'hiddenfield',
                    name: 'id'
                },
                {
                    xtype: 'hiddenfield',
                    name: 'direction'
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    itemId: 'txt-comportpool-name',
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    width: 600,
                    required: true,
                    allowBlank: false
                },
                {
                    xtype: 'displayfield',
                    name: 'direction_visual',
                    itemId: 'txt-comportpool-direction',
                    fieldLabel: Uni.I18n.translate('comports.preview.direction', 'MDC', 'Direction'),
                    hidden: true,
                    width: 600
                },
                {
                    xtype: 'combobox',
                    name: 'comPortType',
                    itemId: 'cbo-comportpool-type',
                    fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                    emptyText: Uni.I18n.translate('comPortPool.formFieldEmptyText.selectCommunicationType', 'MDC', 'Select communication type...'),
                    store: 'Mdc.store.ComPortTypes',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'comPortType',
                    valueField: 'comPortType',
                    required: true,
                    width: 600
                },
                {
                    xtype: 'combobox',
                    name: 'discoveryProtocolPluggableClassId',
                    itemId: 'cbo-comportpool-protocol-detect',
                    fieldLabel: Uni.I18n.translate('comPortPool.formFieldLabel.protocolDetection', 'MDC', 'Protocol detection'),
                    emptyText: Uni.I18n.translate('comPortPool.formFieldEmptyText.selectProtocolDetection', 'MDC', 'Select protocol detection...'),
                    store: 'Mdc.store.DeviceDiscoveryProtocols',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'name',
                    valueField: 'id',
                    required: true,
                    width: 600
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'saveModel',
                            itemId: 'createEditButton'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            href: '#/administration/comportpools/'
                        }
                    ]
                }
            ]
        }
    ],

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
        }
        this.down('#cancelLink').href = returnLink;
    }
});