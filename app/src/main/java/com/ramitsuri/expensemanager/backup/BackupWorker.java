package com.ramitsuri.expensemanager.backup;

import android.support.annotation.NonNull;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.Request;
import com.ramitsuri.expensemanager.R;
import com.ramitsuri.expensemanager.constants.Others;
import com.ramitsuri.expensemanager.entities.Expense;
import com.ramitsuri.expensemanager.entities.LoaderResponse;
import com.ramitsuri.expensemanager.helper.AppHelper;
import com.ramitsuri.expensemanager.helper.ExpenseHelper;
import com.ramitsuri.expensemanager.helper.SheetsHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import androidx.work.Worker;

public class BackupWorker extends Worker {

    private static String SHEETS_ID = "1pzDFprwHn6pbh6lRC8_emFEvQSa3MDnnU6baOAPwgcQ";

    @NonNull
    @Override
    public WorkerResult doWork() {
        LoaderResponse response = backup();
        if(response.getResponseCode() == LoaderResponse.FAILURE){
            return WorkerResult.FAILURE;
        }else {
            return WorkerResult.SUCCESS;
        }
    }

    private LoaderResponse backup() {
        GoogleAccountCredential credential= GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(
                Others.SCOPES)).setBackOff(new ExponentialBackOff());
        credential.setSelectedAccountName(AppHelper.getAccountName());
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory= JacksonFactory.getDefaultInstance();
        com.google.api.services.sheets.v4.Sheets service = new com.google.api.services.sheets.v4.Sheets.Builder(
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
            batchUpdate = service.spreadsheets().batchUpdate(SHEETS_ID, content);
            BatchUpdateSpreadsheetResponse response = batchUpdate.execute();
            ExpenseHelper.updateSyncStatusAfterBackup(expensesToBackup);
            ExpenseHelper.deleteBackedUpExpenses();
            AppHelper.setFirstBackupComplete(true);
            AppHelper.setLastBackupTime(System.currentTimeMillis());
            return new LoaderResponse(LoaderResponse.SUCCESS, null, null);
        } catch (UserRecoverableAuthIOException e) {
            return new LoaderResponse(LoaderResponse.FAILURE, e.getIntent(), null);
        } catch (IOException e) {
            return new LoaderResponse(LoaderResponse.FAILURE, null, null);
        }
    }
}