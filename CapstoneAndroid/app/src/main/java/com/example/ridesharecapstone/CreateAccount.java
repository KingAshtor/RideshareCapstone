package com.example.ridesharecapstone;

import static com.example.ridesharecapstone.api.retrofit.Api.API;
import static com.example.ridesharecapstone.api.retrofit.Api.GSON;
import static com.example.ridesharecapstone.api.retrofit.Api.handle;
import static com.example.ridesharecapstone.util.Hash.doubleHash;
import static com.example.ridesharecapstone.util.ToastUtils.toast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ridesharecapstone.api.User;
import com.example.ridesharecapstone.api.retrofit.Api;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import okhttp3.MediaType;
import okhttp3.RequestBody;

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
        //Make sure smoker/eater checkboxes don't have more than 1 choice selected
        final Set<CheckBox> smokerPreferences = new HashSet<CheckBox>() {{
            add(smoker); add(nonSmoker); add(anySmoker);
        }};
        smokerPreferences.forEach(checkBox -> checkBox.setOnCheckedChangeListener((_1, _2) -> {
            if (smokerPreferences.stream().map(CompoundButton::isChecked)
                    .filter(checked -> checked).count() <= 1
            ) return;
            smokerPreferences.forEach(preference -> {
                if (preference == checkBox) return;
                preference.setChecked(false);
            });
        }));
        final Set<CheckBox> eaterPreferences = new HashSet<CheckBox>() {{
            add(eater); add(nonEater); add(anyEater);
        }};
        eaterPreferences.forEach(checkBox -> checkBox.setOnCheckedChangeListener((_1, _2) -> {
            if (eaterPreferences.stream().map(CompoundButton::isChecked)
                    .filter(checked -> checked).count() <= 1
            ) return;
            eaterPreferences.forEach(preference -> {
                if (preference == checkBox) return;
                preference.setChecked(false);
            });
        }));

        submit.setOnClickListener(view -> CompletableFuture.supplyAsync(() -> {
            // Test validity of form data
            final Matcher matcher = EMAIL_PATTERN.matcher(txt(email));
            if (Arrays.asList(
                    txt(fName), txt(lName), txt(email),
                    txt(line1), txt(city), txt(state), txt(zip),
                    txt(password), txt(confirm)
            ).contains("") || !matcher.find()) return false;
            if (!driver.isChecked() && !rider.isChecked()) return false;
            //Test whether user already exists
            final Api.ApiResponse<JsonObject> userResponse =
                    handle(API.getUser(txt(email)), JsonObject.class);
            if (!userResponse.hasError() && userResponse.getCode() == 200) {
                toast(this, "This email is already in use.");
                return false;
            }
            // Add user address
            final Api.ApiResponse<Integer> addrResponse = handle(API.addAddr(
                    txt(line1), txt(line2),
                    txt(city), txt(state), txt(zip)
            ), Integer.class);
            if (addrResponse.hasError() || addrResponse.getCode() != 200) {
                toast(this, "An error occurred creating addr.");
                return false;
            }
            final Integer addrId = addrResponse.getBody();
            final String salt = UUID.randomUUID().toString(),
                    finalHash = doubleHash(password.getText().toString(), salt);
            final Map<String, Object> data = new HashMap<String, Object>() {{
                put("email", txt(email));
                put("fName", txt(fName));
                put("lName", txt(lName));
                put("salt", salt);
                put("hashedPwd", finalHash);
                put("homeAddress", addrId);
            }};
            //Create user with data
            final Api.ApiResponse<Void> createUserResponse =
                    handle(API.putUser(RequestBody.create(MediaType.parse("application/json"), GSON.toJson(new HashMap<>(data)))), Void.class);
            if (createUserResponse.hasError() || createUserResponse.getCode() != 200) {
                toast(this, "An error occurred creating user.");
                handle(API.delAddr(addrId), Void.class);
                return false;
            }
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
                handle(API.addRole(txt(email), role), Void.class);
            return true;
        }).thenAccept(success -> {
            if (success) startActivity(new Intent(this, ProfilePage.class));
        }));
    }
}