Ext.require([
    'Ext.form.*',
    'Ext.Img',
    'Ext.tip.QuickTipManager'
]);

Ext.onReady(function() {
    Ext.tip.QuickTipManager.init();

    var formPanel = Ext.widget('form', {
        renderTo: Ext.getBody(),
        frame: true,
        width: 350,
        bodyPadding: 10,
        bodyBorder: true,
        title: 'Account Registration',

        defaults: {
            anchor: '100%'
        },
        fieldDefaults: {
            labelWidth: 110,
            labelAlign: 'left',
            msgTarget: 'none',
            invalidCls: '' //unset the invalidCls so individual fields do not get styled as invalid
        },

        /*
         * Listen for validity change on the entire form and update the combined error icon
         */
        listeners: {
            fieldvaliditychange: function() {
                this.updateErrorState();
            },
            fielderrorchange: function() {
                this.updateErrorState();
            }
        },

        updateErrorState: function() {
            var me = this,
                errorCmp, fields, errors;

            if (me.hasBeenDirty || me.getForm().isDirty()) { //prevents showing global error when form first loads
                errorCmp = me.down('#formErrorState');
                fields = me.getForm().getFields();
                errors = [];
                fields.each(function(field) {
                    Ext.Array.forEach(field.getErrors(), function(error) {
                        errors.push({name: field.getFieldLabel(), error: error});
                    });
                });
                errorCmp.setErrors(errors);
                me.hasBeenDirty = true;
            }
        },

        items: [{
            xtype: 'textfield',
            name: 'username',
            fieldLabel: 'User Name',
            allowBlank: false,
            minLength: 6
        }, {
            xtype: 'textfield',
            name: 'email',
            fieldLabel: 'Email Address',
            vtype: 'email',
            allowBlank: false
        }, {
            xtype: 'textfield',
            name: 'password1',
            fieldLabel: 'Password',
            inputType: 'password',
            style: 'margin-top:15px',
            allowBlank: false,
            minLength: 8
        }, {
            xtype: 'textfield',
            name: 'password2',
            fieldLabel: 'Repeat Password',
            inputType: 'password',
            allowBlank: false,
            /**
             * Custom validator implementation - checks that the value matches what was entered into
             * the password1 field.
             */
            validator: function(value) {
                var password1 = this.previousSibling('[name=password1]');
                return (value === password1.getValue()) ? true : 'Passwords do not match.'
            }
        },

        /*
         * Terms of Use acceptance checkbox. Two things are special about this:
         * 1) The boxLabel contains a HTML link to the Terms of Use page; a special click listener opens this
         *    page in a modal Ext window for convenient viewing, and the Decline and Accept buttons in the window
         *    update the checkbox's state automatically.
         * 2) This checkbox is required, i.e. the form will not be able to be submitted unless the user has
         *    checked the box. Ext does not have this type of validation built in for checkboxes, so we add a
         *    custom getErrors method implementation.
         */
        {
            xtype: 'checkboxfield',
            name: 'acceptTerms',
            fieldLabel: 'Terms of Use',
            hideLabel: true,
            margin: '15 0 0 0',
            boxLabel: 'I have read and accept the <a href="#" class="terms">Terms of Use</a>.',

            // Listener to open the Terms of Use page link in a modal window
            listeners: {
                click: {
                    element: 'boxLabelEl',
                    fn: function(e) {
                        var target = e.getTarget('.terms'),
                            win;
                        
                        e.preventDefault();
                        
                        if (target) {
                            win = Ext.getCmp('termsWindow') || Ext.widget('window', {
                                id: 'termsWindow',
                                closeAction: 'hide',
                                title: 'Terms of Use',
                                modal: true,
                                contentEl: Ext.getDom('legalese'),
                                width: 700,
                                height: 400,
                                bodyPadding: '10 20',
                                autoScroll: true,
                                
                                buttons: [{
                                    text: 'Decline',
                                    handler: function() {
                                        this.up('window').close();
                                        formPanel.down('[name=acceptTerms]').setValue(false);
                                    }
                                }, {
                                    text: 'Accept',
                                    handler: function() {
                                        this.up('window').close();
                                        formPanel.down('[name=acceptTerms]').setValue(true);
                                    }
                                }]
                            });
                            win.show();
                        }
                    }
                }
            },

            // Custom validation logic - requires the checkbox to be checked
            getErrors: function() {
                return this.getValue() ? [] : ['You must accept the Terms of Use']
            }
        }],

        dockedItems: [{
            cls: Ext.baseCSSPrefix + 'dd-drop-ok',
            xtype: 'container',
            dock: 'bottom',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            padding: '10 10 5',

            items: [{
                xtype: 'component',
                id: 'formErrorState',
                invalidCls: Ext.baseCSSPrefix + 'form-invalid-icon',
                validCls: Ext.baseCSSPrefix + 'dd-drop-icon',
                baseCls: 'form-error-state',
                flex: 1,
                validText: 'Form is valid',
                invalidText: 'Form has errors',
                tipTpl: Ext.create('Ext.XTemplate', '<ul class="' + Ext.plainListCls + '"><tpl for="."><li><span class="field-name">{name}</span>: <span class="error">{error}</span></li></tpl></ul>'),

                getTip: function() {
                    var tip = this.tip;
                    if (!tip) {
                        tip = this.tip = Ext.widget('tooltip', {
                            target: this.el,
                            title: 'Error Details:',
                            minWidth: 200,
                            autoHide: false,
                            anchor: 'top',
                            mouseOffset: [-11, -2],
                            closable: true,
                            constrainPosition: false,
                            cls: 'errors-tip'
                        });
                        tip.show();
                    }
                    return tip;
                },

                setErrors: function(errors) {
                    var me = this,
                        tip = me.getTip();

                    errors = Ext.Array.from(errors);

                    // Update CSS class and tooltip content
                    if (errors.length) {
                        me.addCls(me.invalidCls);
                        me.removeCls(me.validCls);
                        me.update(me.invalidText);
                        tip.setDisabled(false);
                        tip.update(me.tipTpl.apply(errors));
                    } else {
                        me.addCls(me.validCls);
                        me.removeCls(me.invalidCls);
                        me.update(me.validText);
                        tip.setDisabled(true);
                        tip.hide();
                    }
                }
            }, {
                xtype: 'button',
                formBind: true,
                disabled: true,
                text: 'Submit Registration',
                handler: function() {
                    var form = this.up('form').getForm();

                    /* Normally we would submit the form to the server here and handle the response...
                    form.submit({
                        clientValidation: true,
                        url: 'register.php',
                        success: function(form, action) {
                           //...
                        },
                        failure: function(form, action) {
                            //...
                        }
                    });
                    */

                    if (form.isValid()) {
                        var out = [];
                        Ext.Object.each(form.getValues(), function(key, value){
                            out.push(key + '=' + value);
                        });
                        Ext.Msg.alert('Submitted Values', out.join('<br />'));
                    }
                }
            }]
        }]
    });

});
