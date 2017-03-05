package com.ramitsuri.expensemanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.ramitsuri.expensemanager.adapter.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewActivity extends AppCompatActivity {

    private List<String> mValues;
    private RecyclerViewAdapter mAdapter;
    private List<String> mCategories;
    private List<String> mPaymentMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        RecyclerView recyclerViewValues = (RecyclerView) findViewById(R.id.recycler_view_values);
        RecyclerView.LayoutManager recyclerViewLManager = new LinearLayoutManager(this);
        if(getIntent().getIntExtra(Constants.INTENT_EXTRA_RECYCLER_VIEW_ACTIVITY_MODE,
                Constants.RECYCLER_VIEW_CATEGORIES) == Constants.RECYCLER_VIEW_CATEGORIES){
            mValues = getCategories();
        } else {
            mValues = getPaymentMethods();
        }
        mAdapter = new RecyclerViewAdapter(mValues);
        recyclerViewValues.setHasFixedSize(true);
        recyclerViewValues.setLayoutManager(recyclerViewLManager);
        recyclerViewValues.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                mValues.remove(position);
                mAdapter.notifyItemRemoved(position);

                /*
                 adapter.notifyItemRemoved(position + 1);    //notifies the RecyclerView Adapter that data in adapter has been removed at a particular position.
                 adapter.notifyItemRangeChanged(position, adapter.getItemCount());   //notifies the RecyclerView Adapter that positions of element in adapter has been changed from position(removed element index to end of list), please update it.
                 */
            }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewValues);
    }

    public List<String> getCategories() {
        mCategories = new ArrayList<>();
        mCategories.add("Food");
        mCategories.add("Travel");
        mCategories.add("Entertainment");
        mCategories.add("Utilities");
        mCategories.add("Rent");
        mCategories.add("Home");
        mCategories.add("Groceries");
        mCategories.add("Tech");
        mCategories.add("Miscellaneous");
        mCategories.add("Fun");
        mCategories.add("Personal");
        mCategories.add("Shopping");
        return mCategories;
    }

    public List<String> getPaymentMethods(){
        mPaymentMethods = new ArrayList<>();
        mPaymentMethods.add("Discover");
        mPaymentMethods.add("Cash");
        mPaymentMethods.add("WF Checking");
        mPaymentMethods.add("WF Savings");
        mPaymentMethods.add("Amazon");
        return mPaymentMethods;
    }
}
