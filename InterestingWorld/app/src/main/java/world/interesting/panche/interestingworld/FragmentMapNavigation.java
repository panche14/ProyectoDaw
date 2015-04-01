package world.interesting.panche.interestingworld;

/**
 * Created by Alex on 15/01/2015.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

public class FragmentMapNavigation extends Fragment implements GoogleMap.OnInfoWindowClickListener, LocationListener {

    public MapView mMapView;
    public GoogleMap mMap;
    public Bundle mBundle;
    private ProgressDialog pDialog;
    Bundle bundle= new Bundle();
    View inflatedView;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
    Bitmap bmImg;
    boolean not_first_time_showing_info_window=false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.map, container, false);

        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            System.out.println("error map");
// TODO handle this situation
        }
        System.out.println("OncreateView");
        mMapView = (MapView) inflatedView.findViewById(R.id.map);
        mMapView.onCreate(mBundle);
        locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        setUpMapIfNeeded(inflatedView);

        return inflatedView;
    }


    @Override
    public void onStart() {
        super.onStart();
        loadData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        if (mMap != null) {setUpMap();}
    }

    private void setUpMapIfNeeded(View inflatedView) {
        if (mMap == null) {
            mMap = ((MapView) inflatedView.findViewById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this);
        centerCity();
        System.out.println("Mapa seteado");
        //mMap.animateCamera(CameraUpdateFactory.zoomBy(15));
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        System.out.println("onresume");
        setUpMap();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMap=null;
    }

    @Override
    public void onDestroy() {
        super.onPause();
        mMapView.onPause();
        System.out.println("OnDestroy");
    }
    public void centerCity()
    {
        CameraPosition camPos;

            LatLng center = new LatLng(50.654458, 2.383097);
            camPos = new CameraPosition.Builder()
                    .target(center)   //Centramos el mapa en Madrid
                    .zoom(3)         //Establecemos el zoom en 19
                    .build();

        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);

        mMap.animateCamera(camUpd3);
    }
    public void loadData()
    {

        pDialog = new ProgressDialog(this.getActivity());
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Procesando...");
        pDialog.setCancelable(true);
        pDialog.setMax(100);
        AsyncHttpClient client = new AsyncHttpClient();

        String url="http://interestingworld.webcindario.com/consulta_locations.php";



        client.post(url,new AsyncHttpResponseHandler() {
            @Override
            public void onStart()
            {
                pDialog.setProgress(0);
                pDialog.show();
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode==200)
                {

                    try {
                        System.out.println(new String(responseBody));
                        setResult(new String(responseBody));


                    }catch(JSONException e)
                    {
                        System.out.println("Falla:"+e );
                        Toast.makeText(getActivity(), "Error en el registro, compruebe los campos", Toast.LENGTH_SHORT).show();
                    }
                }
                pDialog.hide();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getActivity(), "Parece que hay algún problema con la red", Toast.LENGTH_SHORT).show();
                pDialog.hide();
            }
        });
    }

    public ArrayList setResult (String result) throws JSONException {

        list.clear();
        String cadenaJSON = result.toString();//Le pasamos a la variable cadenaJSON una cadena de tipo JSON (en este caso es la creada anteriormente)

        JSONObject jsonObject = new JSONObject(cadenaJSON); //Creamos un objeto de tipo JSON y le pasamos la cadena JSON
        String posts = jsonObject.getString("posts");

        JSONArray array = new JSONArray(posts);
        for(int i=0; i < array.length(); i++) {
            JSONObject jsonChildNode = array.getJSONObject(i);
            jsonChildNode = new JSONObject(jsonChildNode.optString("post").toString());

            ArrayList<String> datos = new ArrayList<String>();
            datos.add(jsonChildNode.getString("id"));
            datos.add(jsonChildNode.getString("name"));
            datos.add(jsonChildNode.getString("description"));
            datos.add(jsonChildNode.getString("lat"));
            datos.add(jsonChildNode.getString("lng"));
            datos.add(jsonChildNode.getString("photo_url"));
            datos.add(jsonChildNode.getString("email"));
            list.add(datos);
            addLocation(jsonChildNode.getString("lat"),jsonChildNode.getString("lng"),jsonChildNode.getString("name"),jsonChildNode.getString("photo_url"));
        }
        return  list;
    }
    public void addLocation(String lat,String lng,String name,String url_photo)
    {
        if(!lat.equals("0")) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),
                    Double.parseDouble(lng))).title(name).snippet(name));
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater(mBundle).inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoContents(final Marker marker) {

            TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = ((TextView) myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText(marker.getSnippet());
            String url_photo="";

            //Cargamos la imagen dinamicamente
            try {
                int i=0;
                while(i<list.size())
                {
                    if(list.get(i).get(1).equals(marker.getTitle()))
                    {
                        final Boolean[] finish = {false};
                        url_photo=list.get(i).get(5).toString();
                        i=list.size();
                        //Si es la primera vez que llamamos a la función de cargar imagen debemos utilizar el callback para saber cuando ha finalizado la carga y recargar debidamente
                        if (not_first_time_showing_info_window) {
                            Picasso.with(getActivity())
                                    .load("http://" + url_photo)
                                    .error(R.drawable.not_found).skipMemoryCache()
                                    .into((ImageView) myContentsView.findViewById(R.id.badge));
                            not_first_time_showing_info_window=false;
                        }
                        else
                        {
                            not_first_time_showing_info_window=true;
                            Picasso.with(getActivity())
                                    .load("http://" + url_photo)
                                    .error(R.drawable.not_found).skipMemoryCache()
                                    .into((ImageView) myContentsView.findViewById(R.id.badge), new InfoWindowRefresher(marker));
                        }

                        System.out.println(url_photo);
                        return myContentsView;
                    }
                    i++;
                }

            }catch(Exception e)
            {
                System.out.println("Error cargando imagen: "+e);
                return myContentsView;
            }

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }
    @Override
    public void onInfoWindowClick(Marker marker) {
        int i=0;
        while(i<list.size())
        {
            if(list.get(i).get(1).equals(marker.getTitle()))
            {
                String id_location=list.get(i).get(0);
                System.out.println(id_location);

                Fragment fragment = new FragmentLocationDetail();
                List<String> info=list.get(i);
                setInfoBundle(info);
                fragment.setArguments(bundle);
                ((MaterialNavigationDrawer)this.getActivity()).setFragmentChild(fragment,marker.getTitle());
            }
            i++;
        }

    }

    //Refresca la ventana de información
    private class InfoWindowRefresher implements Callback {
        private Marker markerToRefresh;

        private InfoWindowRefresher(Marker markerToRefresh) {
            this.markerToRefresh = markerToRefresh;
        }

        @Override
        public void onSuccess() {
            markerToRefresh.showInfoWindow();
        }

        @Override
        public void onError() {}
    }
    public void setInfoBundle(List<String> info)
    {
        bundle.putString("id_location",info.get(0).toString());
        bundle.putString("title",info.get(1).toString());
        bundle.putString("description",info.get(2).toString());
        bundle.putString("lat",info.get(3).toString());
        bundle.putString("lng", info.get(4).toString());
        bundle.putString("url_photo",info.get(5).toString());
        bundle.putString("user_location",info.get(6).toString());
    }


}
