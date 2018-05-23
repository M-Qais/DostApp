package qais.mirza.dost;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import qais.mirza.dost.Model.User;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegisterationActivity extends AppCompatActivity {

    Button btnsignin;
    Button btnRegister;
    RelativeLayout rootlayout;

    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/arkhip.regular.ttf").
                        setFontAttrId(R.attr.fontPath).
                        build());
        setContentView(R.layout.activity_registeration);

        init();
        Listeners();

//        init for firebase
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null){

            Intent i=new Intent(RegisterationActivity.this,Booking.class);
            startActivity(i);

        }
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
    }

    //initialization for th evariables......
    public void init() {

        btnsignin = findViewById(R.id.btn_signin);
        btnRegister = findViewById(R.id.btn_register);
        rootlayout = findViewById(R.id.root_layout);
    }

    //listeners
    public void Listeners() {

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });
        btnsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogindialog();
            }
        });

    }

    //dialogue for the register user....
    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Register");
        dialog.setMessage("Please Enter Email for Register....");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.layout_register, null);
        final MaterialEditText edtEmail = view.findViewById(R.id.etEmail);
        final MaterialEditText edtusername = view.findViewById(R.id.etName);
        final MaterialEditText edtpass = view.findViewById(R.id.etPassword);
        final MaterialEditText edtPhone = view.findViewById(R.id.etPhone);

        dialog.setView(view);

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //check validation....
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootlayout, "Please enter Email Address", Snackbar.LENGTH_SHORT);

                }
                if (TextUtils.isEmpty(edtpass.getText().toString())) {
                    Snackbar.make(rootlayout, "Please enter Password", Snackbar.LENGTH_SHORT);

                }
                if (edtpass.getText().toString().length() < 6) {
                    Snackbar.make(rootlayout, "Please enter Password", Snackbar.LENGTH_SHORT);

                }
                if (TextUtils.isEmpty(edtusername.getText().toString())) {
                    Snackbar.make(rootlayout, "Please enter UserName", Snackbar.LENGTH_SHORT);

                }
                if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                    Snackbar.make(rootlayout, "Please enter Phone Number", Snackbar.LENGTH_SHORT);

                }


                //register new user.....

                mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtpass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //save user to db...
                                User user = new User();
                                user.setEmail(edtEmail.getText().toString());
                                user.setName(edtusername.getText().toString());
                                user.setPassword(edtpass.getText().toString());
                                user.setPhone(edtPhone.getText().toString());

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                        setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootlayout, "Registered Successfully", Snackbar.LENGTH_SHORT).
                                                        show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootlayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT).
                                                show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootlayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT).
                                show();
                    }
                });

            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //dialogue for login button....

    private void showLogindialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Login");
        dialog.setMessage("Please Enter Email for SignIn....");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.layout_login, null);
        final MaterialEditText edtEmail = view.findViewById(R.id.etEmail);
        final MaterialEditText edtpass = view.findViewById(R.id.etPassword);


        dialog.setView(view);

        dialog.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //check validation....
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootlayout, "Please enter Email Address", Snackbar.LENGTH_SHORT);

                }
                if (TextUtils.isEmpty(edtpass.getText().toString())) {
                    Snackbar.make(rootlayout, "Please enter Password", Snackbar.LENGTH_SHORT);

                }
                if (edtpass.getText().toString().length() < 6) {
                    Snackbar.make(rootlayout, "Please enter Password", Snackbar.LENGTH_SHORT);

                }


                //Login.....

                mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtpass.getText().toString()).
                        addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(RegisterationActivity.this, Booking.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootlayout, "Failed : " + e.getMessage(), Snackbar.LENGTH_SHORT).
                                show();
                    }
                });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
