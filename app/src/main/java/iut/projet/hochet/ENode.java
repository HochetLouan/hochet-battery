package iut.projet.hochet;

import android.content.Context;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class ENode {
    // curl https://oauth.sandbox.enode.io/oauth2/token -X POST
    // -u BBB:AAA
    // -d "grant_type=client_credentials"
    private static final String ID_USER = "5b3ac0c1-9c73-42cb-ac62-1c740065bf47";
    private static final String SECRET_CLIENT = "218fa2b32225c45b994f68a28a3422a128fcedd2";
    public static final String ENODE_URL_AUTH = "https://oauth.sandbox.enode.io/oauth2/token";
    public static final String ENODE_URL =
            "https://enode-api.sandbox.enode.io/batteries/";
    public static String ENODE_ACCESS_TOKEN = "";
    public static JSONArray batteriesList;
    public static String selectedDeviceId;

    private static RequestQueue requestQueue;
    public static JSONObject datas = null;

    private final Handler handler = new Handler();
    private Runnable updateRunnable;

    private final MutableLiveData<ENode> liveData = new MutableLiveData<>();

    public void addObserver(Observer<? super ENode> observer) {
        liveData.observeForever(observer);
    }

    public void removeObserver(Observer<? super ENode> observer) {
        liveData.removeObserver(observer);
    }

    public void start(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        requestToken();
        // Définition du Runnable
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (!(ENODE_ACCESS_TOKEN.isEmpty()))
                    fetchDatas();
                handler.postDelayed(this, 2000); // Exécute à nouveau après 2 secondes
            }
        };
        handler.post(updateRunnable); // Démarrer la mise à jour automatique
    }

    public void requestToken() {
        Log.d("ENode","requesting token");
        // Création de la requête POST : différent de la méthode GET (encodage des paramètres en base 64)
        StringRequest postRequest = new StringRequest(Request.Method.POST, ENODE_URL_AUTH,
                response -> {
                    try {
                        ENODE_ACCESS_TOKEN = new JSONObject(response).getString("access_token");
                    } catch (JSONException e) {
                        Log.e("ENode",e.toString());
                    }
                    Log.d("ENode","ACCESS_TOKEN : " + ENODE_ACCESS_TOKEN);
                },
                error -> Log.e("ENode",error.toString())
        ) {
            // Ajout des paramètres POST (équivalent à -d "grant_type=client_credentials")
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("grant_type", "client_credentials");
                return params;
            }

            // Ajout de l'en-tête HTTP pour l'authentification Basic (équivalent à -u user:password)
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String credentials = ID_USER+":"+SECRET_CLIENT;
                try {
                    // Encodage en Base64 des identifiants
                    String base64Credentials = Base64.encodeToString(credentials.getBytes("UTF-8"),
                            Base64.NO_WRAP);
                    headers.put("Authorization", "Basic " + base64Credentials);
                } catch (UnsupportedEncodingException error) {
                    Log.e("ENode",error.toString());
                }
                return headers;
            }
        };

        // Ajout de la requête à la file d'attente
        requestQueue.add(postRequest);
    }


    // for fragment Device

    public void fetchDatas() {
        Log.d("ENode","fetching datas");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                ENode.ENODE_URL + selectedDeviceId, null,
                response -> {
                    Log.d("ENode","datas received : " + response);
                    datas = response;
                    // update ControlViewModel
                    if (datas!=null){
                        synchronized (this) {
                            Log.d("ENode"," notifying observers");
                            liveData.setValue(this);
                        }
                    }

                }, error -> Log.e("ENode",error.toString())
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + ENode.ENODE_ACCESS_TOKEN);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
    public void setBatteryOperationMode(String operationMode) {
        if (ENODE_ACCESS_TOKEN.isEmpty()) {
            Log.e("ENode", "Access token is empty, cannot set operation mode.");
            return;
        }

        //Endpoint
        String url = ENODE_URL + selectedDeviceId + "/operation-mode";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("operationMode", operationMode);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url, jsonBody,
                response -> {
                    Log.d("ENode", "Operation mode set successfully: " + response);
                },
                error -> {
                    Log.e("ENode", "Error setting operation mode: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + ENODE_ACCESS_TOKEN);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
    public void fetchBatteries() {

        if (ENODE_ACCESS_TOKEN.isEmpty()) {
            Log.e("ENode", "Access token is empty, cannot fetch batteries.");
            return;
        }
        //Un / après batteries mene vers la mauvaise route dans ENODE_URL
        String url = "https://enode-api.sandbox.enode.io/batteries?pageSize=50";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("ENode", "Batteries response: " + response);
                    try {
                        //Recupere les batteries dans le JSON
                        batteriesList = response.getJSONArray("data");
                        Log.d("ENode", "Batteries found: " + batteriesList.length());
                        synchronized (this) {
                            liveData.setValue(this);
                        }
                    } catch (JSONException e) {
                        Log.e("ENode", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("ENode", "Error fetching batteries: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + ENODE_ACCESS_TOKEN);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        requestQueue.add(request);
    }
}
