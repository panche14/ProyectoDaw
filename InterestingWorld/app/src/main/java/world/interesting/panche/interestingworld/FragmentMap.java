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
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

public class FragmentMap extends Fragment implements GoogleMap.OnInfoWindowClickListener, LocationListener {

    public MapView mMapView;
    public GoogleMap mMap;
    public Bundle mBundle;
    private ProgressDialog pDialog;
    Bundle bundle= new Bundle();
    View inflatedView;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    double lat_old=50.654458,lng_old=2.383097,zoom_old=3.0;
    int category=0;
    MenuItem selected;
    AsyncHttpClient client = new AsyncHttpClient();
    Boolean cancel=false;
    Double distance;
    int option=0;

    ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
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
        setHasOptionsMenu(true);
        final GoogleMap.OnCameraChangeListener mOnCameraChangeListener =
                new GoogleMap.OnCameraChangeListener() {

                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        getDistance();
                    }
                };
        mMap.setOnCameraChangeListener(mOnCameraChangeListener);
        cancel=false;
        return inflatedView;
    }
    @Override
    public void onStart() {
        super.onStart();
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
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        System.out.println("onresume");

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onDestroyView() {
        client.cancelRequests(this.getActivity(),true);
        mMap=null;
        cancel=true;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onPause();
        mMapView.onPause();
    }
    public void centerCity()
    {
        CameraPosition camPos;
            LatLng center = new LatLng(lat_old, lng_old);
            camPos = new CameraPosition.Builder()
                    .target(center)
                    .zoom(Float.parseFloat(String.valueOf(zoom_old)))
                    .build();

        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);

        mMap.animateCamera(camUpd3);
        loadData(getDistance());
    }

    public void loadData(double distance)
    {
        //Seteamos de nuevo el centro del mapa y el zoom
        lat_old=mMap.getCameraPosition().target.latitude;
        lng_old=mMap.getCameraPosition().target.longitude;
        zoom_old=mMap.getCameraPosition().zoom;

        pDialog = new ProgressDialog(this.getActivity());
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Procesando...");
        pDialog.setCancelable(true);
        pDialog.setMax(100);

        RequestParams params = new RequestParams();
        if(getActivity() instanceof MainActivityUser) {
            params.put("category", ((MainActivityUser) getActivity()).getCategory());
        }else{
            params.put("category", ((MainActivity) getActivity()).getCategory());
        }
        params.put("distance",distance);
        params.put("lat",mMap.getCameraPosition().target.latitude);
        params.put("lng",mMap.getCameraPosition().target.longitude);

        String url=Links.getUrl_get_map();

        client.post(url,params,new AsyncHttpResponseHandler() {
            @Override
            public void onStart()
            {

            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode==200)
                {

                    try {

                        if(cancel==false) {
                            setResult(new String(responseBody));
                        }
                    }catch(JSONException e)
                    {
                        System.out.println("Falla:"+e );
                    }
                }

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getActivity(), "Parece que hay algún problema con la red", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public ArrayList setResult (String result) throws JSONException {

        list.clear();
        String cadenaJSON = result.toString();//Le pasamos a la variable cadenaJSON una cadena de tipo JSON (en este caso es la creada anteriormente)

        JSONObject jsonObject = new JSONObject(cadenaJSON); //Creamos un objeto de tipo JSON y le pasamos la cadena JSON
        String posts = jsonObject.getString("posts");

        JSONArray array = new JSONArray(posts);
        mMap.clear();
        for(int i=0; i < array.length(); i++) {
            JSONObject jsonChildNode = array.getJSONObject(i);
            jsonChildNode = new JSONObject(jsonChildNode.optString("post").toString());

            ArrayList<String> datos = new ArrayList<String>();
            datos.add(jsonChildNode.getString("id"));
            datos.add(jsonChildNode.getString("name"));
            datos.add(jsonChildNode.getString("description"));
            datos.add(jsonChildNode.getString("photo_url"));
            datos.add(jsonChildNode.getString("user_name"));
            datos.add(jsonChildNode.getString("lastname"));
            datos.add(jsonChildNode.getString("id_user"));
            datos.add(jsonChildNode.getString("photo_user"));
            datos.add(jsonChildNode.getString("lat"));
            datos.add(jsonChildNode.getString("lng"));
            datos.add(jsonChildNode.getString("address"));
            datos.add(jsonChildNode.getString("country"));
            datos.add(jsonChildNode.getString("locality"));
            datos.add(jsonChildNode.getString("rating"));
            datos.add(jsonChildNode.getString("id_category"));

            list.add(datos);
            if (cancel == false) {
                addLocation(jsonChildNode.getString("lat"), jsonChildNode.getString("lng"), jsonChildNode.getString("name"), jsonChildNode.getString("description"), jsonChildNode.getString("photo_url"), jsonChildNode.getString("id_category"));
            }
        }
        return  list;
    }
    public void addLocation(String lat,String lng,String name,String description,String url_photo,String category)
    {
        switch(category)
        {
            case "0":
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),
                        Double.parseDouble(lng))).title(name).snippet(description)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                break;
            case "1":
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),
                        Double.parseDouble(lng))).title(name).snippet(description)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                break;
            case "2":
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),
                        Double.parseDouble(lng))).title(name).snippet(description)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                break;
            case "3":
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),
                        Double.parseDouble(lng))).title(name).snippet(description)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                break;
            case "4":
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),
                        Double.parseDouble(lng))).title(name).snippet(description)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                break;
            case "5":
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),
                        Double.parseDouble(lng))).title(name).snippet(description)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                break;
            default:
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),
                        Double.parseDouble(lng))).title(name).snippet(description)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                break;
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
                        url_photo=list.get(i).get(3).toString();
                        i=list.size();
                        //Si es la primera vez que llamamos a la función de cargar imagen debemos utilizar el callback para saber cuando ha finalizado la carga y recargar debidamente
                        if (not_first_time_showing_info_window) {
                            Picasso.with(getActivity())
                                    .load(Links.getUrl_images() + url_photo)
                                    .error(R.drawable.not_found).resize(128, 128).centerCrop()
                                    .into((ImageView) myContentsView.findViewById(R.id.badge));

                            not_first_time_showing_info_window=false;
                        }
                        else
                        {
                            not_first_time_showing_info_window=true;
                            Picasso.with(getActivity())
                                    .load(Links.getUrl_images() + url_photo)
                                    .error(R.drawable.not_found).resize(128,128).centerCrop()
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

                Fragment fragment = new FragmentLocationDetailTabs();

                world.interesting.panche.interestingworld.Location loc= new world.interesting.panche.interestingworld.Location(list.get(i).get(0),
                        list.get(i).get(1),list.get(i).get(2),list.get(i).get(3),list.get(i).get(4),list.get(i).get(5),list.get(i).get(6),
                        list.get(i).get(7),list.get(i).get(8),list.get(i).get(9),list.get(i).get(10)
                        ,list.get(i).get(11),list.get(i).get(12),list.get(i).get(13),list.get(i).get(14));
                if(this.getActivity().getLocalClassName().equals("MainActivity")) {
                    ((MainActivity) getActivity()).SetLocationSelected(loc);
                }else {
                    ((MainActivityUser) getActivity()).SetLocationSelected(loc);
                }
                ((MaterialNavigationDrawer)this.getActivity()).setFragmentChild(fragment,list.get(i).get(1));
                ((MaterialNavigationDrawer)this.getActivity()).onAttachFragment(fragment);
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
    //Buscador por categorias
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        selected= menu.findItem(R.id.list);
        switch (option) {
            //All
            case 0:
                selected.setIcon(R.drawable.location_white);
                break;
            //Monuments
            case 1:
                selected.setIcon(R.drawable.museum_bar);
                break;
            //Museums
            case 2:
                selected.setIcon(R.drawable.art_bar);
                break;
            //Beachs
            case 3:
                selected.setIcon(R.drawable.beach_bar);
                break;
            //Bar
            case 4:
                selected.setIcon(R.drawable.beer_bar);
                break;
            //Restaurant
            case 5:
                selected.setIcon(R.drawable.restaurant_bar);
                break;
            //Fotografias
            case 6:
                selected.setIcon(R.drawable.photograph_white);
                break;
            //Ocio
            case 7:
                selected.setIcon(R.drawable.leisure_white);
                break;
            default:
                selected.setIcon(R.drawable.location_white);
                break;

        }
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click

        switch (item.getItemId()) {
            //All
            case R.id.category0:
                option=0;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setCategory(0);
                }else{
                    ((MainActivity) getActivity()).setCategory(0);
                }
                list.clear();
                loadData(distance);
                selected.setIcon(R.drawable.location_white);
                return true;
            //Monuments
            case R.id.category1:
                option=1;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setCategory(1);
                }else{
                    ((MainActivity) getActivity()).setCategory(1);
                }
                list.clear();
                loadData(distance);
                selected.setIcon(R.drawable.museum_bar);
                return true;
            //Museums
            case R.id.category2:
                option=2;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setCategory(2);
                }else{
                    ((MainActivity) getActivity()).setCategory(2);
                }
                list.clear();
                loadData(distance);
                selected.setIcon(R.drawable.art_bar);
                return true;
            //Beachs
            case R.id.category3:
                option=3;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setCategory(3);
                }else{
                    ((MainActivity) getActivity()).setCategory(3);
                }
                list.clear();
                loadData(distance);
                selected.setIcon(R.drawable.beach_bar);
                return true;
            //Bar
            case R.id.category4:
                option=4;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setCategory(4);
                }else{
                    ((MainActivity) getActivity()).setCategory(4);
                }
                list.clear();
                loadData(distance);
                selected.setIcon(R.drawable.beer_bar);
                return true;
            //Restaurant
            case R.id.category5:
                option=5;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setCategory(5);
                }else{
                    ((MainActivity) getActivity()).setCategory(5);
                }
                list.clear();
                loadData(distance);
                selected.setIcon(R.drawable.restaurant_bar);
                return true;
            //Fotografias
            case R.id.category6:
                option=6;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setCategory(6);
                }else{
                    ((MainActivity) getActivity()).setCategory(6);
                }
                list.clear();
                loadData(distance);
                selected.setIcon(R.drawable.photograph_white);
                return true;
            //Ocio
            case R.id.category7:
                option=7;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setCategory(7);
                }else{
                    ((MainActivity) getActivity()).setCategory(7);
                }
                list.clear();
                loadData(distance);
                selected.setIcon(R.drawable.leisure_white);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public Double getDistance()
    {
        double[] zooms = {21282,16355,10064,5540,2909,1485,752,378,190,95,48,24,12,6,3,1.48,0.74,0.37,0.19};
        if(Math.round(mMap.getCameraPosition().zoom)<19) {
            distance = (zooms[Math.round(mMap.getCameraPosition().zoom)] * (mMapView.getWidth())) / 1000;
        }else{
            distance = (zooms[18] * (mMapView.getWidth())) / 1000;
        }
        if(zoom_old+1<mMap.getCameraPosition().zoom || zoom_old-1 > mMap.getCameraPosition().zoom) {
            loadData(distance);
        }
        else if(mMap.getCameraPosition().target.latitude>lat_old+((distance/1.5)/100) || mMap.getCameraPosition().target.latitude<lat_old-((distance/1.5)/100))
        {
            loadData(distance);
        }else if(mMap.getCameraPosition().target.longitude>lng_old+((distance/1.5)/100) || mMap.getCameraPosition().target.longitude<lng_old-((distance/1.5)/100))
        {
            loadData(distance);
        }
        return distance;
    }



}
