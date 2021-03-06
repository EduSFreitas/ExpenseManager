package com.ramitsuri.expensemanager.work;

import android.content.Context;
import android.text.TextUtils;

import com.ramitsuri.expensemanager.MainApplication;
import com.ramitsuri.expensemanager.constants.Constants;
import com.ramitsuri.expensemanager.data.ExpenseManagerDatabase;
import com.ramitsuri.expensemanager.entities.Expense;
import com.ramitsuri.expensemanager.entities.SheetInfo;
import com.ramitsuri.expensemanager.utils.AppHelper;
import com.ramitsuri.expensemanager.utils.ObjectHelper;
import com.ramitsuri.expensemanager.utils.TransformationHelper;
import com.ramitsuri.sheetscore.consumerResponse.InsertConsumerResponse;

import java.util.List;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

public class ExpensesBackupWorker extends BaseWorker {

    public ExpensesBackupWorker(@NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String workType = getInputData().getString(Constants.Work.TYPE);
        try {
            if (MainApplication.getInstance().getSheetRepository() == null) {
                onFailure(workType, "Sheet repo null");
                return Result.failure();
            }

            // Spreadsheet Id
            String spreadsheetId = AppHelper.getSpreadsheetId();
            if (TextUtils.isEmpty(spreadsheetId)) {
                onFailure(workType, "Spreadsheet id is empty or null");
                return Result.failure();
            }

            List<Integer> editedMonths =
                    ExpenseManagerDatabase.getInstance().editedSheetDao().getAll();

            // Expenses
            List<Expense> expensesToBackup;
            if (editedMonths == null || editedMonths.size() == 0) {
                // All expenses will be appended
                expensesToBackup =
                        ExpenseManagerDatabase.getInstance().expenseDao().getAllUnsynced();
            } else {
                // Expenses might be appended (new) and sheets might be rewritten (update request)
                expensesToBackup = ExpenseManagerDatabase.getInstance().expenseDao()
                        .getAllForBackup(editedMonths);
            }

            if (expensesToBackup == null) {
                onFailure(workType, "Expenses to backup is null");
                return Result.failure();
            }

            // Do not continue if no synced expenses were edited (resulting in a possibility of a
            // sheet now having 0 expenses, in which case expense size can be zero)
            if (expensesToBackup.size() == 0 &&
                    (editedMonths == null || editedMonths.size() == 0)) {
                onFailure(workType, "No new or edited expenses");
                return Result.failure();
            }

            List<SheetInfo> sheetInfos = TransformationHelper
                    .filterSheetInfos(ExpenseManagerDatabase.getInstance().sheetDao().getAll());
            if (!isSheetInfosValid(sheetInfos)) {
                onFailure(workType, "No info about sheet ids to attach to expenses");
                return Result.failure();
            }

            InsertConsumerResponse response = MainApplication.getInstance().getSheetRepository()
                    .getInsertRangeResponse(spreadsheetId, expensesToBackup, editedMonths,
                            sheetInfos);
            if (response.isSuccessful()) {
                ExpenseManagerDatabase.getInstance().expenseDao().updateUnsynced();
                // Delete all records of edited sheets as all expenses in them are backed up now
                ExpenseManagerDatabase.getInstance().editedSheetDao().deleteAll();
                onSuccess(workType, "Backup successful");
                return Result.success();
            } else if (response.getException() != null) {
                onFailure(workType, "From operation: " + response.getException());
                return Result.failure();
            }

            onFailure(workType, "Unknown reason");
            return Result.failure();
        } catch (Exception e) {
            onFailure(workType, "Unknown: " + e);
            return Result.failure();
        }
    }

    private boolean isSheetInfosValid(@Nonnull List<SheetInfo> sheetInfos) {
        List<String> months = AppHelper.getMonths();
        return ObjectHelper.isSheetInfosValid(sheetInfos, months);
    }
}
