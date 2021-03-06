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
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.SubGroupDetailActivity;
import com.andresdlg.groupmeapp.uiPackage.UserProfileSetupActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 17/03/18.
 */

public class RVSubGroupDetailAdapter extends RecyclerView.Adapter<RVSubGroupDetailAdapter.SubGroupDetailViewHolder>{

    private DatabaseReference subGroupsRef;
    private List<Users> usersList;
    private Map<String, String> usersRoles;
    private String groupKey;
    private String subGroupKey;
    private Context context;
    private String myRol;
    private int cantAdmins;

    public RVSubGroupDetailAdapter(List<Users> usersList, Map<String, String> usersRoles, String groupKey,String subGroupKey, Context context){
        this.usersList = usersList;
        this.usersRoles = usersRoles;
        this.groupKey = groupKey;
        this.subGroupKey = subGroupKey;
        this.context = context;
        subGroupsRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey);
    }

    @NonNull
    @Override
    public SubGroupDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_group_details_list_item, parent, false);
        return new SubGroupDetailViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final SubGroupDetailViewHolder groupDetailViewHolder, @SuppressLint("RecyclerView") final int position) {
        Users u = usersList.get(position);
        String rol = null;
        for(Map.Entry<String, String> entry : usersRoles.entrySet()) {
            if(u.getUserid().equals(entry.getKey())){
                rol = entry.getValue();
            }
        }
        if(rol!=null){
            groupDetailViewHolder.setDetails(context,u.getName(),u.getAlias(),rol,u.getImageURL(),u.getUserid());
        }
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
        for(Map.Entry<String, String> entry : usersRoles.entrySet()) {
            if(StaticFirebaseSettings.currentUserId.equals(entry.getKey())){
                myRol = entry.getValue();
            }
        }
    }

    private void setAdminCount(){
        cantAdmins = 0;
        for(Map.Entry<String, String> entry : usersRoles.entrySet()) {
            if(entry.getValue().equals(Roles.SUBGROUP_ADMIN.toString())){
                cantAdmins += 1;
            }
        }
    }


    class SubGroupDetailViewHolder extends RecyclerView.ViewHolder{

        DatabaseReference userRef;

        View mView;

        SubGroupDetailViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

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
            if(myRol.equals(Roles.SUBGROUP_ADMIN.toString())){
                btnMenu.setVisibility(View.VISIBLE);
            }

            userRef = subGroupsRef.child("members").child(iduser);

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



                    if(rol.equals(Roles.SUBGROUP_ADMIN.toString())){
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
                            if(rol.equals(Roles.SUBGROUP_ADMIN.toString()) && myRol.equals(Roles.SUBGROUP_ADMIN.toString())){
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
                                                    userRef.setValue(Roles.SUBGROUP_MEMBER).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            //setAdminCount();
                                                            usersRoles.put(iduser, Roles.SUBGROUP_MEMBER.toString());
                                                            mContactRol.setText("MIEMBRO");
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
                                                    deleteUserFromSubGroup(iduser,getAdapterPosition(), false);
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
                                                                        deleteUserFromSubGroup(iduser,getAdapterPosition(),true);
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
                            }else if(rol.equals(Roles.SUBGROUP_MEMBER.toString()) && myRol.equals(Roles.SUBGROUP_ADMIN.toString())) {
                                //btnMenu.setVisibility(View.VISIBLE);
                                popupMenu.getMenuInflater().inflate(R.menu.activity_group_detail_item_admin_to_member_menu, menu);
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        int id = menuItem.getItemId();
                                        switch (id) {
                                            case R.id.rolAdmin:
                                                //userRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey).child("members").child(iduser);
                                                userRef.setValue(Roles.SUBGROUP_ADMIN).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        usersRoles.put(iduser, Roles.SUBGROUP_ADMIN.toString());
                                                        mContactRol.setText("ADMINISTRADOR");
                                                        notifyDataSetChanged();
                                                    }
                                                });

                                                Toast.makeText(context, "Hacer admin", Toast.LENGTH_SHORT).show();
                                                break;
                                            case R.id.delete:
                                                //rejectRequest(iduser);
                                                deleteUserFromSubGroup(iduser,getAdapterPosition(), false);
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

        private void deleteUserFromSubGroup(final String userId, final int position, boolean deleteSubgroup) {

            //ELIMINAR DE LOS GRUPOS
            subGroupsRef.child("members").child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Borrado del subgrupo", Toast.LENGTH_SHORT).show();
                    /*usersList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, usersList.size());*/
                }
            });

            //ELIMINAR DE LOS SUBGRUPOS
            /*subGroupEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot d : dataSnapshot.getChildren()){
                        DatabaseReference childRef = d.child("members").child(userId).getRef();
                        childRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Borrado de los subgrupos", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    removeListener1();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            subGroupsRef.addListenerForSingleValueEvent(subGroupEventListener);*/

            //ELIMINAR DE LOS USUARIOS
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("groups").child(groupKey).child("subgroups").child(subGroupKey);
            usersRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                }
            });

            //ELIMINAR DEL MAPA DE ROLES
            usersRoles.remove(userId);

            usersList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, usersList.size());

            if(deleteSubgroup){

                DatabaseReference subGroupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey);

                //ELIMINO EL SUBGRUPO DEL NODO GRUPOS
                subGroupRef.removeValue();

                //ELIMINO EL SUBGRUPO DEL NODO USUARIOS PARA TODOS LOS MIEMBROS DE ESTE SUBGRUPO
                DatabaseReference userSubgroupRef = FirebaseDatabase.getInstance().getReference("Users");
                for(Users member: usersList){
                    userSubgroupRef.child(member.getUserid()).child("groups").child(groupKey).child("subgroups").child(subGroupKey).removeValue();
                }

                //CIERRO LA VENTANA Y VUELVO A SUBGROUP FRAGMENT
                ((SubGroupDetailActivity)context).finish();

                //FALTARIA IMPLEMENTAR EL BORRADO DE LOS DIRECTORIOS EN FIREBASE STORAGE UNA VEZ QUE ESO ESTE DISPONIBLE
            }
        }
    }
}
