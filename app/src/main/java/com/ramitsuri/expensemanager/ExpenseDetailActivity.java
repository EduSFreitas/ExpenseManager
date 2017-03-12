package com.ramitsuri.expensemanager;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ramitsuri.expensemanager.helper.ExpenseHelper;
import com.ramitsuri.expensemanager.dialog.CategoryPickerDialogFragment;
import com.ramitsuri.expensemanager.dialog.CurrencyPickerDialogFragment;
import com.ramitsuri.expensemanager.dialog.DatePickerDialogFragment;
import com.ramitsuri.expensemanager.dialog.PaymentPickerDialogFragment;
import com.ramitsuri.expensemanager.entities.Category;
import com.ramitsuri.expensemanager.entities.Expense;

public class ExpenseDetailActivity extends AppCompatActivity implements View.OnClickListener,
        PaymentPickerDialogFragment.PaymentMethodPickerCallbacks,
        DatePickerDialogFragment.DatePickerCallbacks,
        CategoryPickerDialogFragment.CategoryPickerCallbacks,
        CurrencyPickerDialogFragment.CurrencyPickerCallbacks{

    EditText mFieldAmount, mFieldDescription, mFieldStore;
    Button mCurrencyPicker, mDatePicker, mCategoryPicker, mPaymentMethodPicker;
    private FloatingActionButton mFabDone;
    private Toolbar mToolbar;
    private String mPaymentMethod;
    private Category mCategory;
    private long mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        setupActionBar();
        setupView();
        setupListeners();
    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupView() {
        mDatePicker = (Button) findViewById(R.id.date_picker);
        mCategoryPicker = (Button)findViewById(R.id.category_picker);
        mPaymentMethodPicker = (Button)findViewById(R.id.payment_method_picker);
        mCurrencyPicker = (Button)findViewById(R.id.currency_picker);
        mFieldAmount = (EditText)findViewById(R.id.edit_text_amount);
        mFieldDescription = (EditText)findViewById(R.id.edit_text_description);
        mFieldStore = (EditText)findViewById(R.id.edit_text_store);
        mFabDone = (FloatingActionButton)findViewById(R.id.fab_done);
    }

    private void setupListeners() {
        mDatePicker.setOnClickListener(this);
        mCategoryPicker.setOnClickListener(this);
        mPaymentMethodPicker.setOnClickListener(this);
        mCurrencyPicker.setOnClickListener(this);
        mFabDone.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == mDatePicker){
            DialogFragment newFragment = new DatePickerDialogFragment();
            newFragment.show(getSupportFragmentManager(), "datePicker");
        } else if(view == mCategoryPicker){
            DialogFragment newFragment = new CategoryPickerDialogFragment();
            newFragment.show(getSupportFragmentManager(), "categoryPicker");
        } else if(view == mPaymentMethodPicker){
            DialogFragment newFragment = new PaymentPickerDialogFragment();
            newFragment.show(getSupportFragmentManager(), "paymentMethodPicker");
        } else if(view == mCurrencyPicker){
            DialogFragment newFragment = new CurrencyPickerDialogFragment();
            newFragment.show(getSupportFragmentManager(), "currencyPicker");
        } else if(view == mFabDone){
            createExpense();
            finish();
        }
    }

    private void createExpense() {
        Expense expense = new Expense();
        expense.setRowIdentifier("1");
        expense.setDateTime(mDate);
        expense.setStore(mFieldStore.getEditableText().toString());
        expense.setDescription(mFieldDescription.getEditableText().toString());
        expense.setPaymentMethod(mPaymentMethod);
        expense.setAmount(mFieldAmount.getEditableText().toString());
        expense.setCategory(mCategory);
        ExpenseHelper.addExpense(expense);
    }

    @Override
    public void onPaymentMethodPicked(String paymentMethod) {
        mPaymentMethod = paymentMethod;
    }

    @Override
    public void onDatePicked(long date) {
        mDate = date;
    }

    @Override
    public void onCurrencyPicked(String currency) {

    }

    @Override
    public void onCategoryPicked(String category) {
        mCategory = new Category();
        mCategory.setName(category);
        mCategory.setId(1);
    }
}
