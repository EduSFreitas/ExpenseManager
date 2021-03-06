package com.ramitsuri.expensemanagerlegacy.backup;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.ramitsuri.expensemanagerlegacy.R;
import com.ramitsuri.expensemanagerlegacy.constants.Others;
import com.ramitsuri.expensemanagerlegacy.entities.Expense;
import com.ramitsuri.expensemanagerlegacy.entities.LoaderResponse;
import com.ramitsuri.expensemanagerlegacy.helper.AppHelper;
import com.ramitsuri.expensemanagerlegacy.helper.ExpenseHelper;
import com.ramitsuri.expensemanagerlegacy.helper.SheetsHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BackupWorker extends Worker {

    public BackupWorker(@NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        LoaderResponse response = backup();
        if (response.getResponseCode() == LoaderResponse.FAILURE) {
            return Result.failure();
        } else {
            return Result.success();
        }
    }

    private LoaderResponse backup() {
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(
                        Others.SCOPES)).setBackOff(new ExponentialBackOff());
        credential.setSelectedAccountName(AppHelper.getAccountName());
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        com.google.api.services.sheets.v4.Sheets service =
                new com.google.api.services.sheets.v4.Sheets.Builder(
                        transport, jsonFactory, credential)
                        .setApplicationName(getApplicationContext().getString(R.string.app_name))
                        .build();

        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
        List<Request> requests = new ArrayList<>();
        List<Expense> expensesToBackup = ExpenseHelper.getExpensesRequiringBackup();
        Request expensesRequest = SheetsHelper.getExpenseSheetsRequest(expensesToBackup);
        requests.add(expensesRequest);
        content.setRequests(requests);
        Sheets.Spreadsheets.BatchUpdate batchUpdate;
        try {
            String spreadsheetId = AppHelper.getSpreadsheetId();
            if (!TextUtils.isEmpty(spreadsheetId)) {
                batchUpdate = service.spreadsheets().batchUpdate(spreadsheetId, content);
                batchUpdate.execute();

                ExpenseHelper.updateSyncStatusAfterBackup(expensesToBackup);
                ExpenseHelper.deleteBackedUpExpenses();
                AppHelper.setFirstBackupComplete(true);
                AppHelper.setLastBackupTime(System.currentTimeMillis());
                return new LoaderResponse(LoaderResponse.SUCCESS, null, null);
            } else {
                return new LoaderResponse(LoaderResponse.FAILURE, null, null);
            }
        } catch (UserRecoverableAuthIOException e) {
            return new LoaderResponse(LoaderResponse.FAILURE, e.getIntent(), null);
        } catch (IOException e) {
            return new LoaderResponse(LoaderResponse.FAILURE, null, null);
        }
    }
}
