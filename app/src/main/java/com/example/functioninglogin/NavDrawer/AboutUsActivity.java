package com.example.functioninglogin.NavDrawer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.functioninglogin.R;

public class AboutUsActivity extends AppCompatActivity {

    private int[] photoIds = {
            R.drawable.joytechv1, R.drawable.joytechv1, R.drawable.joytechv1,
            R.drawable.joytechv1, R.drawable.joytechv1, R.drawable.joytechv1
    };

    private String[] names = {
            "Derek Gonzalez Fuentes", "Joshua Perez Guzman", "José Tavarez Novas",
            "Adrian Torres Gonzalez", "Fernando La Menza Escalante", "José Mojica Meléndez"
    };

    private String[] roles = {
            "Lead Developer", "Research and Database Designer", "UI/UX Designer",
            "Documentation and Reporting Specialist", "Tester/Quality Assurance", "Project Manager"
    };

    private String[] descriptions = {
            "Derek es el motor técnico detrás del proyecto. Su experiencia en desarrollo de software ha sido esencial para la implementación de las funcionalidades clave de la aplicación. Como líder de desarrollo, ha coordinado la estructura del código, guiado decisiones técnicas y asegurado el buen funcionamiento del sistema.",
            "Joshua se ha encargado de investigar tecnologías adecuadas y diseñar la base de datos del sistema. Gracias a su enfoque meticuloso y estructurado, el backend de la aplicación cuenta con una arquitectura sólida, eficiente y segura.",
            "José Tavarez ha sido responsable del diseño visual y la experiencia del usuario. Desde la navegación intuitiva hasta los colores y la disposición visual, su trabajo ha garantizado una interfaz atractiva y accesible para todos los usuarios.",
            "Adrian ha documentado todos los aspectos técnicos y funcionales del proyecto. Su trabajo permite comprender fácilmente cómo está construido el sistema y facilita su mantenimiento y presentación ante terceros.",
            "Fernando ha asegurado que la aplicación funcione correctamente, identificando y reportando errores durante el proceso de desarrollo. Su atención al detalle ha sido clave para entregar un producto estable y confiable.",
            "José Mojica ha liderado la planificación y coordinación del equipo. Ha supervisado el progreso del proyecto, asignado tareas y asegurado que todos los objetivos se cumplieran a tiempo, manteniendo al equipo enfocado y motivado."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        Toolbar toolbar = findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        LinearLayout container = findViewById(R.id.member_container);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < names.length; i++) {
            View card = inflater.inflate(R.layout.card_about_member, container, false);

            ImageView photo = card.findViewById(R.id.member_photo);
            TextView name = card.findViewById(R.id.member_name);
            TextView role = card.findViewById(R.id.member_role);

            photo.setImageResource(photoIds[i]);
            name.setText(names[i]);
            role.setText("Rol: " + roles[i]);

            int finalI = i;
            card.setOnClickListener(v -> showMemberDialog(names[finalI], roles[finalI], descriptions[finalI], photoIds[finalI]));

            container.addView(card);
        }
    }

    private void showMemberDialog(String name, String role, String description, int imageResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_member_info, null);

        TextView nameText = dialogView.findViewById(R.id.dialog_member_name);
        TextView roleText = dialogView.findViewById(R.id.dialog_member_role);
        TextView descText = dialogView.findViewById(R.id.dialog_member_description);
        ImageView photoView = dialogView.findViewById(R.id.dialog_member_photo);

        nameText.setText(name);
        roleText.setText(role);
        descText.setText(description);
        photoView.setImageResource(imageResId);

        builder.setView(dialogView)
                .setPositiveButton("Cerrar", null)
                .create()
                .show();
    }
}
