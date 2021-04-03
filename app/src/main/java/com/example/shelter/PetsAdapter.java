package com.example.shelter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class PetsAdapter extends RecyclerView.Adapter<PetsAdapter.PetViewHolder> {
    private Context mContext;
    private List<Pet> mPets;
    //private List<Pet> mFullPets;
    private OnRecyclerViewItemClickListener mListener;

    public PetsAdapter(Context context,List<Pet> pets){
        mContext=context;
        mPets=pets;
       // mFullPets=new ArrayList<>(mPets);
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(mContext).inflate(R.layout.pets_list_item,parent,false);
        return new PetViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet currentPet=mPets.get(position);
        holder.name_tv.setText(currentPet.getName());
        Glide.with(mContext)
                .load(currentPet.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .placeholder(R.drawable.pets_launcher_foreground)
                .centerInside()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mPets.size();
    }

    /*@Override
    public Filter getFilter() {
        return petFilter;
    }
    private Filter petFilter=new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Pet> filteredList=new ArrayList<>();
            if (constraint==null || constraint.length()==0){
                filteredList.addAll(mFullPets);
            }else {
                String filterPattern=constraint.toString().toLowerCase().trim();
                for (Pet p : mFullPets){
                    if (p.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(p);
                    }
                }
            }
            FilterResults results=new FilterResults();
            results.values=filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mPets.clear();
            mPets.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };*/

    public class PetViewHolder extends RecyclerView.ViewHolder implements
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        ImageView imageView;
        TextView name_tv;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.image_pet);
            name_tv=itemView.findViewById(R.id.name_txt);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            MenuItem view_details=menu.add(Menu.NONE,1,1,"View details");
            MenuItem edit=menu.add(Menu.NONE,2,2,"Edit");
            MenuItem delete=menu.add(Menu.NONE,3,3,"Delete");
            view_details.setOnMenuItemClickListener(this);
            edit.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mListener!=null){
                int position= getAdapterPosition();
                if (position!=RecyclerView.NO_POSITION){
                    switch (item.getItemId()){
                        case 1:
                            mListener.OnViewDetailsClick(position);
                            return true;
                        case 2:
                            mListener.OnEditClick(position);
                            return true;
                        case 3:
                            mListener.OnDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener){
        mListener=listener;
    }
}
