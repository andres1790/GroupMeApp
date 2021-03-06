package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.RVSubGroupAdapter;
import com.andresdlg.groupmeapp.DialogFragments.HeaderDialogFragment;
import com.andresdlg.groupmeapp.Entities.SubGroup;
import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupType;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by andresdlg on 02/05/17.
 */

public class SubGroupsFragment extends Fragment {

    //SwipeRefreshLayout swipeContainer;

    //FloatingActionButton fab;
    RapidFloatingActionButton rfaBtn;
    RapidFloatingActionLayout rfaLayout;
    String groupKey;
    RecyclerView rvSubGroups;
    ProgressBar progressBar;
    TextView tvNoSubGroups;

    RVSubGroupAdapter rvSubGroupsAdapter;
    List<SubGroup> subGroups;
    LinearLayoutManager llm;

    View vista;

    DatabaseReference subGroupsRef;

    Bundle bundle;

    Animation fadeIn;
    Animation fadeOut;
    private boolean estabaEnElUltimoElemento;
    Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        bundle = getArguments();

        groupKey = bundle.getString("groupKey");

        subGroups = new ArrayList<>();

        //groupKey = ((FireApp) getActivity().getApplication()).getGroupKey();

        subGroupsRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups");

        setAnimations();

        handler = new Handler();

