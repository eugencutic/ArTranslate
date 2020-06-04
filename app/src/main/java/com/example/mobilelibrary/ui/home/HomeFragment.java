package com.example.mobilelibrary.ui.home;

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
import com.example.mobilelibrary.models.BookModel;
import com.example.mobilelibrary.util.TextTranslator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FirestoreRecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        final RecyclerView recyclerView = root.findViewById(R.id.recycler_view_books);

        // get query
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Query query = db.collection("books")
                .whereEqualTo("userId", user.getUid());
        Toast.makeText(this.getActivity(), user.getUid(), Toast.LENGTH_SHORT).show();
        FirestoreRecyclerOptions<BookModel> options =
                new FirestoreRecyclerOptions.Builder<BookModel>()
                .setQuery(query, BookModel.class)
                .build();

        // create adapter
        adapter = new FirestoreRecyclerAdapter<BookModel, BookViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BookViewHolder holder, int position, @NonNull BookModel model) {
                holder.setModel(model);

                holder.getTextViewBookAuthor().setText(model.getAuthor());
                holder.getTextViewBookTitle().setText(model.getTitle());
            }

            @NonNull
            @Override
            public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_view_book, parent, false);
                return new BookViewHolder(view);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerView.setAdapter(adapter);

//        TextTranslator textTranslator = new TextTranslator();
//        textTranslator.translateText(your text comes here);
//        String translatedText = textTranslator.getTranslatedText();

        return root;
    }

    @Override
    public void onStart(){
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (adapter != null)
            adapter.stopListening();
    }
}
