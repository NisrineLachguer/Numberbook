package com.lachguer.numbook;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<String> contacts;

    public ContactAdapter(List<String> contacts) {
        this.contacts = contacts;
    }

    public void updateContacts(List<String> newContacts) {
        this.contacts = newContacts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        String contact = contacts.get(position);
        String[] parts = contact.split("\n");
        String name = parts[0];
        String number = parts.length > 1 ? parts[1] : "";

        holder.tvName.setText(name);
        holder.tvNumber.setText(number);

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.mipmap.ic_person)
                .error(R.mipmap.ic_person);

        String firstLetter = name.isEmpty() ? "" : name.substring(0, 1).toUpperCase();

        Glide.with(holder.itemView.getContext())
                .load(firstLetter)
                .apply(requestOptions)
                .into(holder.ivAvatar);

        holder.ivCall.setOnClickListener(v -> {
            if (!number.isEmpty()) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + number));
                holder.itemView.getContext().startActivity(callIntent);
            } else {
                Toast.makeText(holder.itemView.getContext(),
                        "Numéro non disponible", Toast.LENGTH_SHORT).show();
            }
        });

        holder.ivMessage.setOnClickListener(v -> {
            if (!number.isEmpty()) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setData(Uri.parse("smsto:" + number));
                holder.itemView.getContext().startActivity(smsIntent);
            } else {
                Toast.makeText(holder.itemView.getContext(),
                        "Numéro non disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts != null ? contacts.size() : 0;
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        CircularImageView ivAvatar;
        TextView tvName;
        TextView tvNumber;
        ImageView ivCall;
        ImageView ivMessage;

        ContactViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvNumber = itemView.findViewById(R.id.tv_number);
            ivCall = itemView.findViewById(R.id.iv_call);
            ivMessage = itemView.findViewById(R.id.iv_message);
        }
    }
}