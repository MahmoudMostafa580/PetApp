package com.example.shelter;

public interface OnRecyclerViewItemClickListener {
    void OnEditClick(int position);
    void OnViewDetailsClick(int position);
    void OnDeleteClick(int position);
}
