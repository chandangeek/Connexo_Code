/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.calendar.SpecialDaysPreview', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.specialDaysPreview',
  fieldLabel: Uni.I18n.translate('general.specialDays', 'UNI', 'Special days'),
  itemId: 'specialDays',
  labelAlign: 'top',
  defaults: {
      labelWidth: 250,
  },

  initComponent: function () {
      var me = this;

      me.items = [
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.recurring', 'UNI', 'Recurring'),
            itemId: 'recurring',
            defaults: {
                margin: '0 0 -10 0'
            }
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.fixed', 'UNI', 'Fixed'),
            itemId: 'fixed',
            defaults: {
                margin: '0 0 -10 0'
            }
        }
    ];

      me.callParent(arguments);
  },

  renderSpecialDays: function(calendarRecord) {
      var me = this;
      var recurringField = me.down('#recurring');

      me.hide();
      recurringField.removeAll();
      if (calendarRecord.recurrentSpecialDays().getCount()) {
          calendarRecord.recurrentSpecialDays().group('dayTypeName');
          var groups = calendarRecord.recurrentSpecialDays().getGroups();

          Ext.each(groups, function(group) {
              recurringField.add(
                  {
                      xtype: 'fieldcontainer',
                      fieldLabel: group.name,
                      labelAlign: 'top',
                      margin: '0 0 10 0',
                      defaults: {
                          margin: '0 0 -10 0',
                      },
                      items: group.children.map(function(record) {
                          return {
                              xtype: 'displayfield',
                              fieldLabel: undefined,
                              value: Ext.util.Format.date(
                                  record.get('date'),
                                  'j F'
                              )
                          }
                      })
                  }
              );
          });
          recurringField.show();
          me.show();
      } else {
          recurringField.hide();
      }

      var fixedField = me.down('#fixed');
      fixedField.removeAll();
      if (calendarRecord.fixedSpecialDays().getCount()) {
          calendarRecord.fixedSpecialDays().group('dayTypeName');
          var groups = calendarRecord.fixedSpecialDays().getGroups();
          Ext.each(groups, function(group) {
              fixedField.add(
                  {
                      xtype: 'fieldcontainer',
                      fieldLabel: group.name,
                      labelAlign: 'top',
                      margin: '0 0 10 0',
                      defaults: {
                          margin: '0 0 -10 0',
                      },
                      items: group.children.map(function(record) {
                          return {
                              xtype: 'displayfield',
                              fieldLabel: undefined,
                              value: Ext.util.Format.date(
                                  record.get('date'),
                                  'j F Y'
                              )
                          }
                      })
                  }
              );
          });
          fixedField.show();
          me.show();
      } else {
          fixedField.hide();
      }
  },
});