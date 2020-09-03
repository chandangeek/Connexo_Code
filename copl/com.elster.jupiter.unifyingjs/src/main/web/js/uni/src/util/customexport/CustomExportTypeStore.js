/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.util.customexport.CustomExportTypeStore', {
  extend: 'Ext.data.Store',
  autoLoad: false,
  fields: ['id', 'name'],
  data: [
    {
      id: 'EXPORT_CUSTOM_NUMBER',
      name: Uni.I18n.translate(
        'export.exportType.customExport',
        'UNI',
        'Custom export'
      )
    },
    {
      id: 'EXPORT_ALL',
      name: Uni.I18n.translate(
        'export.exportType.exportAll',
        'UNI',
        'Export all'
      )
    }
  ]
});