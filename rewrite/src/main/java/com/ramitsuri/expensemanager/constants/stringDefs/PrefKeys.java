package com.ramitsuri.expensemanager.constants.stringDefs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.StringDef;

@StringDef(value = {
        PrefKeys.ACCOUNT_NAME,
        PrefKeys.ACCOUNT_TYPE,
        PrefKeys.SPREADSHEET_ID,
        PrefKeys.SHEET_ID,
        PrefKeys.THEME,
        PrefKeys.DEFAULT_SHEET_ID,
        PrefKeys.ENABLE_SPLITTING,
        PrefKeys.ENABLE_EXPENSE_SYNC,
        PrefKeys.ENABLE_ENTITIES_SYNC,
        PrefKeys.IS_ENTITIES_EDITED,
        PrefKeys.TIME_ZONE_ID
})
@Retention(RetentionPolicy.SOURCE)
public @interface PrefKeys {
    String ACCOUNT_NAME = "settings_account_name";
    String ACCOUNT_TYPE = "settings_account_type";
    String SPREADSHEET_ID = "settings_spreadsheet_id";
    String SHEET_ID = "settings_sheet_id";
    String THEME = "settings_theme";
    String DEFAULT_SHEET_ID = "default_sheet_id";
    String ENABLE_SPLITTING = "enable_splitting";
    String ENABLE_EXPENSE_SYNC = "enable_expense_sync";
    String ENABLE_ENTITIES_SYNC = "enable_entities_sync";
    String IS_ENTITIES_EDITED = "is_entities_edited";
    String TIME_ZONE_ID = "settings_time_zone_id";
    String AUTO_BACKUP = "settings_auto_backup";
    String VERSION_INFO = "version_info";
    String ENABLE_DEBUG_OPTIONS = "enable_debug_options";
    String MIGRATION_STEP = "migration_step";
}