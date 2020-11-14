package com.leandro.notes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leandro.notes.entity.Note;
import com.leandro.notes.utilities.Utilities;

import java.util.ArrayList;
import java.util.Collection;

public class AdapterNotes extends RecyclerView.Adapter<AdapterNotes.ViewHolderNotes> implements Filterable {

    private ArrayList<Note> listNotes;
    private  ArrayList<Note> listNotesAll;
    private static RecyclerViewClickListener clickListener;

    public AdapterNotes(ArrayList<Note> listNotes, RecyclerViewClickListener clickListener) {
        this.listNotes = listNotes;
        this.listNotesAll = new ArrayList<>(listNotes);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public AdapterNotes.ViewHolderNotes onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int layout;

        if (Utilities.IS_GRID_VIEW){
            layout = R.layout.staggered_recycler_view_item;
        }else{
             layout = R.layout.recycler_view_item;
        }

       View view = LayoutInflater.from(parent.getContext())
               .inflate(layout, parent, false);

        return new ViewHolderNotes(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterNotes.ViewHolderNotes holder, final int position) {
        try {
            holder.assingData(listNotes.get(position));
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listNotes.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Note> filteredNotes = new ArrayList<>();

            if (charSequence.toString().isEmpty()){
                filteredNotes.addAll(listNotesAll);
            }else {
                for (Note note: listNotesAll) {
                    if (note.getTitle().toLowerCase().contains(charSequence.toString().toLowerCase()) ||
                            note.getContent().toLowerCase().contains(charSequence.toString().toLowerCase())){
                        filteredNotes.add(note);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredNotes;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            listNotes.clear();
            listNotes.addAll((Collection<? extends Note>) results.values);
            notifyDataSetChanged();
        }
    };


    public class ViewHolderNotes extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView title;
        TextView content;

        public ViewHolderNotes(@NonNull View itemView) {
            super(itemView);
            title =  itemView.findViewById(R.id.titleView);
            content = itemView.findViewById(R.id.contentView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void assingData(Note newNote) {
            title.setText(newNote.getTitle());

            String newContent = newNote.getContent();
            content.setText(newContent.replaceAll("(?m)^[ \t]*\r?\n",""));
        }

        @Override
        public void onClick(View v) {
            clickListener.recyclerViewItemClicked(v,getLayoutPosition());
        }

        //On long click just call click listener with is to Select option
        @Override
        public boolean onLongClick(View v) {
            clickListener.recyclerViewItemLongClicked(true);
            return false;
        }
    }
}
