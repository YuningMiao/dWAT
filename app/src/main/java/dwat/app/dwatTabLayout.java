package dwat.app;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class dwatTabLayout extends TabLayout {
    public dwatTabLayout(Context context) {
        super(context);
    }

    public dwatTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public dwatTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void createTabs() {
        addTab(R.string.Suggestions);
        addTab(R.string.Menu);
    }

    private void addTab(@StringRes int contentDescriptionId) {
        View tabView = LayoutInflater.from(getContext()).inflate(R.layout.tab_icon, null);
        TextView tabHead = (TextView) tabView.findViewById(R.id.tabText);
        tabHead.setText(contentDescriptionId);
        Tab tab = newTab();
        tab.setCustomView(tabView).setContentDescription(contentDescriptionId);
        addTab(tab);
    }
}
