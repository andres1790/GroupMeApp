package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.ContextValidator;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.MainActivity;
import com.andresdlg.groupmeapp.uiPackage.UserProfileSetupActivity;
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

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 17/03/18.
 */

public class RVGroupDetailAdapter extends RecyclerView.Adapter<RVGroupDetailAdapter.GroupDetailViewHolder>{

    private DatabaseReference groupsRef;
    private List<Users> usersList;
    private Map<String, String> usersRoles;
    private String groupKey;
    private Context context;
    private String myRol;
    private int cantAdmins;

    public RVGroupDetailAdapter(List<Users> usersList, Map<String, String> usersRoles,String groupKey, Context context){
        this.usersList = usersList;
        this.usersRoles = usersRoles;
        this.groupKey = groupKey;
        this.context = context;
        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");
    }

    @NonNull
    @Override
    public GroupDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_group_details_list_item, parent, false);
        return new GroupDetailViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupDetailViewHolder groupDetailViewHolder, @SuppressLint("RecyclerView") final int position) {
        Users u = usersList.get(position);
        String rol = null;
        for(Map.Entry<String, String> entry : usersRoles.entrySet()) {
            if(u.getUserid().equals(entry.getKey())){
                rol = entry.getValue();
            }
        }
        groupDetailViewHolder.setDetails(context,u.getName(),u.getAlias(),rol,u.getImageURL(),u.getUserid());
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void setMyRol() {
        myRol = null;
        if(usersRoles.size() > 0){
            for(Map.Entry<String, String> entry : usersRoles.entrySet()) {
                if(StaticFirebaseSettings.currentUserId.equals(entry.getKey())){
                    myRol = entry.getValue();
                }
            }
        }
    }

    private void setAdminCount(){
        cantAdmins = 0;
        for(Map.Entry<String, String> entry : usersRoles.entrySet()) {
            if(entry.getValue().equals(Roles.ADMIN.toString())){
                cantAdmins += 1;
            }
        }
    }


    class GroupDetailViewHolder extends RecyclerView.ViewHolder{

        ValueEventListener groupEventListener;

        DatabaseReference ref;

        View mView;

        GroupDetailViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        @SuppressLint("SetTextI18n")
        void setDetails(final Context context, String contactName, final String contactAlias, final String rol, final String contactPhoto, final String iduser){
            final CircleImageView mContactPhoto = mView.findViewById(R.id.contact_photo);
            TextView mContactName = mView.findViewById(R.id.tvUserName);
            TextView mContactAlias = mView.findViewById(R.id.tvUserAlias);
            final TextView mContactRol = mView.findViewById(R.id.tvRol);
            final ImageButton btnMenu = mView.findViewById(R.id.btn_menu);
            RelativeLayout rl = mView.findViewById(R.id.rl);

            btnMenu.setVisibility(View.GONE);

            mContactAlias.setText(String.format("@%s", contactAlias));
            mContactAlias.setSelected(true);

            mContactName.setText(contactName);
            mContactName.setSelected(true);

            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userProfileIntent = new Intent(context, UserProfileSetupActivity.class);
                    userProfileIntent.putExtra("iduser",iduser);
                    Pair<View, String> p1 = Pair.create((View)mContactPhoto, "userPhoto");
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((AppCompatActivity)context, p1);
                    context.startActivity(userProfileIntent, options.toBundle());
                }
            });

            setMyRol();
            if(myRol != null){
                if(myRol.equals(Roles.ADMIN.toString()) || iduser.equals(StaticFirebaseSettings.currentUserId)){
                    btnMenu.setVisibility(View.VISIBLE);
                }
            }

            if(ContextValidator.isValidContextForGlide(itemView.getContext())){
                Glide.with(itemView.getContext())
                        .load(contactPhoto)
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
                        .into(mContactPhoto);
            }

            ref = groupsRef.child(groupKey).child("members").child(iduser);

            if(rol.equals(Roles.ADMIN.toString())){
                mContactRol.setText("ADMINISTRADOR");
            }else{
                mContactRol.setText("MIEMBRO");
            }
            mContactRol.setSelected(true);

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(context, view);
                    final Menu menu = popupMenu.getMenu();
                    if(rol.equals(Roles.ADMIN.toString()) && myRol.equals(Roles.ADMIN.toString())){
                        //btnMenu.setVisibility(View.VISIBLE);

                        popupMenu.getMenuInflater().inflate(R.menu.activity_group_detail_item_admin_to_admin_menu, menu);

                        if(iduser.equals(StaticFirebaseSettings.currentUserId)){
                            popupMenu.getMenu().getItem(1).setTitle("Abandonar grupo");
                        }

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                int id = menuItem.getItemId();
                                switch (id){
                                    case R.id.rolAdmin:

                                        setAdminCount();

                                        if(cantAdmins >1){
                                            ref.setValue(Roles.MEMBER).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //setAdminCount();
                                                    usersRoles.put(iduser, Roles.MEMBER.toString());
                                                    notifyDataSetChanged();
                                                }
                                            });
                                            Toast.makeText(context,"Este usuario ya no es administrador", Toast.LENGTH_SHORT).show();
                                            break;
                                        }else{
                                            Toast.makeText(context,"Debe haber por lo menos un administrador", Toast.LENGTH_SHORT).show();
                                            break;
                                        }


                                    case R.id.delete:

                                        setAdminCount();

                                        if(cantAdmins >1){
                                            deleteUserFromGroup(iduser,getAdapterPosition(),false);
                                            Toast.makeText(context,"Eliminado", Toast.LENGTH_SHORT).show();
                                            break;
                                        }else{
                                            if(usersList.size()>1){
                                                Toast.makeText(context,"Debe haber por lo menos un administrador", Toast.LENGTH_SHORT).show();
                                            }else {
                                                new AlertDialog.Builder(context,R.style.MyDialogTheme)
                                                        .setTitle("¿Esta seguro que desea abandonar el grupo?")
                                                        .setMessage("Como usted es el unico miembro del grupo, al abandonarlo el mismo será eliminado asi como todo su contenido")
                                                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                deleteUserFromGroup(iduser,getAdapterPosition(),true);
                                                            }
                                                        })
                                                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        })
                                                        .setCancelable(false)
                                                        .show();
                                            }
                                        }
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                    }else if(rol.equals(Roles.MEMBER.toString()) && myRol.equals(Roles.ADMIN.toString())) {
                        //btnMenu.setVisibility(View.VISIBLE);
                        popupMenu.getMenuInflater().inflate(R.menu.activity_group_detail_item_admin_to_member_menu, menu);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                int id = menuItem.getItemId();
                                switch (id) {
                                    case R.id.rolAdmin:
                                        ref.setValue(Roles.ADMIN).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                usersRoles.put(iduser, Roles.ADMIN.toString());
                                                notifyDataSetChanged();
                                            }
                                        });

                                        Toast.makeText(context, "Hacer admin", Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.delete:
                                        //rejectRequest(iduser);
                                        deleteUserFromGroup(iduser,getAdapterPosition(),false);
                                        Toast.makeText(context, "Eliminar", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                    } else{

                        popupMenu.getMenuInflater().inflate(R.menu.activity_group_detail_item_admin_to_member_menu, menu);
                        popupMenu.getMenu().getItem(0).setVisible(false);
                        popupMenu.getMenu().getItem(1).setTitle("Abandonar grupo");
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                int id = menuItem.getItemId();
                                switch (id) {
                                    case R.id.delete:
                                        //rejectRequest(iduser);
                                        deleteUserFromGroup(iduser,getAdapterPosition(),false);
                                        Toast.makeText(context, "Eliminar", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                    }
                }
            });
        }

        private void deleteUserFromGroup(final String userId, final int position, boolean deleteAllContent) {

            ((FireApp) context.getApplicationContext()).setGroupUsers(null);

            //ELIMINAR DE LOS GRUPOS
            DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey);
            groupRef.child("members").child(userId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Toast.makeText(context, "Borrado del grupo", Toast.LENGTH_SHORT).show();

                    usersList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, usersList.size());
                    ((FireApp) context.getApplicationContext()).setGroupUsers(usersList);
                }
            });

            //ELIMINAR DE LOS SUBGRUPOS
            groupEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot d : dataSnapshot.getChildren()){
                        DatabaseReference childRef = d.child("members").child(userId).getRef();
                        childRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                               // Toast.makeText(context, "Borrado de los subgrupos", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    removeListener1();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            groupRef.child("subgroups").addListenerForSingleValueEvent(groupEventListener);

            //ELIMINAR DE LOS USUARIOS
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("groups").child(groupKey);
            usersRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                }
            });


            //ELIMINAR DEL MAPA DE ROLES
            usersRoles.remove(userId);

            //BORRO EL GRUPO Y TODAS LAS INVITACIONES PENDIENTES SI SOY EL UNICO MIEMBRO QUE QUEDABA
            if(deleteAllContent){
                groupRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "El grupo ha sido eliminado", Toast.LENGTH_SHORT).show();
                    }
                });

                final DatabaseReference usersNotifications = FirebaseDatabase
                        .getInstance()
                        .getReference("Users");
                usersNotifications.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot firebaseUser : dataSnapshot.getChildren()){
                            for(DataSnapshot noti : firebaseUser.child("notifications").getChildren()){
                                if(noti.child("type").getValue().toString().equals(NotificationTypes.GROUP_INVITATION.toString())
                                        && noti.child("from").getValue().toString().equals(groupKey)){
                                    usersNotifications
                                            .child(firebaseUser.getKey())
                                            .child("notifications")
                                            .child(noti.getKey())
                                            .removeValue();

                                    usersNotifications
                                            .child(firebaseUser.getKey())
                                            .child("groups")
                                            .child(groupKey)
                                            .removeValue();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }


            //BORRO ALL EL CONTENIDO DEL GRUPO QUE ESTA EN EL STORAGE SI CORRESPONDE
            //IMPLEMENTAR UNA VEZ QUE PUEDA HACERSE. TODAVIA NO ESTA DESARROLLADO POR LOS MUCHACHOS DE FIREBASE
            //VER SI SE PUEDE IMPLEMENTAR CON GOOGLE CLOUD

            /*if(deleteAllContent){
                StorageReference groupStorageRef = FirebaseStorage.getInstance().getReference("Groups").child(groupKey);
                groupStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Grupo eliminado del storage", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error al eliminar grupo del storage", Toast.LENGTH_SHORT).show();
                    }
                });
            }*/


            //ME MUEVO AL MAIN ACTIVITY SI FUI YO EL QUE SALI
            if(userId.equals(StaticFirebaseSettings.currentUserId)){
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(i);
                ((FireApp) context.getApplicationContext()).setGroupKey(null);
                ((FireApp) context.getApplicationContext()).setGroupUsers(null);
                ((FireApp) context.getApplicationContext()).setEvents(null);
                ((FireApp) context.getApplicationContext()).setGroupName(null);
                ((FireApp) context.getApplicationContext()).setGroupPhoto(null);
            }

        }

        private void removeListener1() {
            groupsRef.removeEventListener(groupEventListener);
        }

    }
}
