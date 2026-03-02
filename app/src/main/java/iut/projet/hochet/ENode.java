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
    private static final String DEVICE_ID = "dd5113bf-fa1a-4c28-8d92-46b666789db0";
    private static final String USER_ID = "5b3ac0c1-9c73-42cb-ac62-1c740065bf47";
    private static final String USER_SECRET_KEY = "66f2dbc1453d74bffa8e823c63bbaf58a56bae1d";
    public static final String ENODE_AUTH_URL = "https://oauth.sandbox.enode.io/oauth2/token";
    public static final String ENODE_SANDBOX_URL =
            "https://enode-api.sandbox.enode.io/batteries/"+DEVICE_ID;
    public static String ENODE_ACCESS_TOKEN = "";

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
        StringRequest postRequest = new StringRequest(Request.Method.POST, ENODE_AUTH_URL,
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
                String credentials = USER_ID+":"+USER_SECRET_KEY;
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
                ENode.ENODE_SANDBOX_URL, null,
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

}