        return inflater.inflate(R.layout.fragment_sub_groups,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRetainInstance(true);

        vista = view;

        progressBar = view.findViewById(R.id.progressBar);

        tvNoSubGroups = view.findViewById(R.id.tvNoSubGroups);

        /*fab = view.findViewById(R.id.fabSubGroups);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHeaderDialogFragment();
            }
        });*/

        rfaBtn = getActivity().findViewById(R.id.activity_main_rfab);
        rfaLayout = getActivity().findViewById(R.id.activity_main_rfal);

        rvSubGroups = view.findViewById(R.id.rvSubGroups);
        //rvSubGroups.setHasFixedSize(false); //El tamaño queda fijo, mejora el desempeño
        llm = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        rvSubGroups.setLayoutManager(llm);
        rvSubGroupsAdapter = new RVSubGroupAdapter(subGroups,groupKey,getContext());
        rvSubGroups.setAdapter(rvSubGroupsAdapter);
        rvSubGroups.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    if(llm.findLastCompletelyVisibleItemPosition() != subGroups.size()-1){
                        if(estabaEnElUltimoElemento){
                            rfaBtn.startAnimation(fadeIn);
                            estabaEnElUltimoElemento = false;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    rfaLayout.setVisibility(View.VISIBLE);
                                }
                            }, 100);
                        }
                        //rfaBtn.setVisibility(View.VISIBLE);
                        //fab.show();
                    }else{
                        if(recyclerView.canScrollVertically(-1) || recyclerView.canScrollVertically(1)){
                            if (!recyclerView.canScrollVertically(1)) {
                                rfaBtn.startAnimation(fadeOut);
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        rfaLayout.setVisibility(View.GONE);
                                    }
                                }, 200);
                                estabaEnElUltimoElemento = true;
                            }
                        }
                    }
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                //if(dy > 0 || dx < 0 && fab.isShown()){
                /*if((dy > 0 || dx < 0)  && (rfaBtn.getVisibility() == View.VISIBLE)){
                    if(recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE){
                        rfaBtn.startAnimation(fadeOut);
                    }
                }*/
            }
        });


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)rvSubGroups.getLayoutParams();

        if(bundle.getBoolean("fromNotificationSubGroupInvitation")){
            int newMarginDp = 32;
            params.topMargin = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newMarginDp, metrics));
        }

        if(bundle.getBoolean("fromNotificationNewPost")){
            int newMarginDp = 8;
            params.topMargin = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newMarginDp, metrics));
        }

        if(!bundle.getBoolean("fromNotificationSubGroupInvitation") && !bundle.getBoolean("fromNotificationNewPost")){
            int newMarginDp = 8;
            params.topMargin = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newMarginDp, metrics));
        }

        fillSubGroups();

        //swipeContainer = view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        /*swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                //fetchTimelineAsync(0);
                //fillSubGroups(view);
                //swipeContainer.setRefreshing(false);
            }
        });*/

        // Configure the refreshing colors
        /*swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);*/

        //swipeContainer.setRefreshing(true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isAdded()){
            if(isVisibleToUser){
                //fab.show();
            }else{
                //fab.hide();
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void fillSubGroups() {
        subGroupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot data, String s) {
                SubGroup sgf = new SubGroup(data.child("name").getValue().toString(),null,null,null,null);
                sgf.setName(data.child("name").getValue().toString());
                sgf.setImageUrl(data.child("imageUrl").getValue().toString());
                sgf.setMembers((Map<String,String>) data.child("members").getValue());
                sgf.setSubGroupKey(data.child("subGroupKey").getValue().toString());
                List<Task> tasks = new ArrayList();
                for(DataSnapshot d : data.child("tasks").getChildren()){
                    Task task = d.getValue(Task.class);
                    tasks.add(task);
                }
                sgf.setTasks(tasks);
                subGroups.add(sgf);
                rvSubGroupsAdapter.setCantidadTasks(sgf.getSubGroupKey(),tasks.size());
                rvSubGroupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot data, String s) {
                if(data.child("members").getValue()!=null){
                    SubGroup sgf = new SubGroup(data.child("name").getValue().toString(),null,null,null,null);
                    sgf.setName(data.child("name").getValue().toString());
                    sgf.setImageUrl(data.child("imageUrl").getValue().toString());
                    sgf.setMembers((Map<String,String>) data.child("members").getValue());
                    sgf.setSubGroupKey(data.child("subGroupKey").getValue().toString());
                    List<Task> tasks = new ArrayList();
                    for(DataSnapshot d : data.child("tasks").getChildren()){
                        Task task = d.getValue(Task.class);
                        tasks.add(task);
                    }
                    sgf.setTasks(tasks);
                    int i = findPosition(sgf.getSubGroupKey());
                    if(i != -1){
                        subGroups.remove(i);
                        subGroups.add(i,sgf);
                    }

                    RVSubGroupAdapter.taskTypes type = rvSubGroupsAdapter.checkTasksSize(subGroups.get(i).getSubGroupKey(),tasks.size());

                    if(type == RVSubGroupAdapter.taskTypes.NEW_TASK){
                        rvSubGroupsAdapter.setNewTaskFlag();
                        rvSubGroupsAdapter.notifyItemChanged(i);
                    }else if(type == RVSubGroupAdapter.taskTypes.UPDATED_TASK){
                        rvSubGroupsAdapter.setUpdatedTaskFlag();
                        rvSubGroupsAdapter.notifyItemChanged(i);
                    }else{
                        rvSubGroupsAdapter.setDeletedTaskFlag();
                        rvSubGroupsAdapter.notifyDataSetChanged();
                    }

                    //Container.setRefreshing(false);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot data) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        subGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    tvNoSubGroups.setVisibility(View.GONE);
                    rvSubGroups.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }else {
                    tvNoSubGroups.setVisibility(View.VISIBLE);
                    rvSubGroups.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int findPosition(String subGroupKey) {
        for(int i = 0; i < subGroups.size(); i++){
            if (subGroupKey.equals(subGroups.get(i).getSubGroupKey())){
                return i;
            }
        }
        return -1;
    }

    private void setAnimations(){
        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(100);
        fadeIn.setFillEnabled(true);
        fadeIn.setFillAfter(true);

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(200);
        fadeOut.setFillEnabled(true);
        fadeOut.setFillAfter(true);
    }

    /*private void showHeaderDialogFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        HeaderDialogFragment newFragment = new HeaderDialogFragment(GroupType.SUBGROUP,groupKey);
        newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }*/
}
