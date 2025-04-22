package com.example.newstep.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newstep.ProgressActivity;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Databases.QuoteDatabaseHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;


public class HomeFragment extends Fragment {
    TextView textViewQuote,welcome;
    QuoteDatabaseHelper dbHelper;
    String quote;
    ImageView click;
    Button g;
    CardView ProgressCard,BadgesCard;
    long lastUpdatedTime;
    FirebaseFirestore fdb;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        textViewQuote = rootView.findViewById(R.id.quote_textView);
        click=rootView.findViewById(R.id.clickIcon);
        welcome=rootView.findViewById(R.id.welcomeText);
        g=rootView.findViewById(R.id.goals_button);
        BadgesCard=rootView.findViewById(R.id.badges_card);
        ProgressCard =rootView.findViewById(R.id.progress_card);
        fdb=FirebaseFirestore.getInstance();

        String currentUserId = FirebaseUtil.getCurrentUserId();

        if (currentUserId != null) {

            fdb.collection("Users").document(currentUserId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String username = document.getString("username");
                                if (username != null) {
                                    welcome.setText("Welcome " + username+"!");
                                } else {
                                    welcome.setText("Welcome!");
                                }
                            }
                        }
                    });
        } else {

            welcome.setText("Welcome!");
        }



        dbHelper = new QuoteDatabaseHelper(getContext());
        String[] quoteData = dbHelper.getQuote();
        quote = quoteData[0];
        lastUpdatedTime = Long.parseLong(quoteData[1]);

        Calendar last = Calendar.getInstance();
        last.setTimeInMillis(lastUpdatedTime);

        Calendar now = Calendar.getInstance();
        boolean isNewDay = now.get(Calendar.DAY_OF_YEAR) != last.get(Calendar.DAY_OF_YEAR)
                || now.get(Calendar.YEAR) != last.get(Calendar.YEAR);

        if (isNewDay) {
            quote = "Write your quote";
            click.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "It's a new day! Don't forget to write your quote.", Toast.LENGTH_SHORT).show();
        }

        textViewQuote.setText(quote);
      textViewQuote.setOnLongClickListener(new View.OnLongClickListener() {
          @Override
          public boolean onLongClick(View view) {
              showQuoteEditDialog(quote);
              return true;
          }
      });


        g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                Fragment existingFragment = fragmentManager.findFragmentByTag(GoalsFragment.class.getSimpleName());
                if (existingFragment != null) {
                    fragmentManager.beginTransaction().remove(existingFragment).commit();
                }

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                GoalsFragment goalsFragment=new GoalsFragment();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.fragment_container, goalsFragment, GoalsFragment.class.getSimpleName());
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();
            }
        });

        ProgressCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                Fragment existingFragment = fragmentManager.findFragmentByTag(GoalsFragment.class.getSimpleName());
                if (existingFragment != null) {
                    fragmentManager.beginTransaction().remove(existingFragment).commit();
                }

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                ProgressFragment progressFragment= new ProgressFragment();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.fragment_container, progressFragment, ProgressFragment.class.getSimpleName());
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();
            }
        });

        BadgesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                Fragment existingFragment = fragmentManager.findFragmentByTag(BadgesFragment.class.getSimpleName());
                if (existingFragment != null) {
                    fragmentManager.beginTransaction().remove(existingFragment).commit();
                }

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                BadgesFragment badgesFragment= new BadgesFragment();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.fragment_container, badgesFragment, BadgesFragment.class.getSimpleName());
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();

            }
        });
        return rootView;
    }


    private void showQuoteEditDialog(String currentQuote) {
        final EditText editText = new EditText(getContext());
        editText.setText(currentQuote);

        new AlertDialog.Builder(getContext())
                .setTitle("Edit your quote")
                .setView(editText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newQuote = editText.getText().toString().trim();
                        if (newQuote.isEmpty()){
                            Toast.makeText(getContext(), "Error: the quote is empty",
                                    Toast.LENGTH_SHORT).show();

                        } else if ( newQuote.length() > 50) {
                            Toast.makeText(getContext(), "your quote is too long!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            dbHelper.saveQuote(newQuote);
                            textViewQuote.setText(newQuote);
                            lastUpdatedTime = System.currentTimeMillis();
                            click.setVisibility(View.GONE);
                        }

                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("lastFragment", "HomeFragment").apply();
    }
}