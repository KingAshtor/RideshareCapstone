package com.example.ridesharecapstone;

import static com.example.ridesharecapstone.api.retrofit.Api.API;
import static com.example.ridesharecapstone.api.retrofit.Api.DO_NOTHING;
import static com.example.ridesharecapstone.api.retrofit.Api.GSON;
import static com.example.ridesharecapstone.api.retrofit.Api.enqueue;
import static com.example.ridesharecapstone.util.Hash.doubleHash;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.ridesharecapstone.api.User;
import com.example.ridesharecapstone.util.Hash;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccount extends AppCompatActivity {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._+]+@[\\w]+\\.[\\w]+(\\.[\\w]+)?$");

    private String txt(TextView view) {
        return view.getText().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        final TextView
                fName = findViewById(R.id.fNameInput),
                lName = findViewById(R.id.lNameInput),
                email = findViewById(R.id.PTCEmailInput),
                line1 = findViewById(R.id.Line1AccInput),
                line2 = findViewById(R.id.Line2AccInput),
                city = findViewById(R.id.cityInput),
                state = findViewById(R.id.stateInput),
                zip = findViewById(R.id.zipInput),
                password = findViewById(R.id.passAccInput),
                confirm = findViewById(R.id.passConfAccInput);
        final CheckBox
                driver = findViewById(R.id.giveRB),
                rider = findViewById(R.id.needRB),

                eater = findViewById(R.id.eatRB),
                nonEater = findViewById(R.id.noEatRB),
                anyEater = findViewById(R.id.noPrefRB),

                smoker = findViewById(R.id.smokeRB),
                nonSmoker = findViewById(R.id.dontSmokeRB),
                anySmoker = findViewById(R.id.noSmokeRB);
        final Button submit = findViewById(R.id.submitBtn);
        final AppCompatActivity context = this;

        submit.setOnClickListener(view -> {
            // Test validity of form data
            final Matcher matcher = EMAIL_PATTERN.matcher(txt(email));
            if (Arrays.asList(
                    txt(fName), txt(lName), txt(email),
                    txt(line1), txt(line2), txt(city), txt(state), txt(zip),
                    txt(password), txt(confirm)
            ).contains("") || !matcher.find()) return;

            // Test validity of form data
            if (!driver.isChecked() && !rider.isChecked()) return;
            if (!eater.isChecked() && !nonEater.isChecked() && !anyEater.isChecked()) return;
            if (!smoker.isChecked() && !nonSmoker.isChecked() && !anySmoker.isChecked()) return;

            // Test if user by email already exists
            enqueue((user, userRes) -> {
                //has user
                if (userRes.code() == 200) return;

                // Add address
                enqueue((addrId, addrRes) -> {
                    if (addrRes.code() != 200) return;

                    final String
                            salt = UUID.randomUUID().toString(),
                            finalHash = doubleHash(password.getText().toString(), salt);
                    final Map<String, Object> data = new HashMap<String, Object>() {{
                        put("email", txt(email));
                        put("fName", txt(fName));
                        put("lName", txt(lName));
                        put("salt", salt);
                        put("hashedPwd", finalHash);
                        put("homeAddress", addrId);
                    }};
                    // Create user with data
                    enqueue((_1, createUserRes) -> {
                        // Revert address if error when creating user
                        if (createUserRes.code() != 200)
                            enqueue(DO_NOTHING, context, API.delAddr(addrId), Void.class);
                    }, context, API.putUser(GSON.toJson(data)), Void.class);

                    // Collect roles from inputs
                    final Set<String> roles = new HashSet<>();
                    if (driver.isChecked()) roles.add("driver");
                    if (rider.isChecked()) roles.add("rider");

                    if (eater.isChecked()) roles.add("eater");
                    else if (nonEater.isChecked()) roles.add("noneater");
                    else if (anyEater.isChecked()) roles.add("anyeater");

                    if (smoker.isChecked()) roles.add("smoker");
                    else if (nonSmoker.isChecked()) roles.add("nonsmoker");
                    else if (anySmoker.isChecked()) roles.add("anysmoker");

                    // Add roles to user
                    for (String role : roles)
                        enqueue(DO_NOTHING, context, API.addRole(txt(email), role), Void.class);
                }, context, API.addAddr(
                        txt(line1), txt(line2),
                        txt(city), txt(state), txt(zip)
                ), Integer.class);
            }, context, API.getUser(txt(email)), User.class);
        });
    }
}