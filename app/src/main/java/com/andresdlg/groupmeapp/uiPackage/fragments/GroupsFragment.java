package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.RVGroupAdapter;
import com.andresdlg.groupmeapp.Adapters.RVNotificationAdapter;
import com.andresdlg.groupmeapp.DialogFragments.HeaderDialogFragment;
import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.Utils.GroupType;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class GroupsFragment extends Fragment implements View.OnClickListener, HeaderDialogFragment.OnSaveGroupListener, RVNotificationAdapter.OnSaveGroupListener{

    TextView tvNoGroups;
    RVGroupAdapter adapter;
    DatabaseReference mUserGroupsRef;
    DatabaseReference groupsRef;
    List<Group> groups;
    RecyclerView rv;
    FloatingActionButton mFloatingActionButton;

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_groups,container,false);
        setRetainInstance(true);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        groups = new ArrayList<>();

        tvNoGroups = view.findViewById(R.id.tvNoGroups);

        //Recicler view
        rv = view.findViewById(R.id.rvGroups);
        rv.setHasFixedSize(true); //El tamaño queda fijo, mejora el desempeño
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        //Floating action button
        mFloatingActionButton = view.findViewById(R.id.fabGroups);
        mFloatingActionButton.setOnClickListener(this);

        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");
        mUserGroupsRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("groups");
    }

    private void getAllGroups(){
        mUserGroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(data.child("status").getValue().toString().equals(GroupStatus.ACCEPTED.toString())){
                        getGroup(data.getKey());
                    }
                }

                if(!dataSnapshot.hasChildren()){
                    tvNoGroups.setVisibility(View.VISIBLE);
                }else {
                    tvNoGroups.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapter = new RVGroupAdapter(getContext(),groups);
        rv.setAdapter(adapter);


    }

    private void getGroup(String key) {
        DatabaseReference groupRef = groupsRef.child(key);
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group u = dataSnapshot.getValue(Group.class);
                if(u != null){
                    updateGroups(u);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fabGroups:
                showHeaderDialogFragment();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getAllGroups();
    }

    private void showHeaderDialogFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        HeaderDialogFragment newFragment = new HeaderDialogFragment(GroupType.GROUP);
        newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    @Override
    public void onSavedGroup(boolean saved) {
        if(saved){
            getAllGroups();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isAdded()){
            if(isVisibleToUser){
                mFloatingActionButton.show();
            }else{
                mFloatingActionButton.hide();
            }
        }
    }

    private void updateGroups(Group group) {
        boolean exists = false;
        for(int i=0; i < groups.size(); i++){
            if(groups.get(i).getGroupKey().equals(group.getGroupKey())){
                exists = true;
                groups.remove(i);
                groups.add(i,group);
                adapter.notifyItemChanged(i);
            }
        }
        if(!exists){
            groups.add(group);
            adapter.notifyDataSetChanged();
        }
    }
}
