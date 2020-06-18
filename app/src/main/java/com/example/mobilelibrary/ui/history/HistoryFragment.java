package com.example.mobilelibrary.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilelibrary.R;
import com.example.mobilelibrary.models.TranslatedText;
import com.example.mobilelibrary.ui.home.HomeFragment;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HistoryFragment extends Fragment {

    private HistoryViewModel historyViewModel;
    private FirestoreRecyclerAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel =
                ViewModelProviders.of(this).get(HistoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        final RecyclerView recyclerView = root.findViewById(R.id.recycler_view_translated_text);

        // get query
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Query query = db.collection("translations")
                .whereEqualTo("userId", user.getUid());
        Toast.makeText(this.getActivity(), user.getUid(), Toast.LENGTH_SHORT).show();
        FirestoreRecyclerOptions<TranslatedText> options =
                new FirestoreRecyclerOptions.Builder<TranslatedText>()
                        .setQuery(query, TranslatedText.class)
                        .build();

        // create adapter
        adapter = new FirestoreRecyclerAdapter<TranslatedText, TranslatedTextViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull TranslatedTextViewHolder holder, int position, @NonNull TranslatedText model) {
                holder.setModel(model);

                holder.getTextViewTextToTranslate().setText(model.getTextToTranslate());
                holder.getTextViewTranslatedText().setText(model.getTranslatedText());
            }

            @NonNull
            @Override
            public TranslatedTextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_view_translated_text, parent, false);
                return new TranslatedTextViewHolder(view);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerView.setAdapter(adapter);

        return root;
    }
}
