package com.leandro.notes;

import android.view.View;

public interface RecyclerViewClickListener {
    //Default clicks
    void recyclerViewItemClicked(View view, int position);
    //Clicks after a Long click
    void recyclerViewItemLongClicked(boolean isToSelect);
}
