package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.Entities.Notification;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.andresdlg.groupmeapp.Utils.NotificationTypes.GROUP_INVITATION;

/**
 * Created by andresdlg on 13/01/18.
 */

public class RVNotificationAdapter extends RecyclerView.Adapter<RVNotificationAdapter.NotificationViewHolder>{

    private DatabaseReference usersRef;
    private DatabaseReference groupsRef;

    private DatabaseReference userRef;
    private DatabaseReference groupRef;
    private ValueEventListener usersEventListener;
    private ValueEventListener groupsEventListener;

    private List<Notification> notifications;
    private Context context;

    private OnSaveGroupListener mOnSaveGroupListener;
    private PrettyTime prettyTime;

    public RVNotificationAdapter(List<Notification> notifications, Context context){
        this.notifications = notifications;
        this.context = context;
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");
        this.prettyTime = new PrettyTime(new Locale("es"));

        onAttachToParentFragment(((AppCompatActivity)context).getSupportFragmentManager().getFragments().get(1));
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notifications_list_item, parent, false);
        return new NotificationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationViewHolder notificationViewHolder, @SuppressLint("RecyclerView") final int position) {
        ///TODO: Recuperar información del usuario que envio la notificacion con FirebaseDatabase

        if(!notifications.get(position).getType().equals(GROUP_INVITATION.toString())){
            userRef = usersRef.child(notifications.get(position).getFrom());
            usersEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    notificationViewHolder.setPosition(position);
                    notificationViewHolder.userAlias.setText(u.getAlias());
                    notificationViewHolder.setImage(context,u.getImageURL());
                    notificationViewHolder.hideBtn(context,notifications.get(position).getType());
                    notificationViewHolder.notificationMessage.setText(notifications.get(position).getMessage());
                    notificationViewHolder.setNewNotification(notifications.get(position).getState());

                    //String date = dateDifference(notifications.get(position).getDate());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(notifications.get(position).getDate());
                    String date = prettyTime.format(calendar);

                    notificationViewHolder.notificationDate.setText(date);
                    notificationViewHolder.setNotificationKey(notifications.get(position).getNotificationKey());

                    removeUserListener();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            userRef.addValueEventListener(usersEventListener);
        }else{
            groupRef = groupsRef.child(notifications.get(position).getFrom());
            groupsEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Group g = dataSnapshot.getValue(Group.class);
                    notificationViewHolder.setPosition(position);
                    notificationViewHolder.hideBtn(context,notifications.get(position).getType());
                    notificationViewHolder.setImage(context,g.getImageUrl());
                    notificationViewHolder.notificationMessage.setText(notifications.get(position).getMessage());
                    notificationViewHolder.setNewNotification(notifications.get(position).getState());
                    //String date = dateDifference(notifications.get(position).getDate());

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(notifications.get(position).getDate());
                    String date = prettyTime.format(calendar);

                    notificationViewHolder.notificationDate.setText(date);
                    notificationViewHolder.setGroupKey(g.getGroupKey());
                    notificationViewHolder.setNotificationKey(notifications.get(position).getNotificationKey());

                    removeGroupsListener();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            groupRef.addListenerForSingleValueEvent(groupsEventListener);
        }
    }

    private void removeGroupsListener() {
        groupRef.removeEventListener(groupsEventListener);
    }

    private void removeUserListener() {
        userRef.removeEventListener(usersEventListener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void updateNotificationStates() {
        for(int i = 0; i<notifications.size() ; i++){
            if(notifications.get(i).getState().equals(NotificationStatus.READ.toString())){
                notifyItemChanged(i);
            }
        }
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageButton menuBtn;
        TextView userAlias;
        ImageView userPhoto;
        ImageView newNotificationIndicator;
        TextView notificationMessage;
        TextView notificationDate;
        private String groupKey;
        private DatabaseReference groupRef;
        private String notificationKey;
        private int position;
        DatabaseReference userRef;

        NotificationViewHolder(View itemView) {
            super(itemView);
            userAlias = itemView.findViewById(R.id.userAlias);
            userAlias.setSelected(true);
            userPhoto = itemView.findViewById(R.id.contact_photo);
            newNotificationIndicator = itemView.findViewById(R.id.newNotificationIndicator);
            notificationMessage = itemView.findViewById(R.id.notificationText);
            notificationMessage.setSelected(true);
            notificationDate = itemView.findViewById(R.id.date);
            menuBtn = itemView.findViewById(R.id.menu_btn);
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId);
        }

        void setPosition(int position){
            this.position = position;
        }

        void setImage(final Context context, final String imageURL) {
            Glide.with(context)
                    .load(imageURL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            itemView.findViewById(R.id.homeprogress).setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(userPhoto);

        }

        void setGroupKey(String groupKey){
            this.groupKey = groupKey;
            groupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey);
        }

        void hideBtn(final Context context, String type) {
            if(!type.equals(GROUP_INVITATION.toString())){
                menuBtn.setVisibility(View.GONE);
            }else{
                userAlias.setVisibility(View.INVISIBLE);
                menuBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final PopupMenu popupMenu = new PopupMenu(context, view);
                        final Menu menu = popupMenu.getMenu();

                        popupMenu.getMenuInflater().inflate(R.menu.fragment_notifications_group_menu, menu);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                int id = menuItem.getItemId();
                                switch (id){
                                    case R.id.accept:
                                        //ENVIAR MENSAJE
                                        acceptInvitation(groupKey, position);
                                        break;
                                    case R.id.reject:
                                        //AGREGAR A GRUPO
                                        rejectInvitation(groupKey, position);
                                        break;
                                    default:
                                        break;
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                    }
                });
            }
        }

        private void acceptInvitation(String groupKey, int position) {
            userRef.child("groups").child(groupKey).child("status").setValue(GroupStatus.ACCEPTED.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Invitación aceptada", Toast.LENGTH_SHORT).show();
                }
            });
            userRef.child("notifications").child(notificationKey).removeValue();

            notifications.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notifications.size());

            mOnSaveGroupListener.onSavedGroup(true);
        }

        private void rejectInvitation(String groupKey, int position) {
            userRef.child("groups").child(groupKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Invitación rechazada", Toast.LENGTH_SHORT).show();
                }
            });
            userRef.child("notifications").child(notificationKey).removeValue();

            notifications.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notifications.size());

            mOnSaveGroupListener.onSavedGroup(true);
        }

        void setNotificationKey(String notificationKey) {
            this.notificationKey = notificationKey;
        }

        public void setNewNotification(String state) {
            if(state.equals(NotificationStatus.UNREAD.toString())){
                newNotificationIndicator.setVisibility(View.VISIBLE);
            }else {
                newNotificationIndicator.setVisibility(View.GONE);
            }
        }
    }

    public interface OnSaveGroupListener{
        void onSavedGroup(boolean saved);
    }

    private void onAttachToParentFragment(Fragment fragment){
        try {
            mOnSaveGroupListener = (OnSaveGroupListener) fragment;
        }
        catch (ClassCastException e){
            throw new ClassCastException(fragment.toString() + " must implement OnUserSelectionSetListener");
        }
    }
}
