package ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaen.selfqoutes.R;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import model.Journal;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Journal> journalList;

    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.journal_row, viewGroup, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Journal journal = journalList.get(position);
        String imageUrl;

        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.
                        getTimeAdded().
                        getSeconds() * 1000);

        holder.dateAdded.setText(timeAgo);
        holder.title.setText(journal.getTitle());
        holder.thought.setText(journal.getThought());
        imageUrl = journal.getImageUrl();

        Picasso.get()
                .load(imageUrl)
                //.placeholder()
                .fit()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public  TextView title, thought, dateAdded, name;
        public ImageView imageView, shareButton;
        TextView userId, userName;


        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            context = ctx;
            title = itemView.findViewById(R.id.cardTitle);
            thought = itemView.findViewById(R.id.cardThought);
            dateAdded = itemView.findViewById(R.id.cardTime);
            imageView = itemView.findViewById(R.id.cardImage);
            userName = itemView.findViewById(R.id.userName);

            shareButton = itemView.findViewById(R.id.cardShare);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //context.startActivity();
                }
            });

        }
    }
}
