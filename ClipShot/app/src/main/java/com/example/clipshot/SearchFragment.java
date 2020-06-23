package com.example.clipshot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static androidx.core.content.ContextCompat.getSystemService;

public class SearchFragment extends Fragment {

    // Declaring Variables
    String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String currentUsername;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Call text focus and keyboard for smooth transtion from feed fragment
        EditText searchQuery = Objects.requireNonNull(getActivity()).findViewById(R.id.searchQuery);
        searchQuery.requestFocus();

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);

        AppCompatImageView iconSearch = Objects.requireNonNull(getActivity()).findViewById(R.id.iconSearch);

        int SEARCHBAR_VISIBILITY = 0;
        Log.d("checkClick", String.valueOf(SEARCHBAR_VISIBILITY));

        iconSearch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View v) {

                ((MainActivity) Objects.requireNonNull(getActivity())).openFragment(FeedFragment.newInstance("",""));
                Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setElevation(20f); // Float == px
            }
        });

        // Document reference of user data that will read user data
        DocumentReference documentReference = db.collection("users").document(userUid);

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {

                    Map<String, Object> findCurrentUsername = document.getData();

                    currentUsername = (String) findCurrentUsername.get("Username");

                    Log.d("checkItem", currentUsername);
                } else {
                    Log.d("checkItem", "No such document");
                }
            } else {
                Log.d("checkItem", "get failed with ", task.getException());
            }
        });

        searchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                /*Uri imageUri;
                FirebaseStorage imageStorage;
                StorageReference storageReference;*/

                ArrayList<String> usernames = new ArrayList<>();
                ArrayList<String> gameNames = new ArrayList<>();

                db.collection("users")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                    Map<String, Object> findUsernames = document.getData();

                                    if (Objects.requireNonNull(findUsernames.get("Username")).toString().contains(s.toString().toLowerCase()) && s.toString().toLowerCase().length() > 0) {

                                        if (usernames.size() < 10) {
                                            usernames.add((String) findUsernames.get("Username"));
                                        } else {
                                            break;
                                        }

                                        if (currentUsername.contains(s.toString().toLowerCase())) {
                                            usernames.remove(currentUsername);
                                        }

                                        Log.d("checkTAG", String.valueOf(usernames));

                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.list_view_items, usernames);
                                        ListView lvData = Objects.requireNonNull(getActivity()).findViewById(R.id.lvData);
                                        lvData.setAdapter(adapter);

                                        lvData.setClickable(true);
                                        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                                                String pickedProfile = lvData.getItemAtPosition(position).toString();

                                                CollectionReference usersRef = db.collection("users");
                                                Query queryUser = usersRef.whereEqualTo("Username", pickedProfile);
                                                queryUser.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        String userVisitedUid ="";
                                                        String userVisitedEmail ="";
                                                        if (task.isSuccessful()) {
                                                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                                                String user = documentSnapshot.getString("Username");

                                                                if (user.equals(pickedProfile)) {
                                                                    Log.d("TAG", "User Exists antes de pesquisa");

                                                                    userVisitedUid = documentSnapshot.getId();
                                                                    userVisitedEmail= documentSnapshot.getString("Email");
                                                                    Log.d("check", "onComplete: " + userVisitedEmail);

                                                                    Log.d("TAG", "onComplete antes da pesquisa: " + userVisitedUid);


                                                                }

                                                            }
                                                        }

                                                        Bundle args = new Bundle();
                                                        args.putString("pickedProfile", pickedProfile);
                                                        args.putString("docID",userVisitedUid);
                                                        args.putString("email",userVisitedEmail);




                                                        VisitedProfileFragment fragment = new VisitedProfileFragment();
                                                        fragment.setArguments(args);

                                                        assert getFragmentManager() != null;
                                                        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();

                                                    }


                                                });


                                            }
                                        });
                                    }
                                }
                            } else {
                                Log.d("checkTAG", "Error getting documents: ", task.getException());
                            }
                        });

                db.collection("videos")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                    Map<String, Object> findGameNames = document.getData();

                                    if (Objects.requireNonNull(findGameNames.get("GameName")).toString().contains(s.toString()) && s.toString().length() > 0) {

                                        if (gameNames.size() < 1) {

                                            gameNames.add((String) findGameNames.get("GameName"));
                                            Log.d("checkTAG", String.valueOf(gameNames));
                                        } else {
                                            break;
                                        }



                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.list_view_items, gameNames);
                                        ListView lvData2 = Objects.requireNonNull(getActivity()).findViewById(R.id.lvDataGames);
                                        lvData2.setAdapter(adapter);

                                        lvData2.setClickable(true);
                                        lvData2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                                                String pickedGameName = lvData2.getItemAtPosition(position).toString();


                                                /*CollectionReference usersRef = db.collection("users");
                                                Query queryUser = usersRef.whereEqualTo("Username", pickedProfile);
                                                queryUser.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        String userVisitedUid ="";
                                                        String userVisitedEmail ="";
                                                        if (task.isSuccessful()) {
                                                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                                                String user = documentSnapshot.getString("Username");

                                                                if (user.equals(pickedProfile)) {
                                                                    Log.d("TAG", "User Exists antes de pesquisa");

                                                                    userVisitedUid = documentSnapshot.getId();
                                                                    userVisitedEmail= documentSnapshot.getString("Email");
                                                                    Log.d("check", "onComplete: " + userVisitedEmail);

                                                                    Log.d("TAG", "onComplete antes da pesquisa: " + userVisitedUid);


                                                                }

                                                            }
                                                        }*/

                                                        Bundle args = new Bundle();
                                                        args.putString("pickedGameName", pickedGameName);
                                                        /*args.putString("docID",userVisitedUid);
                                                        args.putString("email",userVisitedEmail);*/


                                                        VisitedGameFragment fragment = new VisitedGameFragment();
                                                        fragment.setArguments(args);

                                                        assert getFragmentManager() != null;
                                                        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                                                 /*   }
                                                });*/


                                            }
                                        });
                                    }
                                }
                            } else {
                                Log.d("checkTAG", "Error getting documents: ", task.getException());
                            }
                        });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}