package com.ramitsuri.expensemanager;

import com.google.api.services.sheets.v4.SheetsScopes;

public class Constants {

    public static final String[] SCOPES = {SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE};

    public class BundleKeys {
        public static final String PICKER_VALUES = "picker_values";
        public static final String PICKED_ITEM = "picked_item";
        public static final String DATE_PICKER_YEAR = "date_picker_year";
        public static final String DATE_PICKER_MONTH = "date_picker_month";
        public static final String DATE_PICKER_DAY = "date_picker_day";
        public static final String SELECTED_EXPENSE = "selected_expense";
    }

    public class RequestCode {
        public static final int GOOGLE_SIGN_IN = 100;
    }

    public class Work {
        public static final String APP_NAME = "app_name";
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String SPREADSHEET_ID = "spreadsheet_id";
        public static final String SHEET_ID = "sheet_id";
        public static final String TYPE = "type";
    }

    public class Tag {
        public static final String ONE_TIME_BACKUP = "one_time_backup";
        public static final String SCHEDULED_BACKUP = "scheduled_backup";
    }

    public class Range {
        public static final String CATEGORIES = "Entities!C1:C20";
        public static final String PAYMENT_METHODS = "Entities!A1:A20";
        public static final String CATEGORIES_PAYMENT_METHODS = "Entities!A1:C20";
    }

    public class SystemTheme {
        public static final String LIGHT = "light";
        public static final String DARK = "dark";
        public static final String SYSTEM_DEFAULT = "system_default";
        public static final String BATTERY_SAVER = "battery_saver";
    }

    public class AddExpenseMode {
        public static final int ADD = 0;
        public static final int EDIT = 1;
    }

    public class LogType {
        public static final String PERIODIC_BACKUP = "periodic_backup";
        public static final String ONE_TIME_BACKUP = "one_time_backup";
    }

    public class LogResult {
        public static final String SUCCESS = "success";
        public static final String FAILURE = "failure";
    }
}