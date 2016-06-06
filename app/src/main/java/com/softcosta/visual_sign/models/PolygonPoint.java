package com.softcosta.visual_sign.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivo on 26/05/2016.
 */
public class PolygonPoint {
    public final int x;
    public final int y;

    private final List<PolygonPoint> connections;

    public PolygonPoint(int x, int y) {
        this.x = x;
        this.y = y;
        this.connections = new ArrayList<>();
    }
}
