package com.s.samsungitschool.recom00.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.MalformedJsonException;
import com.s.samsungitschool.recom00.R;
import com.s.samsungitschool.recom00.interfaces.EntryUserService;
import com.s.samsungitschool.recom00.interfaces.MapService;
import com.s.samsungitschool.recom00.maps.MapsActivity;
import com.s.samsungitschool.recom00.model.User;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class NewAppFragmentActivity extends Fragment {

    private static final String URI_FOR_REGISTRATION = "http://188.235.192.155:80";

    AlertDialog.Builder alertDialogBuilderInput;

    Spinner spinnerParking, spinnerPit, spinnerSign;

    Button addressBT, sendBT;
    TextView addressTV;
    EditText commentET;


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private final String NEW_POINT_LAT = "NEW_POINT_LAT";
    private final String NEW_POINT_LNG = "NEW_POINT_LNG";
    private final String AUTH_STRING = "AUTH_STRING";

    private double newPointLat;
    private double newPointLng;

    private String addPointServerAns = "";
    private String addNoteServerAns = "";
    boolean serverError = false;
    boolean sendSuccessfully = true;

    private String authString = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.f_new_app_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addressBT = (Button) getView().findViewById(R.id.address_bt);
        sendBT = (Button) getView().findViewById(R.id.send_bt);

        addressTV = (TextView) getView().findViewById(R.id.address_tv);
        commentET = (EditText) getView().findViewById(R.id.comment_et);

        spinnerParking = (Spinner) getView().findViewById(R.id.spinnerProblemParking);
        spinnerPit = (Spinner) getView().findViewById(R.id.spinnerProblemPit);
        spinnerSign = (Spinner) getView().findViewById(R.id.spinnerProblemSign);


        TabHost tabHost = (TabHost) getView().findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.tab1);
        tabSpec.setIndicator("Парковка");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.tab2);
        tabSpec.setIndicator("Яма");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag3");
        tabSpec.setContent(R.id.tab3);
        tabSpec.setIndicator("Знак");
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);

        final Intent i = new Intent( getActivity(), MapsActivity.class);

        addressBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(i, 1);
            }
        });

        sendBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sharedPreferences = getActivity().getSharedPreferences("SP", MODE_PRIVATE);

                authString = sharedPreferences.getString(AUTH_STRING, "");
                if (!authString.equals("")) {

                    new addPointAsyncTask().execute("");
                    new addNoteAsyncTask().execute("");

                } else {
                    alertDialogBuilderInput.setTitle("Ошибка");
                    alertDialogBuilderInput.setMessage("Вы не авторизовались");
                    serverError = true;
                    sendSuccessfully = false;
                }

                if (sendSuccessfully) {
                    Toast.makeText(getActivity(), "Данные успешно отправлены", Toast.LENGTH_LONG).show();
                }

            }
        });


        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                newPointLat = data.getDoubleExtra(NEW_POINT_LAT, 0);
                newPointLng = data.getDoubleExtra(NEW_POINT_LNG, 0);

                String tempString = newPointLat + " " + newPointLng;
                addressTV.setText(tempString);
                break;
        }
    }

    class addPointAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URI_FOR_REGISTRATION)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            MapService mapService = retrofit.create(MapService.class);
            Call<Object> call = mapService.addPoint(authString, newPointLat, newPointLng);

            Response response = null;
            try {
                response = call.execute();
                addPointServerAns = response.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addPointServerAns != null ) {
                if (!addPointServerAns.equals("")) {

                    if (addPointServerAns.equals("User not authorized")) {
                        alertDialogBuilderInput.setTitle("Ошибка");
                        alertDialogBuilderInput.setMessage("Вы не авторизовались");
                        serverError = true;
                        sendSuccessfully = false;

                    } else if (addPointServerAns.equals("0")) {
                        alertDialogBuilderInput.setTitle("Ошибка");
                        alertDialogBuilderInput.setMessage("Ошибка сервера");
                        serverError = true;
                        sendSuccessfully = false;

                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (serverError) {
                displayAlert("server_error");
            }

        }
    }

    class addNoteAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URI_FOR_REGISTRATION)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            MapService mapService = retrofit.create(MapService.class);
            /*Call<Object> call = mapService.addNote(authString, newPointLat, commentET.toString());

            Response response = null;
            try {
                response = call.execute();
                addNoteServerAns = response.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addNoteServerAns != null ) {
                if (!addNoteServerAns.equals("")) {

                    if (addNoteServerAns.equals("User not authorized")) {
                        alertDialogBuilderInput.setTitle("Ошибка");
                        alertDialogBuilderInput.setMessage("Вы не авторизовались");
                        serverError = true;
                        sendSuccessfully = false;

                    } else if (addNoteServerAns.equals("Point do not exist")) {
                        alertDialogBuilderInput.setTitle("Ошибка");
                        alertDialogBuilderInput.setMessage("Точка не добавлена");
                        serverError = true;
                        sendSuccessfully = false;

                    } else if (addNoteServerAns.equals("Note already exists")) {
                        alertDialogBuilderInput.setTitle("Ошибка");
                        alertDialogBuilderInput.setMessage("Запись уже существует");
                        serverError = true;
                        sendSuccessfully = false;

                    }
                }
            }*/

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (serverError) {
                displayAlert("server_error");
            }

        }
    }

    private void displayAlert(final String code) {
        alertDialogBuilderInput.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (code.equals("server_error")) {

                }
            }
        });

        AlertDialog alertDialog = alertDialogBuilderInput.create();
        alertDialog.show();

    }
}
