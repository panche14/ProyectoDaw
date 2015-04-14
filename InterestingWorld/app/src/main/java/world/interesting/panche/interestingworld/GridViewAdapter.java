package world.interesting.panche.interestingworld;

/**
 * Created by Alex on 05/03/2015.
 */


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class GridViewAdapter extends BaseAdapter {
    private final Context context;
    private List<String> urls = new ArrayList<String>();
    public ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();

    public GridViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        }

        // Get the image URL for the current position.
        String url = getItem(position);
        Class cl=context.getClass();
        if(cl.getName().equals("world.interesting.panche.interestingworld.MainActivity")) {

            ((MainActivity) context).getmPicasso() //
                    .load("http://"+url)//.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).skipMemoryCache()
                    .placeholder(R.drawable.back1) //
                    .error(R.drawable.not_found) //
                    .resize(256,256).centerCrop()//
                    .tag(context)
                    .into(view);
            ((MainActivity) context).getmPicasso() .invalidate("http://"+url);

        }else {

            ((MainActivityUser) context).getmPicasso() //
                    .load("http://"+url)//.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).skipMemoryCache()
                    .placeholder(R.drawable.back1) //
                    .error(R.drawable.not_found) //
                    .resize(256,256).centerCrop()//
                    .tag(context)
                    .into(view);
            ((MainActivityUser) context).getmPicasso() .invalidate("http://"+url);
        }




        return view;
    }

    @Override public int getCount() {
        return urls.size();
    }

    @Override public String getItem(int position) {
        return urls.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    public String getId(int position) {
        return list.get(position).get(0);
    }
    public List<String> getInfoSelectedPhoto(int position)
    {
        return list.get(position);
    }

    public void changeModelList(List<String> models, ArrayList<ArrayList<String>> new_list) {
        urls.clear();
        urls.addAll(models);
        list=new_list;
        System.out.println("LIST: "+list);
        System.out.println("URLS: "+urls);
        notifyDataSetChanged();
    }



}
