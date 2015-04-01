package world.interesting.panche.interestingworld;

/**
 * Created by Panche on 31/03/2015.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.app.ToolbarActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

/**
 * Created by neokree on 30/12/14.
 */
public class FragmentProfile extends Fragment implements MaterialTabListener {
    MaterialTabHost tabHost;
    ViewPager pager;
    ViewPagerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_layout,container,false);

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        tabHost = (MaterialTabHost) view.findViewById(R.id.tabHost);
        tabHost.setPrimaryColor(getResources().getColor(R.color.colorPrimary));
        pager = (ViewPager) view.findViewById(R.id.pager );

        // init view pager
        adapter = new ViewPagerAdapter(this.getFragmentManager());
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // when user do a swipe the selected tab change
                tabHost.setSelectedNavigationItem(position);

            }
        });

        // insert all tabs from pagerAdapter data
        for (int i = 0; i < adapter.getCount(); i++) {
            tabHost.addTab(
                    tabHost.newTab()
                            .setText(adapter.getPageTitle(i))
                            .setTabListener(this)
            );

        }

        tabHost.setSelectedNavigationItem(0);

        return view;
    }

    @Override
    public void onTabSelected(MaterialTab materialTab) {
        pager.setCurrentItem(materialTab.getPosition());
    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {

    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {

    }


    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        public Fragment getItem(int num) {
            Fragment frag;
            switch(num)
            {
                case 0:
                    frag=new Profile();
                break;
                case 1:
                    frag= new FragmentLocationsUser();
                break;
                case 2:
                    frag=new FragmentPhotosUser();
                break;
                default:
                    frag=new Profile();
                break;
            }
            return frag;

        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
           CharSequence option;
            switch(position)
            {
                case 0:
                    option="Perfil";
                    break;
                case 1:
                    option= "Mis puntos de interés";
                    break;
                case 2:
                    option="Fotografías subidas";
                    break;
                default:
                    option="Perfil";
                    break;
            }
            return option;
        }

    }
}