package com.softcosta.visual_sign;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SignalMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final int FADED_RED = 0xA0FF0000;
    private List<PolygonOptions> pols = new ArrayList<>();
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng arg0) {
                // TODO Auto-generated method stub
                Log.d("arg0", arg0.latitude + "-" + arg0.longitude);
                addPoint(arg0);
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                refreshMap();
                mMap.addMarker(new MarkerOptions().position(arg0).title("Point"));
            }
        });

        Random rnd = new Random(System.currentTimeMillis());


        PolygonOptions polygon = createTriangle(new LatLng(-45, 90), new LatLng(45, -90), new LatLng(-45, -90));
        polygon.fillColor(FADED_RED);

        pols.add(polygon);

        polygon = createTriangle(new LatLng(-45, 90), new LatLng(45, 90), new LatLng(45, -90));
        polygon.fillColor(FADED_RED);

        pols.add(polygon);

        List<LatLng> newPoints = new ArrayList<>();
//        for(int x = 0 ; x < 200 ; x++ ) {
//            LatLng point = new LatLng((rnd.nextDouble() * 90) - 45, (rnd.nextDouble() * 180) - 90);
//            newPoints.add(point);
//            mMap.addMarker(new MarkerOptions().position(point).title("Point " + x));
//        }

//        addPoints(pols, newPoints);

        refreshMap();
    }

    private void refreshMap() {
        mMap.clear();
        for (PolygonOptions pol : pols) {
            mMap.addPolygon(pol);
        }
    }

    private double calculateAngle(LatLng a, LatLng b) {
        return (b.latitude - a.latitude) / (b.longitude - a.longitude); // FIXME: 26/05/2016 - divided by zero error (tan is not advisable)
    }

    private Double calculateVectorAngle(LatLng start, LatLng a, LatLng b) {
        BigDecimal saLat = BigDecimal.valueOf(a.latitude).subtract(BigDecimal.valueOf(start.latitude));
        BigDecimal saLong = BigDecimal.valueOf(a.longitude).subtract(BigDecimal.valueOf(start.longitude));
        BigDecimal distanceA = BigDecimal.valueOf(Math.sqrt(saLat.pow(2).add(saLong.pow(2)).doubleValue()));

        BigDecimal sbLat = BigDecimal.valueOf(b.latitude).subtract(BigDecimal.valueOf(start.latitude));
        BigDecimal sbLong = BigDecimal.valueOf(b.longitude).subtract(BigDecimal.valueOf(start.longitude));
        BigDecimal distanceB = BigDecimal.valueOf(Math.sqrt(sbLat.pow(2).add(sbLong.pow(2)).doubleValue()));

        BigDecimal dotProduct = saLat.multiply(saLong).add(sbLat.multiply(sbLong));

        BigDecimal multiply = distanceA.multiply(distanceB);

        BigDecimal divide = BigDecimal.ZERO;
        divide = dotProduct.divide(multiply, 5, BigDecimal.ROUND_UP);
        if(divide.doubleValue() > 1) {
            Log.d(SignalMapsActivity.class.getSimpleName(), "Multiply: " + multiply + "\t doProduct: " + dotProduct + "\tdivide" + divide);
        }

        return Math.acos(divide.doubleValue());
    }

    private boolean isInside(PolygonOptions pol, LatLng point) {
        List<LatLng> points = pol.getPoints();

        LatLng a = points.get(0);
        LatLng b = points.get(1);
        LatLng c = points.get(2);

        Double angleA = calculateVectorAngle(point, a, b);
        Double angleB = calculateVectorAngle(point, b, c);
        Double angleC = calculateVectorAngle(point, c, a);

        Double sumAngles = angleA + angleB + angleC;
        Log.d(SignalMapsActivity.class.getSimpleName(), "Angles : " + angleA + ", " + angleB + ", " + angleC + "\t Sum : " + sumAngles);

        refreshMap();
        return (sumAngles-Math.PI*2) < 0.01 && (sumAngles-Math.PI*2) > -0.01;

        /*double ab = calculateAngle(a, b);
        double ac = calculateAngle(a, c);
        double ap = calculateAngle(a, point);

        if((ap > ab && ap > ac) || (ap < ab && ap < ac)) {
            return false;
        }

        double ba = calculateAngle(b, a);
        double bc = calculateAngle(b, c);
        double bp = calculateAngle(b, point);

        if((bp > ba && bp > bc) || (bp < ba && bp < bc)) {
            return false;
        }

        double ca = calculateAngle(c, a);
        double cb = calculateAngle(c, b);
        double cp = calculateAngle(c, point);


        if((cp > ca && cp > cb) || (cp < ca && cp < cb)) {
            return false;
        } else {
            return true;
        }*/
    }

    private boolean isInsidePolygon(LatLng dot, PolygonOptions polygon) {
        List<LatLng> points = polygon.getPoints();

        double maxLong=0, maxLat=0;
        double minLong=999, minLat=999;

        for(LatLng point : points) {
            if(point.latitude > maxLat) {
                maxLat = point.latitude;
            }

            if(point.latitude < minLat) {
                minLat = point.latitude;
            }

            if(point.longitude > maxLong) {
                maxLong = point.longitude;
            }

            if(point.longitude < minLong) {
                minLong = point.longitude;
            }
        }

        if(dot.latitude > maxLat || dot.latitude < minLat
                || dot.longitude > maxLong || dot.longitude < minLong) {
            return false;
        }

        LatLng pointA = points.get(0);
        LatLng pointB = points.get(1);
        LatLng pointC = points.get(2);

        double mab = (pointA.latitude - pointB.latitude) / (pointA.longitude - pointB.longitude);
        double mac = (pointA.latitude - pointC.latitude) / (pointA.longitude - pointC.longitude);
        double mbc = (pointB.latitude - pointC.latitude) / (pointB.longitude - pointC.longitude);

        double bab = -mab*pointA.longitude+ pointA.latitude;
        double bac = -mac*pointA.longitude+ pointA.latitude;
        double bbc = -mbc*pointB.longitude+ pointB.latitude;

        double yP1 = mab*dot.longitude + bab;
        double yP2 = mac*dot.longitude + bac;
        double yP3 = mbc*dot.longitude + bbc;

        int count = 0;

        if ((yP1 > dot.latitude && yP2 < dot.latitude) || (yP1 < dot.latitude && yP2 > dot.latitude)) {
            count ++;
        }
        if ((yP1 > dot.latitude && yP3 < dot.latitude) || (yP1 < dot.latitude && yP3 > dot.latitude)) {
            count ++;
        }
        if ((yP3 > dot.latitude && yP2 < dot.latitude) || (yP3 < dot.latitude && yP2 > dot.latitude)) {
            count ++;
        }

        //fails - imagine dividing a rectangle in two... the bottom triangle will be confused with the top one

        Log.d(SignalMapsActivity.class.getSimpleName(), "Count: " + count);
        return (count % 2 == 0) && (count > 0) ;
    }

    private void addPoints(List<LatLng> newPoints) {
        List<LatLng> failed = new ArrayList<>();

        for(LatLng point : newPoints) {

            if(!addPoint(point)){
                failed.add(point);
            }
        }

        if(newPoints.size() < pols.size()) {
            Log.d(SignalMapsActivity.class.getSimpleName(), "Repeating");

            addPoints(failed);
        }
    }


    private boolean addPoint(LatLng latLng) {
        List<PolygonOptions> newPols = new ArrayList<>();
        PolygonOptions removable = null;
        for(PolygonOptions polygon : pols) {
            List<LatLng> points = polygon.getPoints();
            boolean contains = isInsidePolygon(latLng, polygon);//PolyUtil.containsLocation(latLng, points, false);
            Log.d(SignalMapsActivity.class.getSimpleName(), "Contains: " + contains);
            if(contains) { //isInside(polygon, latLng)) {
                removable = polygon;

                LatLng pointA = points.get(0);
                LatLng pointB = points.get(1);
                LatLng pointC = points.get(2);

                PolygonOptions newPol = createTriangle(latLng, pointA, pointB);
                newPols.add(newPol);

                newPol = createTriangle(latLng, pointB, pointC);
                newPols.add(newPol);

                newPol = createTriangle(pointA, latLng,  pointC);
                newPols.add(newPol);
                break;
            }
        }

        if(removable != null) {
            pols.remove(removable);
            pols.addAll(newPols);
        }

        return removable != null;
    }

    @NonNull
    private PolygonOptions createTriangle(LatLng a, LatLng b, LatLng c) {
        PolygonOptions newPol = new PolygonOptions();
        newPol.add(a);
        newPol.add(b);
        newPol.add(c);

        newPol.fillColor(FADED_RED);
        return newPol;
    }
}
