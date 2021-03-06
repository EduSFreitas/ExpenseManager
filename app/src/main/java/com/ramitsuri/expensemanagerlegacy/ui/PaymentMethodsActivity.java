package com.ramitsuri.expensemanagerlegacy.ui;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.ramitsuri.expensemanagerlegacy.R;
import com.ramitsuri.expensemanagerlegacy.adapter.ListActivityAdapter;
import com.ramitsuri.expensemanagerlegacy.constants.Others;
import com.ramitsuri.expensemanagerlegacy.dialog.EditTextDialogFragment;
import com.ramitsuri.expensemanagerlegacy.entities.PaymentMethod;
import com.ramitsuri.expensemanagerlegacy.helper.PaymentMethodHelper;

import java.util.List;

public class PaymentMethodsActivity extends AppCompatActivity implements View.OnClickListener,
        ListActivityAdapter.ListActivityAdapterCallbacks,
        EditTextDialogFragment.EditTextDialogCallbacks {

    private ListActivityAdapter<PaymentMethod> mAdapter;
    private List<PaymentMethod> mPaymentMethods;
    private Toolbar mToolbar;
    private FloatingActionButton mFabAddValue;
    private RecyclerView mRecyclerViewValues;
    private PaymentMethod mPaymentMethodToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        setupActionBar();
        setupViews();
        setTitle(getString(R.string.nav_menu_payment_methods));
    }

    private void setupViews() {
        mFabAddValue = (FloatingActionButton)findViewById(R.id.fab_add_value);
        mFabAddValue.setOnClickListener(this);
        mRecyclerViewValues = (RecyclerView)findViewById(R.id.recycler_view_values);
        RecyclerView.LayoutManager recyclerViewLManager = new LinearLayoutManager(this);
        mPaymentMethods = PaymentMethodHelper.getAllPaymentMethods();
        mAdapter = new ListActivityAdapter(this, mPaymentMethods);
        mRecyclerViewValues.setHasFixedSize(false);
        mRecyclerViewValues.setLayoutManager(recyclerViewLManager);
        mRecyclerViewValues.setAdapter(mAdapter);
        /*ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){
                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        final int position = viewHolder.getAdapterPosition();
                        final PaymentMethod removedValue = mPaymentMethods.get(position);
                        mPaymentMethods.remove(position);
                        mAdapter.notifyItemRemoved(position);
                        View.OnClickListener undoClickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mPaymentMethods.add(position, removedValue);
                                mAdapter.notifyItemInserted(position);
                                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
                            }
                        };
                        Snackbar.make(viewHolder.itemView, getString(R.string.payment_deleted),
                                Snackbar.LENGTH_LONG).setAction(getString(R.string.undo),
                                undoClickListener).show();
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerViewValues);*/
    }

    private void setupActionBar() {
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        if (view == mFabAddValue) {
            handleFabAddClicked();
        }
    }

    private void handleFabAddClicked() {
        mPaymentMethodToEdit = new PaymentMethod();
        mPaymentMethodToEdit.setName("");
        mPaymentMethods.add(mPaymentMethodToEdit);
        Bundle args = new Bundle();
        args.putString(Others.PAYMENT_METHOD_PICKER_METHOD, mPaymentMethodToEdit.toString());
        EditTextDialogFragment newFragment = EditTextDialogFragment.newInstance();
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), EditTextDialogFragment.TAG);
    }

    @Override
    public void onItemClicked(Object item) {
        mPaymentMethodToEdit = (PaymentMethod)item;
        Bundle args = new Bundle();
        args.putString(Others.PAYMENT_METHOD_PICKER_METHOD, mPaymentMethodToEdit.toString());
        EditTextDialogFragment newFragment = EditTextDialogFragment.newInstance();
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), EditTextDialogFragment.TAG);
    }

    @Override
    public void onItemEdited(String value) {
        mPaymentMethodToEdit.setName(value);
        if (mPaymentMethodToEdit.getId() == 0) {
            PaymentMethodHelper.addPaymentMethod(mPaymentMethodToEdit.getName());
            mPaymentMethods = PaymentMethodHelper.getAllPaymentMethods();
        } else {
            PaymentMethodHelper.updatePaymentMethodName(mPaymentMethodToEdit.getId(),
                    mPaymentMethodToEdit.getName());
        }
        mAdapter.notifyDataSetChanged();
    }
}
