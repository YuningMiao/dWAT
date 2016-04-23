package dwat.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabAdapter extends FragmentStatePagerAdapter{
    public TabAdapter(FragmentManager fm) { super(fm); }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new Tab_Suggestions();
            case 1:
                return new Tab_Menu();
            default:
                return null;
        }
    }

    @Override
    public int getCount() { return 2; }
}
