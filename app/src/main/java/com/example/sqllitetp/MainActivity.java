package com.example.sqllitetp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.example.sqllitetp.R;
import com.example.sqllitetp.classes.Etudiant;
import com.example.sqllitetp.service.EtudiantService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText nom;
    private EditText prenom;
    private Button add;
    private EditText id;
    private Button rechercher;
    private TextView res;
    private Button findAll;
    private GridView studentGrid;

    void clear() {
        nom.setText("");
        prenom.setText("");
        id.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EtudiantService es = new EtudiantService(this);

        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        add = findViewById(R.id.bn);
        id = findViewById(R.id.id);
        rechercher = findViewById(R.id.load);
        res = findViewById(R.id.res);
        findAll = findViewById(R.id.findAll);
        studentGrid = findViewById(R.id.studentGrid);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nom.getText().toString().trim().isEmpty() || prenom.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    return;
                }
                es.create(new Etudiant(nom.getText().toString(), prenom.getText().toString()));
                clear();

                for (Etudiant e : es.findAll()) {
                    Log.d("Etudiant", e.getId() + " " + e.getNom() + " " + e.getPrenom());
                }
            }
        });

        rechercher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idText = id.getText().toString().trim();
                if (idText.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    int studentId = Integer.parseInt(idText);
                    Etudiant e = es.findById(studentId);
                    if (e != null) {
                        res.setText(e.getNom() + " " + e.getPrenom());
                    } else {
                        res.setText("Étudiant non trouvé");
                    }
                } catch (NumberFormatException ex) {
                    Toast.makeText(MainActivity.this, "Veuillez entrer un ID valide", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Etudiant> etudiants = es.findAll();
                List<String> data = new ArrayList<>();

                for (Etudiant e : etudiants) {
                    data.add("ID: " + e.getId() + "\n" + e.getNom() + " " + e.getPrenom());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, data);
                studentGrid.setAdapter(adapter);

                studentGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Etudiant selectedEtudiant = etudiants.get(position);

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Choose an action")
                                .setItems(new String[]{"Update", "Delete"}, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            showUpdateDialog(selectedEtudiant);
                                        } else if (which == 1) {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("Confirmation")
                                                    .setMessage("Are you sure you want to delete this student?")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            // Call delete method
                                                            es.delete(selectedEtudiant);
                                                            Toast.makeText(MainActivity.this, "Student deleted", Toast.LENGTH_SHORT).show();

                                                            // Refresh the student list
                                                            findAll.performClick();
                                                        }
                                                    })
                                                    .setNegativeButton("No", null)
                                                    .show();
                                        }
                                    }
                                })
                                .create()
                                .show();
                    }
                });
            }
        });

    }

    private void showUpdateDialog(Etudiant selectedEtudiant) {
        final EditText newNom = new EditText(MainActivity.this);
        newNom.setText(selectedEtudiant.getNom());

        final EditText newPrenom = new EditText(MainActivity.this);
        newPrenom.setText(selectedEtudiant.getPrenom());

        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(newNom);
        layout.addView(newPrenom);

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Update Student")
                .setMessage("Update the details of the student")
                .setView(layout)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get updated values
                        String updatedNom = newNom.getText().toString().trim();
                        String updatedPrenom = newPrenom.getText().toString().trim();

                        if (updatedNom.isEmpty() || updatedPrenom.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                        } else {
                            selectedEtudiant.setNom(updatedNom);
                            selectedEtudiant.setPrenom(updatedPrenom);
                            new EtudiantService(MainActivity.this).update(selectedEtudiant);

                            Toast.makeText(MainActivity.this, "Student updated", Toast.LENGTH_SHORT).show();

                            findAll.performClick();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}