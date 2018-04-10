package com.andresdlg.groupmeapp.uiPackage;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.RVSearchContactAdapter;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ReciclerViewClickListener.RecyclerClick_Listener;
import com.andresdlg.groupmeapp.uiPackage.ReciclerViewClickListener.RecyclerTouchListener;
import com.andresdlg.groupmeapp.uiPackage.fragments.GroupAddMembersFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SearchContactActivity extends AppCompatActivity {

    private SearchView searchView;
    List<Users> users;
    DatabaseReference firebaseContacts;
    RVSearchContactAdapter rvSearchContactAdapter;
    RecyclerView rvAddGroupMember;

    CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    int selected;

    //Multiselect stuff
    private ActionMode mActionMode;
    private boolean isShowing;
    private int pxUp;

    DatabaseReference mUsersDatabase;
    DatabaseReference mGroupsDatabase;

    View v;

    //OnUserSelectionSetListener mOnUserSelectionSetListener;

    private String groupKey;
    private List<Users> groupUsers;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_groups_dialog_add_contacts);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Agregar nuevo miembro");

        coordinatorLayout = findViewById(R.id.clgroupaddmembers);

        groupKey = getIntent().getStringExtra("groupKey");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mGroupsDatabase = FirebaseDatabase.getInstance().getReference("Groups");
        mGroupsDatabase.child(groupKey).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //RECYCLERVIEW INITIALIZATION
        rvAddGroupMember = findViewById(R.id.rvAddGroupMember);
        rvAddGroupMember.setHasFixedSize(true);
        rvAddGroupMember.setItemAnimator(new DefaultItemAnimator());

        users = new ArrayList<>();
        //groupUsers = new ArrayList<>();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvAddGroupMember.setLayoutManager(llm);

        rvSearchContactAdapter = new RVSearchContactAdapter(users,this,null);
        rvAddGroupMember.setAdapter(rvSearchContactAdapter);

        searchView = findViewById(R.id.toolbar);
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();
        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                rvSearchContactAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                rvSearchContactAdapter.getFilter().filter(query);
                return false;
            }
        });

        snackbar = Snackbar
                    .make(coordinatorLayout, selected + " Seleccionados",Snackbar.LENGTH_INDEFINITE)
                    .setAction("INVITAR", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sendInvitations();
                            Toast.makeText(SearchContactActivity.this, "Invitación enviada", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    });

        /*List<Fragment> fragments = parentFragment.getChildFragmentManager().getFragments();
        v = fragments.get(0).getView().findViewById(R.id.nsSetup);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        pxUp = Math.round(50 * (displayMetrics.xdpi) / DisplayMetrics.DENSITY_DEFAULT);*/


        //groupKey = ((FireApp) getActivity().getApplication()).getGroupKey();
        groupUsers = ((FireApp) this.getApplication()).getGroupUsers();

        fetchContacts();

        //Aca arranca el multiselect
        implementRecyclerViewClickListeners();
    }

    private void sendInvitations() {
        List<String> selectedMembersId = rvSearchContactAdapter.getSelectedIds();

        DatabaseReference membersRef =  mGroupsDatabase.child(groupKey).child("members");
        for(String id : selectedMembersId){
            membersRef.child(id).setValue(Roles.MEMBER);
        }

        Map<String,Object> map2;
        map2 = new HashMap<>();
        map2.put("status", GroupStatus.PENDING);

        for(final String id : selectedMembersId){
            mUsersDatabase.child(id).child("groups").child(groupKey).updateChildren(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(SearchContactActivity.this,"Grupo guardado en "+id,Toast.LENGTH_SHORT).show();
                }
            });

            DatabaseReference userToNotifications = mUsersDatabase.child(id).child("notifications");
            String notificationKey = userToNotifications.push().getKey();
            Map<String,Object> notification = new HashMap<>();
            notification.put("notificationKey",notificationKey);
            notification.put("title","Invitación a grupo");
            notification.put("message","Has recibido una invitación para unirte al grupo " + name);
            notification.put("from", groupKey);
            notification.put("state", NotificationStatus.UNREAD);
            notification.put("date", Calendar.getInstance().getTime());
            notification.put("type", NotificationTypes.GROUP_INVITATION);

            userToNotifications.child(notificationKey).setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(SearchContactActivity.this, "Invitación de grupo enviada", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void implementRecyclerViewClickListeners() {
        rvAddGroupMember.addOnItemTouchListener(new RecyclerTouchListener(this, rvAddGroupMember, new RecyclerClick_Listener() {
            @Override
            public void onClick(View view, int position) {
                onListItemSelect(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                //Select item on long click
                //onListItemSelect(position);
            }
        }));
    }

    private void fetchContacts() {

        firebaseContacts = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("friends");
        firebaseContacts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                rvSearchContactAdapter.notifyDataSetChanged();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Getting the data from snapshot
                    if(postSnapshot.child("status").getValue().equals(FriendshipStatus.ACCEPTED.toString())){
                        getUser(postSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUser(final String userKey) {
        final DatabaseReference user = FirebaseDatabase.getInstance().getReference("Users").child(userKey);
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users u = dataSnapshot.getValue(Users.class);
                if(!users.contains(u) && !validateExistingMembers(u)){
                    users.add(u);
                    rvSearchContactAdapter.notifyDataSetChanged();
                    //rvSearchContactAdapter.setUsers(users);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean validateExistingMembers(Users u) {
        for(Users user : groupUsers){
            if(u.getUserid().equals(user.getUserid())){
                return true;
            }
        }
        return false;
    }

    private void onListItemSelect(int position) {

        //mOnUserSelectionSetListener.onUserSelectionSet(rvSearchContactAdapter.getSelectedIds());

        rvSearchContactAdapter.toggleSelection(position);//Toggle the selection
        selected = rvSearchContactAdapter.getSelectedCount();
        snackbar.setText( selected + ((selected > 1 ? " Seleccionados" : " Seleccionado")));
        if(selected > 0 && !isShowing){
            //rvAddGroupMember.setTranslationY(pxUp*-1);
            //rvAddGroupMember.setPadding(0,0,0,pxUp);
            snackbar.show();
            isShowing = true;
            //v.setPadding(0,0,0,pxUp);
        }else if(selected == 0 && isShowing){
            //rvAddGroupMember.setPadding(0,0,0,0);
            //v.setPadding(0,0,0,0);
            //rvAddGroupMember.setTranslationY(pxDown);
            snackbar.dismiss();
            isShowing = false;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}