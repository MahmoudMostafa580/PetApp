package com.example.shelter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PetsAdapter extends RecyclerView.Adapter<PetsAdapter.PetViewHolder>  {
    private Context mContext;
    private List<Pet> mPets;
    private OnRecyclerViewItemClickListener mListener;

    public PetsAdapter(Context context,List<Pet> pets){
        mContext=context;
        mPets=pets;
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
        holder.breed_tv.setText(currentPet.getBreed());
        holder.gender_tv.setText(currentPet.getGender());
        holder.weight_tv.setText(currentPet.getWeight());
        Picasso.with(mContext)
                .load(currentPet.getImageUrl())
                .placeholder(R.drawable.pets_launcher_foreground)
                .fit()
                .centerInside()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mPets.size();
    }

    public class PetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        ImageView imageView;
        TextView name_tv;
        TextView breed_tv;
        TextView gender_tv;
        TextView weight_tv;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.image_pet);
            name_tv=itemView.findViewById(R.id.name_txt);
            breed_tv=itemView.findViewById(R.id.breed_txt);
            gender_tv=itemView.findViewById(R.id.gender_txt);
            weight_tv=itemView.findViewById(R.id.weight_txt);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener!=null){
                int position= getAdapterPosition();
                if (position!=RecyclerView.NO_POSITION){
                    mListener.OnItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            MenuItem doWhatever=menu.add(Menu.NONE,1,1,"Do Whatever");
            MenuItem delete=menu.add(Menu.NONE,2,2,"Delete");
            doWhatever.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mListener!=null){
                int position= getAdapterPosition();
                if (position!=RecyclerView.NO_POSITION){
                    switch (item.getItemId()){
                        case 1:
                            mListener.OnWhateverClick(position);
                            return true;
                        case 2:
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
