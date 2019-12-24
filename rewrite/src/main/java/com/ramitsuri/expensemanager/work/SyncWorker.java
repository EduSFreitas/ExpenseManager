package com.ramitsuri.expensemanager.work;

import android.content.Context;

import com.ramitsuri.expensemanager.Constants;
import com.ramitsuri.expensemanager.MainApplication;
import com.ramitsuri.expensemanager.data.ExpenseManagerDatabase;
import com.ramitsuri.expensemanager.entities.SheetInfo;
import com.ramitsuri.sheetscore.consumerResponse.EntitiesConsumerResponse;
import com.ramitsuri.sheetscore.consumerResponse.SheetMetadata;
import com.ramitsuri.sheetscore.consumerResponse.SheetsMetadataConsumerResponse;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
import timber.log.Timber;

public class SyncWorker extends BaseWorker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String workType = getInputData().getString(Constants.Work.TYPE);
        String message;

        if (MainApplication.getInstance().getSheetRepository() == null) {
            message = "Sheet repo null";
            Timber.e(message);
            insertLog(workType,
                    Constants.LogResult.FAILURE,
                    message);
            return Result.failure();
        }
        if (MainApplication.getInstance().getCategoryRepo() == null) {
            message = "Category repo null";
            Timber.e(message);
            insertLog(workType,
                    Constants.LogResult.FAILURE,
                    message);
            return Result.failure();
        }
        if (MainApplication.getInstance().getPaymentMethodRepo() == null) {
            message = "Payment method repo null";
            Timber.e(message);
            insertLog(workType,
                    Constants.LogResult.FAILURE,
                    message);
            return Result.failure();
        }

        // Payment methods and Categories
        EntitiesConsumerResponse entities = MainApplication.getInstance().getSheetRepository()
                .getEntityDataResponse(Constants.Range.CATEGORIES_PAYMENT_METHODS);
        if (entities.getStringLists().size() != 2) {
            message = "Attempting to save entities, list size should be 2";
            Timber.i(message);
            insertLog(workType,
                    Constants.LogResult.FAILURE,
                    message);
        } else {
            message = "Saving payment methods and categories";
            Timber.i(message);
            MainApplication.getInstance().getPaymentMethodRepo()
                    .setPaymentMethods(entities.getStringLists().get(0));
            MainApplication.getInstance().getCategoryRepo()
                    .setCategories(entities.getStringLists().get(1));
            insertLog(workType,
                    Constants.LogResult.SUCCESS,
                    message);
        }

        // Sheet meta data / info
        SheetsMetadataConsumerResponse response =
                MainApplication.getInstance().getSheetRepository().getSheetsMetadataResponse();
        List<SheetInfo> sheetInfos = new ArrayList<>();
        if (response.getSheetMetadataList() != null) {
            message = "Saving sheet infos";
            Timber.i(message);
            for (SheetMetadata sheetMetadata : response.getSheetMetadataList()) {
                sheetInfos.add(new SheetInfo(sheetMetadata));
            }
            ExpenseManagerDatabase.getInstance().sheetDao().setAll(sheetInfos);
            insertLog(workType,
                    Constants.LogResult.SUCCESS,
                    message);
        } else {
            message = "Sheet meta data returned is null " + response.getException().getMessage();
            Timber.i(message);
            insertLog(workType,
                    Constants.LogResult.FAILURE,
                    message);
        }

        return Result.success();
    }
}