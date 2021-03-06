package world.interesting.panche.interestingworld;

/**
 * Created by Alex on 15/01/2015.
 */

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cuneytayyildiz.widget.PullRefreshLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FragmentPhotos extends Fragment implements FragmentDialogPhoto.onRateImage {
    View inflatedView;
    GridViewAdapter gridAdapter;
    private SweetAlertDialog pDialog;
    ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
    private final List<String> urls = new ArrayList<String>();
    public List<String> select_image = new ArrayList<String>();
    FragmentManager fm;
    PullRefreshLayout layout;
    int option=0;
    MenuItem selected;
    GridView gv;
    View emptyView;
    AsyncHttpClient client=new AsyncHttpClient();
    ImageButton reload;




    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        inflatedView = inflater.inflate(R.layout.photos_list, container, false);
        TextView text = new TextView(this.getActivity());
        text.setText(this.getResources().getString(R.string.photos));
        text.setGravity(Gravity.CENTER);
        setHasOptionsMenu(true);
        fm= this.getActivity().getSupportFragmentManager();

        gridAdapter=new GridViewAdapter(this.getActivity());
        emptyView = (View) inflatedView.findViewById(R.id.empty_view);

        gv = (GridView) inflatedView.findViewById(R.id.grid_view);
        gv.setAdapter(gridAdapter);
        gv.setOnScrollListener(new SampleScrollListener(getActivity()));
        gv.setEmptyView(emptyView);
        gv.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        reload = (ImageButton) inflatedView.findViewById(R.id.ic1);

        // listen refresh event
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });


        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                select_image=gridAdapter.getInfoSelectedPhoto(position);
                dialogPhoto();
            }
        });

        layout = (PullRefreshLayout) inflatedView.findViewById(R.id.swipeRefreshLayout);

        // listen refresh event
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                urls.clear();
                loadData();

            }
        });
        urls.clear();
        loadData();
        return inflatedView;
    }
    @Override
    public void onDestroy() {
        client.cancelAllRequests(true);
        super.onDestroy();
    }
    @Override
    public void onDestroyView() {
        client.cancelRequests(this.getActivity(),true);
        super.onDestroyView();
    }
    public void loadData()
    {
        pDialog = new SweetAlertDialog(this.getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Cargando...");
        client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        if(getActivity() instanceof MainActivityUser) {
            params.put("search",((MainActivityUser) getActivity()).getAdvancedSearch());
            params.put("category", ((MainActivityUser) getActivity()).getCategory());
        }else{
            params.put("search",((MainActivity) getActivity()).getAdvancedSearch());
            params.put("category", ((MainActivity) getActivity()).getCategory());
        }

        String url=Links.getUrl_get_images();

        client.post(url,params,new AsyncHttpResponseHandler() {
            @Override
            public void onStart()
            {
                pDialog.setCancelable(true);
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
                        list.clear();
                        urls.clear();
                        changeAdapter();
                    }
                }
                pDialog.hide();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.hide();
            }
        });
    }

    public ArrayList setResult (String result) throws JSONException {

        list.clear();
        urls.clear();
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
            datos.add(jsonChildNode.getString("rating_image"));
            datos.add(jsonChildNode.getString("id_image"));
            datos.add(jsonChildNode.getString("id_category"));
            list.add(datos);
            urls.add(jsonChildNode.getString("photo_url"));
        }
        changeAdapter();
        return  list;
    }

    private void dialogPhoto() {
        // custom dialog
        FragmentDialogPhoto dFragment = new FragmentDialogPhoto();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("id", select_image.get(0));
        args.putString("url", select_image.get(3));
        args.putString("lat", select_image.get(8));
        args.putString("lng", select_image.get(9));
        args.putString("name", select_image.get(1));
        args.putString("description", select_image.get(2));
        dFragment.setArguments(args);
        world.interesting.panche.interestingworld.Location loc= new world.interesting.panche.interestingworld.Location(select_image.get(0),
                select_image.get(1),select_image.get(2),select_image.get(3),select_image.get(4),select_image.get(5),
                select_image.get(6),select_image.get(7),select_image.get(8),select_image.get(9),
                select_image.get(10),select_image.get(11),select_image.get(12),select_image.get(13),select_image.get(16));
        if(this.getActivity().getLocalClassName().equals("MainActivity")) {
            ((MainActivity) getActivity()).SetLocationSelected(loc);
        }else {
            ((MainActivityUser) getActivity()).SetLocationSelected(loc);
            ((MainActivityUser) getActivity()).SetImageUrlFull(select_image.get(3),select_image.get(15));
        }
        dFragment.setTargetFragment(this, 0);
        dFragment.show(fm, "Dialog Photo");
    }
    public List<String> getInfoPhoto()
    {
        return select_image;
    }

    //Buscador por categorias
    //Buscador por categorias
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_advanced, menu);
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
            case 8:
                selected.setIcon(R.drawable.leisure_white);
                break;
            default:
                selected.setIcon(R.drawable.advanced);
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
                    ((MainActivityUser) getActivity()).setAdvancedSearch("");
                    ((MainActivityUser) getActivity()).setCategory(0);
                }else{
                    ((MainActivity) getActivity()).setAdvancedSearch("");
                    ((MainActivity) getActivity()).setCategory(0);
                }
                loadData();
                selected.setIcon(R.drawable.location_white);
                return true;
            //Monuments
            case R.id.category1:
                option=1;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setAdvancedSearch("");
                    ((MainActivityUser) getActivity()).setCategory(1);
                }else{
                    ((MainActivity) getActivity()).setAdvancedSearch("");
                    ((MainActivity) getActivity()).setCategory(1);
                }
                loadData();
                selected.setIcon(R.drawable.museum_bar);
                return true;
            //Museums
            case R.id.category2:
                option=2;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setAdvancedSearch("");
                    ((MainActivityUser) getActivity()).setCategory(2);
                }else{
                    ((MainActivity) getActivity()).setAdvancedSearch("");
                    ((MainActivity) getActivity()).setCategory(2);
                }
                loadData();
                selected.setIcon(R.drawable.art_bar);
                return true;
            //Beachs
            case R.id.category3:
                option=3;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setAdvancedSearch("");
                    ((MainActivityUser) getActivity()).setCategory(3);
                }else{
                    ((MainActivity) getActivity()).setAdvancedSearch("");
                    ((MainActivity) getActivity()).setCategory(3);
                }
                loadData();
                selected.setIcon(R.drawable.beach_bar);
                return true;
            //Bar
            case R.id.category4:
                option=4;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setAdvancedSearch("");
                    ((MainActivityUser) getActivity()).setCategory(4);
                }else{
                    ((MainActivity) getActivity()).setAdvancedSearch("");
                    ((MainActivity) getActivity()).setCategory(4);
                }
                loadData();
                selected.setIcon(R.drawable.beer_bar);
                return true;
            //Restaurant
            case R.id.category5:
                option=5;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setAdvancedSearch("");
                    ((MainActivityUser) getActivity()).setCategory(5);
                }else{
                    ((MainActivity) getActivity()).setAdvancedSearch("");
                    ((MainActivity) getActivity()).setCategory(5);
                }
                loadData();
                selected.setIcon(R.drawable.restaurant_bar);
                return true;
            //Fotografias
            case R.id.category6:
                option=6;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setAdvancedSearch("");
                    ((MainActivityUser) getActivity()).setCategory(6);
                }else{
                    ((MainActivity) getActivity()).setAdvancedSearch("");
                    ((MainActivity) getActivity()).setCategory(6);
                }
                loadData();
                selected.setIcon(R.drawable.photograph_white);
                return true;
            //Ocio
            case R.id.category7:
                option=7;
                if(getActivity() instanceof MainActivityUser) {
                    ((MainActivityUser) getActivity()).setAdvancedSearch("");
                    ((MainActivityUser) getActivity()).setCategory(7);
                }else{
                    ((MainActivity) getActivity()).setAdvancedSearch("");
                    ((MainActivity) getActivity()).setCategory(7);
                }
                loadData();
                selected.setIcon(R.drawable.leisure_white);
                return true;
            case R.id.category8:
                option=8;
                FragmentManager fm = this.getActivity().getSupportFragmentManager();
                FragmentDialogAdvanced dFragment = new FragmentDialogAdvanced();
                dFragment.setTargetFragment(this,1);
                dFragment.show(fm, "Dialog Fragment");
                selected.setIcon(R.drawable.advanced);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Callback del dialog fragment avanzado
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1)
        {
            if(resultCode == 1)
            {
               loadData();
            }
        }
    }
    public void changeAdapter()
    {

        gridAdapter.changeModelList(urls,list);

        // refresh complete
        layout.setRefreshing(false);
    }



    @Override
    public void onRateImage(String State) {
        loadData();
    }
}
