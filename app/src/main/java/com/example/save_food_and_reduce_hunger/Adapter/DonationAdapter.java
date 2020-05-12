package com.example.save_food_and_reduce_hunger.Adapter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.save_food_and_reduce_hunger.DonorDetailActivity;
import com.example.save_food_and_reduce_hunger.Model.Donation;
import com.example.save_food_and_reduce_hunger.R;
import com.squareup.picasso.Picasso;
import java.io.Serializable;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationViewHolder>
 {

    // to store the application context
    private Context context;

    // it will store the list of donation that has been made
    private List<Donation> donationList;

    private LinearLayout linearLayout;

    // views used in the donation list

    TextView name;
    ImageView image;
    Button getButton;
    Donation donationDetails;


    // paramterized constructor to for intialization
    public DonationAdapter(Context context, List<Donation> donationList, LinearLayout linearLayout
    , TextView name, ImageView image, Button button) {
        this.context = context;
        this.donationList = donationList;
        this.linearLayout = linearLayout;
        this.name = name;
        this.image = image;
        this.getButton = button;
    }


    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_view,parent,false);
        return new DonationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DonationViewHolder holder, final int position) {

        // get the donation with position no and set it to the layout

        final Donation donation = donationList.get(position);
        holder.name.setText(donation.getDonorName());

        // picasso to set image on the imageview

        Picasso.with(context)
                .load(donation.getImageUrl())
                .resize(360,460)
                .into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donationDetails = donationList.get(position);
                linearLayout.setVisibility(View.VISIBLE);
                name.setText(donation.getDonorName());
                Picasso.with(context)
                        .load(donation.getImageUrl())
                        .resize(400,400)
                        .into(image);
            }
        });
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send donation information to donation details activity
                Intent intent = new Intent(context, DonorDetailActivity.class);
                intent.putExtra("donationInformation", (Serializable) donationDetails);
                context.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return donationList.size();
    }
    public class DonationViewHolder extends RecyclerView.ViewHolder{
        // views used in the recyclerview list
        TextView name;
        ImageView imageView;
        public DonationViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txt);
            imageView = itemView.findViewById(R.id.img);
        }
    }
}
