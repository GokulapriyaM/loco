package android.duke290.com.loco;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by kevinkuo on 4/24/17.
 */

public class LocationFragment extends Fragment {
    private static TextView mCoordinateMsg;
    private static TextView mAddressMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_fragment, container, false);

        mCoordinateMsg = (TextView) view.findViewById(R.id.frag_coordinates);
        mAddressMsg = (TextView) view.findViewById(R.id.frag_address);

        return view;
    }

    public void displayCoords(double latitude, double longitude) {
        mCoordinateMsg.setText("Latitude: " + latitude + ", Longitude: " + longitude);
    }

    public void displayAddress(String address_msg) {
        mAddressMsg.setText(address_msg);
    }
}
