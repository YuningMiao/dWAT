package dwat.app;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;


//For expandable list view use BaseExpandableListAdapter
public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context _context;
    private List<String> header; // header titles
    // Child data in format of header title, child title
    private HashMap<String, List<String>> child;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<String>> listChildData) {
        this._context = context;
        this.header = listDataHeader;
        this.child = listChildData;
    }

    public boolean onChildClick(ExpandableListView parent, View row, int groupPosition, int childPosition, long id){
        return false;
    }

//    public boolean onGroupClick(ExpandableListView parent, View row, int groupPosition, long id){
//        if(adapter.getGroup(groupPosition).getChildren() == null){
//
//        }
//        else{
//
//        }
//    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if(groupPosition >= 0 && groupPosition < header.size() && child.containsKey(header.get(groupPosition)) && childPosition >= 0 && childPosition < child.size()) {
            // This will return the child
            return this.child.get(this.header.get(groupPosition)).get(
                    childPosition);
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        // Getting child text
        final String childText = (String) getChild(groupPosition, childPosition);

        // Inflating child layout and setting textview
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.childs, parent, false);
        }

        TextView child_text = (TextView) convertView.findViewById(R.id.child);

        child_text.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(groupPosition >= 0 && groupPosition < this.header.size() && this.child.containsKey(this.header.get(groupPosition))) {
            // return children count
            return this.child.get(this.header.get(groupPosition)).size();
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {

        // Get header position
        return this.header.get(groupPosition);
    }

    @Override
    public int getGroupCount() {

        // Get header size
        return this.header.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        // Getting header title
        String headerTitle = (String) getGroup(groupPosition);

        // Inflating header layout and setting text
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.parents, parent, false);
        }

        TextView header_text = (TextView) convertView.findViewById(R.id.parent);

        header_text.setText(headerTitle);

        // If group is expanded then change the text into bold and change the
        // icon
        if (isExpanded && getChildrenCount(groupPosition) > 0) {
            header_text.setTypeface(null, Typeface.BOLD);
            header_text.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.collapse, 0);
        } else {
            // If group is not expanded then change the text back into normal
            // and change the icon
            if(getChildrenCount(groupPosition) > 0) {
                header_text.setTypeface(null, Typeface.NORMAL);
                header_text.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        R.drawable.expand, 0);
            }
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}